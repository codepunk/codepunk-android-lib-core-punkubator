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

import android.content.Context
import com.codepunk.punkubator.R

class MinLengthValidatinator : AbsValidatinator<CharSequence?> {

    val min: Int

    constructor(
        min: Int = 0,
        getMessage: (input: CharSequence?) -> CharSequence?
    ) : super(getMessage) {
        this.min = Math.max(min, 0)
    }

    constructor(min: Int = 0, message: CharSequence?) : super(message) {
        this.min = Math.max(min, 0)
    }

    constructor(
        min: Int = 0,
        context: Context,
        valueName: CharSequence = context.getString(R.string.validatinator_value_name),
        getMessage: (input: CharSequence?) -> CharSequence? = {
            val tMin = Math.max(min, 0)
            context.getString(R.string.validatinator_invalid_min, valueName, tMin)
        }
    ) : super(context, valueName, getMessage) {
        this.min = Math.max(min, 0)
    }

    override fun isValid(input: CharSequence?): Boolean =
        input != null && input.length >= min

}
