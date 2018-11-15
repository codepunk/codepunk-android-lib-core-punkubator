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

package com.codepunk.punkubator.util

import android.content.Context
import com.codepunk.punkubator.R

class MinMaxLengthValidatinator : AbsValidatinator<CharSequence?> {

    val min: Int
    val max: Int

    constructor(min: Int, max: Int, getMessage: (input: CharSequence?) -> CharSequence?) : super(
        getMessage
    ) {
        this.min = Math.max(Math.min(min, max), 0)
        this.max = Math.max(Math.max(min, max), 0)
    }

    constructor(min: Int, max: Int, message: CharSequence?) : super(message) {
        this.min = Math.max(Math.min(min, max), 0)
        this.max = Math.max(Math.max(min, max), 0)
    }

    constructor(
        min: Int,
        max: Int,
        context: Context,
        valueName: CharSequence = context.getString(R.string.validatinator_value_name),
        getMessage: (input: CharSequence?) -> CharSequence? = {
            val tMin = Math.max(Math.min(min, max), 0)
            val tMax = Math.max(Math.max(min, max), 0)
            when (tMin) {
                tMax -> context.resources.getQuantityString(
                    R.plurals.validatinator_invalid_min_max,
                    tMin,
                    valueName,
                    tMin
                )
                else -> context.getString(
                    R.string.validatinator_invalid_min_max,
                    valueName,
                    tMin,
                    tMax
                )
            }
        }
    ) : super(context, valueName, getMessage) {
        this.min = Math.max(Math.min(min, max), 0)
        this.max = Math.max(Math.max(min, max), 0)
    }

    override fun isValid(input: CharSequence?): Boolean = input?.length in min..max
}
