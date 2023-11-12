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

import org.roboquant.common.Asset
import org.roboquant.feeds.Event

/**
 * Extract a historic price from the event for the provided [asset]
 *
 * @param asset the asset to use
 * @param past how many events in the past should be used
 * @param type the type of price to use, default is "DEFAULT"
 * @param name the name of the feature
 */
class HistoricPriceFeature(
    private val asset: Asset,
    private val past: Int = 1,
    private val type: String = "DEFAULT",
    override val name: String = "${asset.symbol}-HISTORIC-PRICE-$past-$type"
) : Feature {

    private val hist = mutableListOf<Double>()

    /**
     * @see Feature.calculate
     */
    override fun calculate(event: Event): Double {
        val action = event.prices[asset]
        hist.add(action?.getPrice(type) ?: Double.NaN)
        return if (hist.size > past) hist.removeFirst() else Double.NaN
    }

    override fun reset() {
        hist.clear()
    }

}
