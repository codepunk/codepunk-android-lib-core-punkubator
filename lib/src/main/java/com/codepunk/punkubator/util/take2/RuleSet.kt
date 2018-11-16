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

import com.codepunk.punkubator.util.take2.Rule.Result
import java.util.Collections

class RuleSet<T> :
    LinkedHashSet<Rule<T>>,
    Rule<T> {

    val behavior: Behavior

    constructor(
        initialCapacity: Int,
        loadFactor: Float,
        behavior: Behavior = Behavior.ALL_STOP_WHEN_INVALID
    ) : super(initialCapacity, loadFactor) {
        this.behavior = behavior
    }

    constructor(initialCapacity: Int, behavior: Behavior = Behavior.ALL_STOP_WHEN_INVALID) : super(
        initialCapacity
    ) {
        this.behavior = behavior
    }

    constructor(behavior: Behavior = Behavior.ALL_STOP_WHEN_INVALID) : super() {
        this.behavior = behavior
    }

    constructor(
        c: MutableCollection<out Rule<T>>?,
        behavior: Behavior = Behavior.ALL_STOP_WHEN_INVALID
    ) : super(c) {
        this.behavior = behavior
    }

    override fun validate(input: T): Result<T> {
        var anyValid = false
        var allValid = true
        val messages = ArrayList<CharSequence>()
        for (rule in this) {
            val result = rule.validate(input)
            messages.addAll(result.messages)
            if (result.valid) {
                anyValid = true
                if (behavior == Behavior.ANY) {
                    break
                }
            } else {
                allValid = false
                if (behavior == Behavior.ALL_STOP_WHEN_INVALID) {
                    break
                }
            }
        }
        return Result(
            input,
            if (behavior == Behavior.ANY) anyValid else allValid,
            Collections.unmodifiableList(messages)
        )
    }

    enum class Behavior {
        ANY,
        ALL,
        ALL_STOP_WHEN_INVALID
    }

}
