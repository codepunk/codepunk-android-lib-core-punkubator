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

interface Validatinator<T> {

    fun validate(input: T, listener: ValidatinatorListener<T>? = null): Boolean

    interface ValidatinatorListener<T> {
        fun onValid(validatinator: Validatinator<T>, input: T)
        fun onInvalid(validatinator: Validatinator<T>, input: T, message: CharSequence?)
    }

}
