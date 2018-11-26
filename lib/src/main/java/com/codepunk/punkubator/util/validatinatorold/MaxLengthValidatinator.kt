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

open class MaxLengthValidatinator protected constructor(
    context: Context?,
    getInputName: (context: Context?, input: CharSequence?) -> CharSequence?,
    getInvalidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    getValidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    protected val maxLength: Int
) : Validatinator<CharSequence?>(context, getInputName, getInvalidMessage, getValidMessage) {

    override fun isValid(input: CharSequence?, options: Options): Boolean =
        input != null && input.length <= maxLength

    open class Builder(maxLength: Int = 0) :
        AbsBuilder<CharSequence?, MaxLengthValidatinator, Builder>() {

        protected val maxLength: Int = Math.max(maxLength, 0)

        override var getInvalidMessage: (
            context: Context?,
            inputName: CharSequence?
        ) -> CharSequence? = { context, inputName ->
            context?.getString(
                R.string.validatinator_invalid_max,
                inputName,
                this.maxLength
            ) ?: throw missingContextException("getInvalidMessage")
        }

        override val thisBuilder: Builder by lazy { this }

        override fun build(): MaxLengthValidatinator = MaxLengthValidatinator(
            context,
            getInputName,
            getInvalidMessage,
            getValidMessage,
            maxLength
        )

    }

}
