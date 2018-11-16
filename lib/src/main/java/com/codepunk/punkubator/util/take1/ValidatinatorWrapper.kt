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

import com.codepunk.punkubator.util.take1.Validatinator.ValidatinatorListener

abstract class ValidatinatorWrapper<T, W>(val wrapped: Validatinator<W>) :
    Validatinator<T>,
    ValidatinatorListener<T> {

    override fun validate(input: T, listener: ValidatinatorListener<T>?): Boolean =
        wrapped.validate(getWrappedInput(input), object : ValidatinatorListener<W> {
            val unwrappedInput: T = input

            override fun onValid(validatinator: Validatinator<W>, input: W) {
                this@ValidatinatorWrapper.onValid(this@ValidatinatorWrapper, unwrappedInput)
                listener?.onValid(this@ValidatinatorWrapper, unwrappedInput)
            }

            override fun onInvalid(
                validatinator: Validatinator<W>,
                input: W,
                message: CharSequence?
            ) {
                this@ValidatinatorWrapper.onInvalid(
                    this@ValidatinatorWrapper,
                    unwrappedInput,
                    message
                )
                listener?.onInvalid(this@ValidatinatorWrapper, unwrappedInput, message)
            }
        })

    override fun onValid(validatinator: Validatinator<T>, input: T) { // No op
    }

    override fun onInvalid(
        validatinator: Validatinator<T>,
        input: T,
        message: CharSequence?
    ) { // No op
    }

    abstract fun getWrappedInput(input: T): W
}
