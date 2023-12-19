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

import org.roboquant.common.div
import org.roboquant.common.minus
import org.roboquant.feeds.Event

/**
 *
 */
class ReturnsFeature(
    private val f: Feature,
    private val n: Int = 1,
    private val missingValue: Double = Double.NaN,
    override val name: String = f.name + "-RETURNS"
) : Feature {

    private val history = mutableListOf<DoubleArray>()
    private val missing = DoubleArray(f.size) { missingValue }
    override val size = f.size

    /**
     * @see Feature.calculate
     */
    override fun calculate(event: Event): DoubleArray {
        val v = f.calculate(event)
        history.add(v)
        return if (history.size > n) {
            val first = history.removeFirst()
            history.last() / first - 1.0
        } else {
            missing
        }
    }


    override fun reset() {
        history.clear()
        f.reset()
    }

}

/**
 * Wrap the feature and calculate the returns
 */
fun SingleValueFeature.returns(n: Int = 1) = ReturnsFeature(this, n)

