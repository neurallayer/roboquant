/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ml

import org.roboquant.feeds.Event

class ReturnsFeature(private val f: Feature, private val n: Int = 1, private val missing: Double = 0.0) :
    Feature by f {

    private val history = mutableListOf<Double>()

    override fun calculate(event: Event): Double {
        if (history.size > n) history.removeFirst()
        val v = f.calculate(event)
        history.add(v)
        val lastIndex = history.lastIndex
        if (lastIndex < n) return missing
        return history[lastIndex] / history[lastIndex - n] - 1.0
    }

    override val name: String
        get() = f.name + "-RETURNS"

    override fun reset() {
        history.clear()
        f.reset()
    }

}


fun Feature.returns(n: Int = 1) = ReturnsFeature(this, n)

