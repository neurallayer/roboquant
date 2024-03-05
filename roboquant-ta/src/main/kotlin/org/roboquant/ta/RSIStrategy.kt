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

package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.PriceSeries
import org.roboquant.common.addAll
import org.roboquant.feeds.Event
import org.roboquant.strategies.RecordingStrategy
import org.roboquant.strategies.Signal

/**
 * Strategy using the Relative Strength MetadataProvider of an asset to generate signals. RSI measures the magnitude
 * of recent price changes to evaluate overbought or oversold conditions in the price of an asset.
 *
 * If the RSI raises above the configured high threshold (default 70), a sell signal will be generated. And if the RSI
 * falls below the configured low threshold (default 30), a buy signal will be generated.
 *
 * @property lowThreshold
 * @property highThreshold
 * @constructor Create a new RSI Strategy
 *
 */
class RSIStrategy(
    val lowThreshold: Double = 30.0,
    val highThreshold: Double = 70.0,
    private val windowSize: Int = 14,
    private val priceType: String = "DEFAULT"
) : RecordingStrategy(prefix = "rsi.") {

    private val history = mutableMapOf<Asset, PriceSeries>()
    private val taLib = TaLib()

    /**
     * Reset the history
     */
    override fun reset() {
        history.clear()
    }

    /**
     * @see RecordingStrategy.generate
     */
    override fun generate(event: Event): List<Signal> {
        history.addAll(event, 1, priceType)
        val result = mutableListOf<Signal>()
        for (asset in event.prices.keys) {
            val data = history.getValue(asset)
            try {
                if (data.isFull()) {
                    val rsi = taLib.rsi(data.toDoubleArray(), windowSize)
                    record(asset.symbol, rsi)
                    if (rsi > highThreshold)
                        result.add(Signal.sell(asset))
                    else if (rsi < lowThreshold)
                        result.add(Signal.buy(asset))
                }
            } catch (ex: InsufficientData) {
                data.increaseCapacity(ex.minSize)
            }
        }
        return result
    }
}
