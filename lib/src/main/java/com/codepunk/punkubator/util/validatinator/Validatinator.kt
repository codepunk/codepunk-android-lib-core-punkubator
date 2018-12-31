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
import java.lang.IllegalStateException

abstract class Validatinator<in T>(
    context: Context? = null,
    inputName: CharSequence? = null,
    invalidMessage: CharSequence? = null,
    validMessage: CharSequence? = null
) {

    // region Properties

    protected val _context: Context? = context

    protected val _inputName: CharSequence? = inputName
    protected open val inputName: CharSequence
        get() = _inputName ?: context.getString(R.string.validatinator_the_value)

    protected val _invalidMessage: CharSequence? = invalidMessage
    protected open val invalidMessage: CharSequence?
        get() = _invalidMessage ?: context.getString(R.string.validatinator_invalid, inputName)

    protected val _validMessage: CharSequence? = validMessage
    protected open val validMessage: CharSequence?
        get() = _validMessage ?: context.getString(R.string.validatinator_valid, inputName)

    protected open val context: Context
        get() = _context ?: throw IllegalStateException(
            Validatinator::class.java.simpleName +
                    " requires a context in order to create localized messages"
        )

    // endregion Properties

    // region Inherited methods

    override fun toString(): String {
        return javaClass.simpleName + '@' + Integer.toHexString(hashCode())
    }

    // endregion Inherited methods

    // region Methods

    fun validate(input: T, options: Options = Options()): Boolean {
        val valid = isValid(input, options)

        val message: CharSequence? = when {
            !options.requestMessage && !options.requestTrace -> null
            valid -> validMessage
            else -> invalidMessage
        }

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

    protected open fun onInvalid(input: T, options: Validatinator.Options) { /* No op */
    }

    protected open fun onValid(input: T, options: Validatinator.Options) { /* No op */
    }

    protected abstract fun isValid(input: T, options: Options): Boolean

    // endregion Methods

    // region Companion object

    companion object {

        @JvmStatic
        protected fun Options.ensureTrace(): ArrayList<ValidatinatorTraceElement> =
            outTrace ?: ArrayList<ValidatinatorTraceElement>().apply {
                outTrace = this
            }

    }

    // endregion Companion object

    // region Nested/inner classes

    data class ValidatinatorTraceElement(

        val validatinator: Validatinator<*>,

        val valid: Boolean,

        val message: CharSequence?

    )

    class Options {

        var requestMessage: Boolean = false

        var requestTrace: Boolean = false

        var outMessage: CharSequence? = null

        var outTrace: ArrayList<ValidatinatorTraceElement>? = null

        fun clear(): Options = apply {
            outMessage = null
            outTrace = null
        }

        fun copy(): Options = Options().also {
            it.requestMessage = requestMessage
            it.requestTrace = requestTrace
        }

    }

    // endregion Nested/inner classes

}
