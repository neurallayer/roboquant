/*
 * Copyright 2020-2024 Neural Layer
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


/**
 * A feature generates data that is derived from a series of events
 */
interface Feature {

    /**
     * Update the feature with a new event and return the latest value
     */
    fun calculate(event: Event): DoubleArray

    /**
     * The name of this feature
     */
    val name: String

    /**
     * The size of the returned DoubleArray. This should be the same size at every event.
     */
    val size: Int

    /**
     * Reset any state in the feature
     */
    fun reset() {}
}


/**
 * A feature generates a single value that is derived from a series of events
 */
abstract class SingleValueFeature: Feature {

    override val size: Int = 1

    /**
     * Update the feature with a new event and return the latest value
     */
    override fun calculate(event: Event): DoubleArray {
        return doubleArrayOf(calculateValue(event))
    }

    abstract fun calculateValue(event: Event): Double

}

