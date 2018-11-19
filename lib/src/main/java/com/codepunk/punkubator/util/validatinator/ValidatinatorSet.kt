/*
 * Copyright (C) 2018 Codepunk, LLC
 * Author(s): Scott Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codepunk.punkubator.util.validatinator

import java.util.*

open class ValidatinatorSet<T> protected constructor(
    validMessage: (input: T) -> CharSequence?,
    invalidMessage: (input: T) -> CharSequence?,
    protected val validatinators: Iterable<Validatinator<T>>,
    protected val style: Style = Style.ALL,
    protected val validateAll: Boolean = false
) : Validatinator<T>(validMessage, invalidMessage) {

    override fun validate(input: T): Result<T> {
        var cause: Result<T>? = null
        val trace = ArrayList<Result<T>>()
        var anyValid = false
        var anyInvalid = false
        for (validatinator in validatinators) {
            val result = validatinator.validate(input)
            trace.add(result)
            if (result.valid) {
                anyValid = true
                if (style == Style.ANY && cause == null) {
                    cause = result
                }
            } else {
                anyInvalid = true
                if (style == Style.ALL && cause == null) {
                    cause = result
                }
            }
            if (cause != null && !validateAll) {
                break
            }
        }
        val valid = when (style) {
            Style.ANY -> anyValid
            Style.ALL -> !anyInvalid
        }
        val message = cause?.message ?: when (valid) {
            true -> validMessage(input)
            false -> invalidMessage(input)
        }
        return Result(
            valid,
            input,
            message,
            cause,
            Collections.unmodifiableList(trace)
        )
    }

    enum class Style {
        ANY,
        ALL
    }

    open class Builder<T> : AbsBuilder<T, ValidatinatorSet<T>, Builder<T>>() {

        override val thisBuilder: Builder<T> = this

        protected val validatinators = ArrayList<Validatinator<T>>()

        protected var style = Style.ALL

        protected var validateAll = false

        override fun build(): ValidatinatorSet<T> = ValidatinatorSet(
            validMessage,
            invalidMessage,
            validatinators,
            style,
            validateAll
        )

        fun add(vararg validatinators: Validatinator<T>): Builder<T> {
            this.validatinators.addAll(validatinators)
            return this
        }

        fun style(style: Style): Builder<T> {
            this.style = style
            return this
        }

        fun validateAll(validateAll: Boolean): Builder<T> {
            this.validateAll = validateAll
            return this
        }

    }

}
