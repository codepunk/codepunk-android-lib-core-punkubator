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

open class LengthRangeValidatinator(
    context: Context? = null,
    inputName: CharSequence? = null,
    invalidMessage: CharSequence? = null,
    validMessage: CharSequence? = null,
    minLength: Int = 0,
    maxLength: Int = Integer.MAX_VALUE
) : Validatinator<CharSequence?>(context, inputName, invalidMessage, validMessage) {

    // region Inherited properties

    override val invalidMessage: CharSequence?
        get() = _invalidMessage ?: when (minLength) {
            maxLength -> context.resources.getQuantityString(
                R.plurals.validatinator_invalid_range,
                minLength,
                inputName,
                minLength
            )
            else -> context.getString(
                R.string.validatinator_invalid_range,
                inputName,
                minLength,
                maxLength
            )
        }

    // endregion Inherited properties

    // region Properties

    protected val minLength: Int = Math.min(Math.max(minLength, 0), Math.max(maxLength, 0))

    protected val maxLength: Int = Math.max(Math.max(minLength, 0), Math.max(maxLength, 0))

    // endregion Properties

    // region Constructors

    constructor(
        context: Context? = null,
        inputName: CharSequence? = null,
        minLength: Int,
        maxLength: Int
    ) : this(context, inputName, null, null, minLength, maxLength)

    // endregion Constructors

    // region Inherited methods

    override fun isValid(input: CharSequence?, options: Options): Boolean =
        input != null && input.length <= maxLength

    // endregion Inherited methods

}
