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

package com.codepunk.punkubator.util.take2

import android.content.Context
import androidx.annotation.StringRes
import com.codepunk.punkubator.R
import com.codepunk.punkubator.util.take2.Rule.Result
import java.util.*

abstract class AbsRule<T>(

    val message: (input: T) -> CharSequence

) : Rule<T> {

    constructor(message: CharSequence) : this({ message })

    constructor(context: Context, inputName: CharSequence? = null) : this(
        context,
        inputName,
        R.string.validatinator_invalid
    )

    constructor(
        context: Context,
        inputName: CharSequence? = null,
        @StringRes resId: Int
    ) : this({ input -> context.getString(resId, inputName ?: "'$input'") })

    override fun validate(input: T): Result<T> =
        isValid(input).let { valid ->
            Result(
                input,
                valid,
                Collections.unmodifiableList(
                    when (valid) {
                        true -> emptyList()
                        false -> Collections.singletonList(message(input))
                    }
                )
            )
        }

    abstract fun isValid(input: T): Boolean

}
