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

abstract class MatchValidatinator<T, M>(
    context: Context? = null,
    inputName: CharSequence? = null,
    invalidMessage: CharSequence? = null,
    validMessage: CharSequence? = null,
    mismatchMessage: CharSequence? = null
) : Validatinator<T>(context, inputName, invalidMessage, validMessage) {

    // region Properties

    protected val _mismatchMessage: CharSequence? = mismatchMessage
    protected open val mismatchMessage: CharSequence?
        get() = _mismatchMessage ?: context.getString(
            R.string.validatinator_invalid_match,
            inputName
        )

    // endregion Properties

    // region Methods

    // region Methods

    fun validate(input: T, other: M, options: Options = Options()): Boolean {
        val valid = isValid(input, options)

        val matches = valid && matches(input, other, options)

        val message: CharSequence? = when {
            !options.requestMessage && !options.requestTrace -> null
            !valid -> invalidMessage
            !matches -> mismatchMessage
            else -> validMessage
        }

        if (options.requestMessage) {
            // If not valid, use the resulting outMessage
            if (valid || options.outMessage == null) {
                options.outMessage = message
            }
        }

        /*
        if (options.requestMessage && (valid || options.outMessage == null)) {
            options.outMessage = message
        }
        */

        if (options.requestTrace) {
            options.ensureTrace().add(ValidatinatorTraceElement(this, matches, message))
        }

        when (matches) {
            true -> onValid(input, options)
            false -> onInvalid(input, options)
        }

        return matches
    }

    protected abstract fun matches(input: T, other: M, options: Options): Boolean

    // endregion Methods

}
