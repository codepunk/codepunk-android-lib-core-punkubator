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

package com.codepunk.punkubator.util.validatinatorold

import android.content.Context
import com.codepunk.punkubator.R
import java.lang.Exception
import java.lang.IllegalStateException

abstract class Validatinator<T> protected constructor(

    protected val context: Context?,

    protected val getInputName: (context: Context?, input: T) -> CharSequence?,

    protected val getInvalidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,

    protected val getValidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?

) {

    // region Inherited methods

    override fun toString(): String {
        return javaClass.simpleName + '@' + Integer.toHexString(hashCode())
    }

    // endregion Inherited methods

    // region Methods

    open fun validate(input: T, options: Options = Options()): Boolean {
        val valid = isValid(input, options)
        val message = generateMessage(input, valid, options)
        if (options.requestMessage && options.outMessage == null) {
            options.outMessage = message
        }
        if (options.requestTrace) {
            options.ensureTrace().add(ValidatinatorTraceElement(this, valid, message))
        }
        when (valid) {
            true -> onValid(input, options)
            false -> onInvalid(input, options)
        }
        return valid
    }

    protected open fun onInvalid(input: T, options: Options) {
        // No op
    }

    protected open fun onValid(input: T, options: Options) {
        // No op
    }

    protected fun generateMessage(input: T, valid: Boolean, options: Options): CharSequence? =
        when {
            options.requestMessage || options.requestTrace -> {
                val inputName = options.inputName ?: getInputName(context, input)
                when (valid) {
                    true -> getValidMessage(context, inputName)
                    false -> getInvalidMessage(context, inputName)
                }
            }
            else -> null
        }

    protected abstract fun isValid(input: T, options: Options): Boolean

    // endregion Methods

    // region Companion object

    companion object {

        @JvmStatic
        fun missingContextException(
            methodName: String
        ): Exception = IllegalStateException(
            Validatinator::class.java.simpleName +
                    " requires a context in order to call $methodName method"
        )

        @JvmStatic
        protected fun Options.ensureTrace(): ArrayList<ValidatinatorTraceElement> =
            outTrace ?: ArrayList<ValidatinatorTraceElement>().apply {
                outTrace = this
            }

    }

    // endregion Companion object

    abstract class AbsBuilder<T, V : Validatinator<T>, B : AbsBuilder<T, V, B>> {

        protected var context: Context? = null

        protected open var getInputName: (context: Context?, input: T) -> CharSequence? =
            { context, input ->
                context?.getString(R.string.validatinator_the_value) ?: "\"$input\""
            }

        protected open var getInvalidMessage: (
            context: Context?,
            inputName: CharSequence?
        ) -> CharSequence? = { context, inputName ->
            context?.getString(R.string.validatinator_invalid, inputName)
                ?: throw missingContextException("getInvalidMessage")
        }

        protected open var getValidMessage: (
            context: Context?,
            inputName: CharSequence?
        ) -> CharSequence? = { context, inputName ->
            context?.getString(R.string.validatinator_valid, inputName)
                ?: throw missingContextException("getValidMessage")
        }

        protected abstract val thisBuilder: B

        fun context(context: Context?): B {
            this.context = context
            return thisBuilder
        }

        fun inputName(getInputName: (context: Context?, input: T) -> CharSequence?): B {
            this.getInputName = getInputName
            return thisBuilder
        }

        fun inputName(inputName: CharSequence?): B {
            this.getInputName = { _, _ -> inputName }
            return thisBuilder
        }

        fun invalidMessage(
            getInvalidMessage: (
                context: Context?,
                inputName: CharSequence?
            ) -> CharSequence?
        ): B {
            this.getInvalidMessage = getInvalidMessage
            return thisBuilder
        }

        fun invalidMessage(invalidMessage: CharSequence?): B {
            this.getInvalidMessage = { _, _ -> invalidMessage }
            return thisBuilder
        }

        fun validMessage(
            getValidMessage: (
                context: Context?,
                inputName: CharSequence?
            ) -> CharSequence?
        ): B {
            this.getValidMessage = getValidMessage
            return thisBuilder
        }

        fun validMessage(validMessage: CharSequence?): B {
            this.getValidMessage = { _, _ -> validMessage }
            return thisBuilder
        }

        abstract fun build(): V

    }

    data class ValidatinatorTraceElement(

        val validatinator: Validatinator<*>,

        val valid: Boolean,

        val message: CharSequence?

    )

    class Options {

        var requestMessage: Boolean = false

        var requestTrace: Boolean = false

        var inputName: CharSequence? = null

        var outMessage: CharSequence? = null

        var outTrace: ArrayList<ValidatinatorTraceElement>? = null

        fun clear(): Options = this.apply {
            outMessage = null
            outTrace = null
        }

        fun copy(): Options = Options().apply {
            requestMessage = this@Options.requestMessage
            requestTrace = this@Options.requestTrace
            inputName = this@Options.inputName
        }

    }

}
