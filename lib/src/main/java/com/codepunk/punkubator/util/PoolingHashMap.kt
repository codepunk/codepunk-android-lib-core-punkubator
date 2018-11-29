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

import androidx.core.util.Pools

open class PoolingHashMap<K, V> : HashMap<K, V> {

    // region Properties

    protected val valuePool: Pools.SimplePool<V>

    // endregion Properties

    // region Constructors

    constructor(initialCapacity: Int, loadFactor: Float, maxPoolSize: Int) : super(
        initialCapacity,
        loadFactor
    ) {
        valuePool = Pools.SimplePool(maxPoolSize)
    }

    constructor(initialCapacity: Int, maxPoolSize: Int) : super(initialCapacity) {
        valuePool = Pools.SimplePool(maxPoolSize)
    }

    constructor(maxPoolSize: Int) : super() {
        valuePool = Pools.SimplePool(maxPoolSize)
    }

    constructor(m: MutableMap<out K, out V>, maxPoolSize: Int) : super(m) {
        valuePool = Pools.SimplePool(maxPoolSize)
    }

    // endregion Constructors

    // region Methods

    fun obtain(key: K, newInstance: () -> V): V = get(key)
        ?: (valuePool.acquire()?.apply {
            onAcquire(key, this)
        } ?: newInstance().apply {
            onNewInstance(key, this)
        }).apply {
            put(key, this)
        }

    fun recycle(key: K) {
        remove(key)?.also {
            if (valuePool.release(it)) {
                onRelease(key, it)
            }
        }
    }

    open fun onAcquire(key: K, value: V) {
        // No op
    }

    open fun onNewInstance(key: K, value: V) {
        // No op
    }

    open fun onRelease(key: K, value: V) {
        // No op
    }

    // endregion Methods

}
