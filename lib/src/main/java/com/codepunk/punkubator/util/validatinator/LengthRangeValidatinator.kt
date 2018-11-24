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

open class LengthRangeValidatinator protected constructor(
    context: Context?,
    getInputName: (context: Context?, input: CharSequence?) -> CharSequence?,
    getInvalidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    getValidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    protected val minLength: Int,
    protected val maxLength: Int
) : Validatinator<CharSequence?>(context, getInputName, getInvalidMessage, getValidMessage) {

    override fun isValid(input: CharSequence?, options: Options): Boolean =
        input != null && input.length in minLength..maxLength

    open class Builder(
        minLength: Int = 0,
        maxLength: Int = Int.MAX_VALUE
    ) : AbsBuilder<CharSequence?, LengthRangeValidatinator, Builder>() {

        protected val minLength = Math.min(Math.max(minLength, 0), Math.max(maxLength, 0))

        protected val maxLength = Math.max(Math.max(minLength, 0), Math.max(maxLength, 0))

        override val thisBuilder: Builder by lazy { this }

        override var getInvalidMessage: (
            context: Context?,
            inputName: CharSequence?
        ) -> CharSequence? = { context, inputName ->
            context?.run {
                when (minLength) {
                    maxLength -> resources.getQuantityString(
                        R.plurals.validatinator_invalid_range,
                        minLength,
                        inputName,
                        minLength
                    )
                    else -> getString(
                        R.string.validatinator_invalid_range,
                        inputName,
                        minLength,
                        maxLength
                    )
                }
            } ?: throw missingContextException("getInvalidMessage")
        }

        override fun build(): LengthRangeValidatinator =
            LengthRangeValidatinator(
                context,
                getInputName,
                getInvalidMessage,
                getValidMessage,
                minLength,
                maxLength
            )
    }
}
