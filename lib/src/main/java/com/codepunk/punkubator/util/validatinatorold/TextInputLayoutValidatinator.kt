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
import com.google.android.material.textfield.TextInputLayout

class TextInputLayoutValidatinator protected constructor(
    context: Context?,
    getInputName: (context: Context?, input: TextInputLayout) -> CharSequence?,
    getInvalidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    getValidMessage: (context: Context?, inputName: CharSequence?) -> CharSequence?,
    protected val wrapped: Validatinator<CharSequence?>
) : Validatinator<TextInputLayout>(context, getInputName, getInvalidMessage, getValidMessage) {

    override fun onInvalid(input: TextInputLayout, options: Options) {
        input.error = options.outMessage
        input.requestFocus()
    }

    override fun onValid(input: TextInputLayout, options: Options) {
        input.error = null
    }

    override fun isValid(input: TextInputLayout, options: Options): Boolean {
        return wrapped.validate(input.editText?.text, options)
    }

    class Builder(

        protected val wrapped: Validatinator<CharSequence?>

    ) : AbsBuilder<TextInputLayout, TextInputLayoutValidatinator, Builder>() {

        override val thisBuilder: Builder = this

        override fun build(): TextInputLayoutValidatinator = TextInputLayoutValidatinator(
            context,
            getInputName,
            getInvalidMessage,
            getValidMessage,
            wrapped
        )

    }
}
