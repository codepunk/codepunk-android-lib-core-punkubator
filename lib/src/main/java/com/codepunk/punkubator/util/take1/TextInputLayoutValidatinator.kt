/*
 * Copyright (C) 2018 Codepunk, LLC
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

package com.codepunk.punkubator.util.take1

import com.google.android.material.textfield.TextInputLayout
import com.codepunk.punkubator.util.take1.Validatinator.ValidatinatorListener

class TextInputLayoutValidatinator(
    private val layout: TextInputLayout,
    wrapped: Validatinator<CharSequence?>
) : ValidatinatorWrapper<TextInputLayout, CharSequence?>(wrapped) {

    override fun getWrappedInput(input: TextInputLayout): CharSequence? = layout.editText?.text

    override fun onValid(validatinator: Validatinator<TextInputLayout>, input: TextInputLayout) {
        input.error = null
    }

    override fun onInvalid(
        validatinator: Validatinator<TextInputLayout>,
        input: TextInputLayout,
        message: CharSequence?
    ) {
        input.error = message
        input.requestFocus()
    }

    fun validate(listener: ValidatinatorListener<TextInputLayout>? = null): Boolean {
        return super.validate(layout, listener)
    }

}
