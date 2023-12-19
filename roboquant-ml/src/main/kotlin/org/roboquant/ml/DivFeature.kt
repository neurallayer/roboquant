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

/**
 * Create a new feature based on the division of two other features.
 */
class DivFeature(private val numerator: SingleValueFeature, private val denominator: SingleValueFeature) : SingleValueFeature() {

    /**
     * @see Feature.calculate
     */
    override fun calculateValue(event: Event): Double {
        return numerator.calculateValue(event) / denominator.calculateValue(event)
    }

    /**
     * Returns the name of this feature
     */
    override val name: String
        get() = numerator.name + "-DIV-" + denominator.name

    /**
     * reset the underlying [numerator] and [denominator] features
     */
    override fun reset() {
        numerator.reset()
        denominator.reset()
    }
}

/**
 * Divide two features and return this new feature.
 *
 *  ```
 *  val feature = PriceFeature(asset, "OPEN") / PriceFeature(asset, "CLOSE")
 *  ```
 * @see DivFeature
 */
operator fun SingleValueFeature.div(denominator: SingleValueFeature) : Feature = DivFeature(this, denominator)
