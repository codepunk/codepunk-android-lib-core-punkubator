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
import com.codepunk.punkubator.util.validatinator.ValidatinatorSet.Constraint.*

open class ValidatinatorSet<T>(
    context: Context? = null,
    inputName: CharSequence? = null,
    invalidMessage: CharSequence? = null,
    validMessage: CharSequence? = null,
    protected val constraint: Constraint = ALL,
    protected val processAll: Boolean = false
) : Validatinator<T>(context, inputName, invalidMessage, validMessage) {

    // region Inherited properties

    override val invalidMessage: CharSequence?
        get() = _invalidMessage ?: context.getString(
            when (constraint) {
                ALL -> R.string.validatinator_invalid_set_all
                ANY -> R.string.validatinator_invalid_set_any
            },
            inputName
        )

    override val validMessage: CharSequence?
        get() = _validMessage ?: context.getString(
            when (constraint) {
                ALL -> R.string.validatinator_valid_set_all
                ANY -> R.string.validatinator_valid_set_any
            },
            inputName
        )

    // endregion Inherited properties

    // region Properties

    protected val validatinators = ArrayList<Validatinator<in T>>()

    // endregion Properties

    // region Constructors

    constructor(
        context: Context? = null,
        inputName: CharSequence? = null,
        constraint: Constraint,
        processAll: Boolean = false
    ) : this(context, inputName, null, null, constraint, processAll)

    // endregion Constructors

    // region Inherited methods

    override fun isValid(input: T, options: Options): Boolean {
        var valid: Boolean = when (constraint) {
            ALL -> true
            ANY -> false
        }

        var cause: Options? = null
        for (innerValidatinator in validatinators) {
            val innerOptions = options.copy()
            val innerValid = innerValidatinator.validate(input, innerOptions)
            innerOptions.outTrace?.also { innerTrace ->
                options.ensureTrace().addAll(innerTrace)
            }

            if (cause == null) {
                when {
                    innerValid && constraint == ANY -> cause = innerOptions
                    !innerValid && constraint == ALL -> cause = innerOptions
                }

                if (cause != null) {
                    valid = innerValid
                    options.outMessage = cause.outMessage
                    if (!processAll) {
                        break
                    }
                }
            }
        }

        if (options.requestMessage && options.outMessage == null) {
            options.outMessage = when (valid) {
                true -> validMessage
                false -> invalidMessage
            }
        }

        return valid
    }

    // endregion Inherited methods

    // region Methods

    fun add(vararg validatinators: Validatinator<T>): ValidatinatorSet<T> {
        this.validatinators.addAll(validatinators)
        return this
    }

    // endregion Methods

    // region Nested/inner classes

    enum class Constraint {
        ALL,
        ANY
    }

    // endregion Nested/inner classes

}
