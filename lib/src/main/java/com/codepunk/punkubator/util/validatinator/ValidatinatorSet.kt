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

import android.content.Context
import com.codepunk.punkubator.R

open class ValidatinatorSet<T> protected constructor(
    context: Context?,
    getInputName: (context: Context?, input: T) -> CharSequence?,
    getInvalidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    getValidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    protected val validatinators: Iterable<Validatinator<T>>,
    protected val constraint: Constraint,
    protected val processAll: Boolean
) : Validatinator<T>(context, getInputName, getInvalidMessage, getValidMessage) {

    override fun isValid(input: T, options: Options): Boolean {
        var valid: Boolean = when (constraint) {
            Constraint.ALL -> true
            Constraint.ANY -> false
        }

        var foundCause = false
        for (innerValidatinator in validatinators) {
            val innerOptions = options.copy()
            val innerValid = innerValidatinator.validate(input, innerOptions)

            // Add elements in inner trace to the outer trace if applicable
            if (options.requestTrace && innerOptions.outTrace != null) {
                innerOptions.outTrace?.run {
                    options.ensureTrace().addAll(this)
                }
            }

            // Check for the "cause" validatinator. This is the validatinator that potentially
            // determines the valid property of the entire set.
            if (!foundCause) {
                foundCause = when {
                    innerValid && constraint == Constraint.ANY -> true
                    !innerValid && constraint == Constraint.ALL -> true
                    else -> false
                }

                if (foundCause) {
                    valid = innerValid
                    options.outMessage = innerOptions.outMessage
                    if (!processAll) {
                        break
                    }
                }
            }
        }

        if (options.requestMessage && options.outMessage == null) {
            options.outMessage = generateMessage(input, valid, options)
        }

        return valid
    }

    open class Builder<T> : AbsBuilder<T, ValidatinatorSet<T>, Builder<T>>() {

        protected val validatinators = ArrayList<Validatinator<T>>()

        protected var constraint: Constraint = Constraint.ALL

        protected var processAll: Boolean = false

        override val thisBuilder: Builder<T> by lazy { this }

        override var getInvalidMessage: (
            context: Context?,
            inputName: CharSequence?
        ) -> CharSequence? = { context, inputName ->
            context?.getString(R.string.validatinator_invalid_set, inputName)
                ?: throw missingContextException("getInvalidMessage")
        }

        override var getValidMessage: (
            context: Context?,
            inputName: CharSequence?
        ) -> CharSequence? = { context, inputName ->
            context?.getString(R.string.validatinator_valid_set, inputName)
                ?: throw missingContextException("getValidMessage")
        }

        override fun build(): ValidatinatorSet<T> = ValidatinatorSet(
            context,
            getInputName,
            getInvalidMessage,
            getValidMessage,
            validatinators,
            constraint,
            processAll
        )

        fun add(vararg validatinator: Validatinator<T>): Builder<T> {
            this.validatinators.addAll(validatinator)
            return thisBuilder
        }

        fun constraint(constraint: Constraint): Builder<T> {
            this.constraint = constraint
            return thisBuilder
        }

        fun processAll(processAll: Boolean): Builder<T> {
            this.processAll = processAll
            return thisBuilder
        }
    }

    enum class Constraint {
        ALL,
        ANY
    }

}
