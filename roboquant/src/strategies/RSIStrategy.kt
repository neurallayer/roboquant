/*
 * Copyright 2022 Neural Layer
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
import org.roboquant.strategies.utils.RSI
import java.time.Instant

/**
 * Strategy using the Relative Strength Index of an asset to generate signals. RSI measures the magnitude of recent
 * price changes to evaluate overbought or oversold conditions in the price of a stock or other asset.
 *
 * If the RSI raise above the configured high threshold (default 70), a sell signal will be generated. And if the RSI
 * falls below the configured low threshold (default 30), a buy signal will be generated.
 *
 * @see RSI
 *
 * @property lowThreshold
 * @property highThreshold
 * @constructor Create a new RSI Strategy
 *
 */
class RSIStrategy(
    val lowThreshold: Double = 30.0,
    val highThreshold: Double = 70.0,
) : PriceStrategy(prefix = "rsi.") {

    private val calculators = mutableMapOf<Asset, RSI>()

    /**
     * Subclasses need to implement this method and return optional a signal.
     *
     * @param asset
     * @param price
     * @param time
     * @return
     */
    override fun generate(asset: Asset, price: Double, time: Instant): Signal? {
        var result: Signal? = null

        val rsi = calculators[asset]
        if (rsi == null) {
            calculators[asset] = RSI(price)
        } else {
            rsi.add(price)
            if (rsi.isReady()) {
                val value = rsi.calculate()
                record(asset.symbol, value)
                if (value > highThreshold)
                    result = Signal(asset, Rating.SELL)
                else if (value < lowThreshold)
                    result = Signal(asset, Rating.BUY)
            }
        }
        return result
    }

    override fun reset() {
        calculators.clear()
    }
}