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
import java.util.regex.Pattern

class WordCharacterValidatinator(
    context: Context? = null,
    inputName: CharSequence? = null,
    invalidMessage: CharSequence? = null,
    validMessage: CharSequence? = null
) : PatternValidatinator(
    context,
    inputName,
    invalidMessage,
    validMessage,
    Pattern.compile("\\w+")
) {

    // region Inherited properties

    override val invalidMessage: CharSequence?
        get() = _invalidMessage ?: context.getString(
            R.string.validatinator_invalid_pattern_word_character,
            inputName
        )

    // endregion Inherited properties

}
