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
import com.codepunk.punkubator.util.Validatinator.ValidatinatorListener

abstract class AbsValidatinator<T>(
    val getMessage: (input: T) -> CharSequence?
) : Validatinator<T> {

    constructor(message: CharSequence?) : this({ message })

    constructor(
        context: Context,
        valueName: CharSequence = context.getString(R.string.validatinator_value_name),
        getMessage: (input: T) -> CharSequence? = {
            context.getString(R.string.validatinator_invalid, valueName)
        }
    ) : this(getMessage)

    override fun validate(input: T, listener: ValidatinatorListener<T>?): Boolean =
        isValid(input).also { valid ->
            if (valid) listener?.onValid(this, input)
            else listener?.onInvalid(this, input, getMessage(input))
        }

    abstract fun isValid(input: T): Boolean

}
