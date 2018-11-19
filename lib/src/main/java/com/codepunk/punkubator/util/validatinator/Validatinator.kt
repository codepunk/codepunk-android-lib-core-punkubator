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

abstract class Validatinator<T> protected constructor(

    protected val validMessage: (input: T) -> CharSequence?,

    protected val invalidMessage: (input: T) -> CharSequence?

) {

    abstract fun validate(input: T): Result<T>

    class Result<T>(
        val valid: Boolean,
        val input: T,
        val message: CharSequence? = null,
        val cause: Result<T>? = null,
        val trace: Iterable<Result<T>>? = null
    )

    abstract class AbsBuilder<T, V : Validatinator<T>, B : AbsBuilder<T, V, B>> {

        protected var context: Context? = null

        protected var inputName: CharSequence? = null

        protected var validMessage: (input: T) -> CharSequence? = { input ->
            getValidMessage(context, inputName, input)
        }

        protected var invalidMessage: (input: T) -> CharSequence? = { input ->
            getInvalidMessage(context, inputName, input)
        }

        protected abstract val thisBuilder: B

        protected open fun getValidMessage(
            context: Context?,
            inputName: CharSequence?,
            input: T
        ): CharSequence? = context?.getString(
            R.string.validatinator_valid,
            inputName ?: "\"$input\""
        )

        protected open fun getInvalidMessage(
            context: Context?,
            inputName: CharSequence?,
            input: T
        ): CharSequence? = context?.getString(
            R.string.validatinator_invalid,
            inputName ?: "\"$input\""
        )

        fun context(context: Context): B {
            this.context = context
            return thisBuilder
        }

        fun inputName(inputName: CharSequence?): B {
            this.inputName = inputName
            return thisBuilder
        }

        fun validMessage(validMessage: (input: T) -> CharSequence?): B {
            this.validMessage = validMessage
            return thisBuilder
        }

        fun validMessage(validMessage: CharSequence?): B {
            this.validMessage = { validMessage }
            return thisBuilder
        }

        fun invalidMessage(invalidMessage: (input: T) -> CharSequence?): B {
            this.invalidMessage = invalidMessage
            return thisBuilder
        }

        fun invalidMessage(invalidMessage: CharSequence?): B {
            this.invalidMessage = { invalidMessage }
            return thisBuilder
        }

        abstract fun build(): V

    }
}
