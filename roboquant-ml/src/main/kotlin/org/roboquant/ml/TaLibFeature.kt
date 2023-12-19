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
import org.roboquant.feeds.PriceBar
import org.roboquant.ta.InsufficientData
import org.roboquant.ta.PriceBarSeries
import org.roboquant.ta.TaLib

/**
 * Use any TaLib indicators to create features
 */
class TaLibFeature(
    override val name: String,
    private val asset: Asset,
    private val missing: Double = Double.NaN,
    private val block: TaLib.(prices: PriceBarSeries) -> Double
) : SingleValueFeature() {

    private val taLib = TaLib()
    private val history = PriceBarSeries(1)

    /**
     * Common
     */
    companion object {

        /**
         * RSI feature
         */
        fun rsi(asset: Asset, timePeriod: Int = 14): TaLibFeature {
            return TaLibFeature("${asset.symbol}-RSI-$timePeriod", asset) {
                rsi(it, timePeriod)
            }
        }

        /**
         * OBV feature
         */
        fun obv(asset: Asset) : TaLibFeature {
            return TaLibFeature("${asset.symbol}-OBV", asset) {
                obv(it.close, it.volume)
            }
        }

        /**
         * EMA feature
         */
        fun ema(asset: Asset, fast: Int = 5, slow: Int = 13) : TaLibFeature {
            return TaLibFeature("${asset.symbol}-EMA-$fast-$slow", asset) {
                val f = ema(it, fast)
                val s = ema(it, slow)
                when {
                    f > s -> 1.0
                    s < f -> -1.0
                    else -> 0.0
                }
            }
        }

    }

    /**
     * @see Feature.calculate
     */
    override fun calculateValue(event: Event): Double {
        val action = event.prices[asset]
        if (action != null && action is PriceBar && history.add(action, event.time)) {
            try {
                return taLib.block(history)
            } catch (e: InsufficientData) {
                history.increaseCapacity(e.minSize)
            }
        }
        return missing
    }

    /**
     * @see Feature.reset
     */
    override fun reset() {
        history.clear()
    }



}
