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
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.strategies.utils.MovingWindow
import org.roboquant.strategies.utils.PercentageMovingWindow

/**
 * Base class for strategies that are interested in historic prices or returns.
 *
 * @property period
 * @property priceType
 * @property useReturns
 * @constructor Create empty Historic price strategy
 */
abstract class HistoricPriceStrategy(
    private val period: Int,
    private val priceType: String = "DEFAULT",
    private val useReturns: Boolean = false
) : RecordingStrategy() {

    private val history = mutableMapOf<Asset, MovingWindow>()

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        for ((asset, action) in event.prices) {
            val movingWindow =
                history.getOrPut(asset) { if (useReturns) PercentageMovingWindow(period) else MovingWindow(period) }
            movingWindow.add(action.getPrice(priceType))
            if (movingWindow.isAvailable()) {
                val data = movingWindow.toDoubleArray()
                assert(data.size == period)
                val signal = generate(asset, movingWindow.toDoubleArray())
                result.addNotNull(signal)
            }
        }
        return result
    }

    /**
     * Generate a signal based on the provided signal and asset
     *
     * @param asset
     * @param data
     * @return
     */
    abstract fun generate(asset: Asset, data: DoubleArray): Signal?


    override fun reset() {
        super.reset()
        history.clear()
    }
}