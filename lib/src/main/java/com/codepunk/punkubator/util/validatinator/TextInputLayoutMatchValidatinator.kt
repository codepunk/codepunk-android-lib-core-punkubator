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
import android.text.TextUtils
import com.google.android.material.textfield.TextInputLayout

open class TextInputLayoutMatchValidatinator(
    context: Context? = null,
    inputName: CharSequence? = null,
    invalidMessage: CharSequence? = null,
    validMessage: CharSequence? = null,
    protected val validatinator: Validatinator<CharSequence?>
) : MatchValidatinator<TextInputLayout, CharSequence?>(
    context,
    inputName,
    invalidMessage,
    validMessage
) {

    // region Constructors

    constructor(
        context: Context? = null,
        inputName: CharSequence? = null,
        validatinator: Validatinator<CharSequence?>
    ) : this(context, inputName, null, null, validatinator)

    // endregion Constructors

    // region Inherited methods

    override fun isValid(input: TextInputLayout, options: Options): Boolean =
        validatinator.validate(input.editText?.text, options)

    override fun matches(input: TextInputLayout, other: CharSequence?, options: Options): Boolean =
        TextUtils.equals(input.editText?.text, other)

    override fun onInvalid(input: TextInputLayout, options: Options) {
        super.onInvalid(input, options)
        input.error = options.outMessage
        input.requestFocus()
    }

    override fun onValid(input: TextInputLayout, options: Options) {
        super.onValid(input, options)
        input.error = null
    }

    // endregion Inherited methods

}
