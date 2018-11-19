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

class LengthRangeValidatinator protected constructor(
    validMessage: (input: CharSequence?) -> CharSequence?,
    invalidMessage: (input: CharSequence?) -> CharSequence?,
    protected val minLength: Int = 0,
    protected val maxLength: Int = Integer.MAX_VALUE
) : SimpleValidatinator<CharSequence?>(validMessage, invalidMessage) {

    override fun isValid(input: CharSequence?): Boolean =
        input != null && input.length in minLength..maxLength

    class Builder(

        _minLength: Int = 0,
        _maxLength: Int = Integer.MAX_VALUE

    ) : AbsBuilder<CharSequence?, LengthRangeValidatinator, Builder>() {

        val minLength: Int
        val maxLength: Int

        override val thisBuilder: Builder = this

        init {
            val adjustedMin = Math.max(_minLength, 0)
            val adjustedMax = Math.max(_maxLength, 0)
            minLength = Math.min(adjustedMin, adjustedMax)
            maxLength = Math.max(adjustedMin, adjustedMax)
        }

        override fun getInvalidMessage(
            context: Context?,
            inputName: CharSequence?,
            input: CharSequence?
        ): CharSequence? = context?.run {
            when (minLength) {
                maxLength -> resources.getQuantityString(
                    R.plurals.validatinator_invalid_range,
                    minLength,
                    inputName ?: "\"$input\"",
                    minLength
                )
                else -> getString(
                    R.string.validatinator_invalid_range,
                    inputName,
                    minLength,
                    maxLength
                )
            }
        }

        override fun build(): LengthRangeValidatinator =
            LengthRangeValidatinator(
                validMessage,
                invalidMessage,
                minLength,
                maxLength
            )

    }

}
