/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.common.RoboquantException
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.strategies.utils.PriceSeries

/**
 * Base class for strategies that are interested in historic prices. Subclasses should override one of the
 * two following methods:
 *
 * [generateSignal] - full control over the Signal
 * [generateRating] - only need to generate a Rating
 *
 * @property period period to keep track of historic data
 * @property priceType the type of price to store, default is "DEFAULT"
 */
abstract class HistoricPriceStrategy(
    private val period: Int,
    private val priceType: String = "DEFAULT",
) : RecordingStrategy() {

    /**
     * Contain the history of all assets
     */
    private val history = mutableMapOf<Asset, PriceSeries>()

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        for ((asset, action) in event.prices) {
            val priceSeries =
                history.getOrPut(asset) { PriceSeries(period) }
            priceSeries.add(action.getPrice(priceType))
            if (priceSeries.isFilled()) {
                val data = priceSeries.toDoubleArray()
                assert(data.size == period)
                val signal = generateSignal(asset, priceSeries.toDoubleArray())
                result.addNotNull(signal)
            }
        }
        return result
    }

    /**
     * Generate a signal based on the provided [asset] and [data]. Default implementation is to call [generateRating]
     * to get the rating.
     */
    open fun generateSignal(asset: Asset, data: DoubleArray): Signal? {
        val rating = generateRating(data)
        return if (rating == null) null else Signal(asset, rating)
    }

    /**
     * Generate a [Rating] based on the provided [data]. If no rating can be provided, this method should return null.
     */
    open fun generateRating(data: DoubleArray): Rating? =
        throw RoboquantException("Should override generateSignal or generateRating")

    override fun reset() {
        super.reset()
        history.clear()
    }
}