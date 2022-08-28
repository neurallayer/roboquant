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

package org.roboquant.feeds.test

import org.roboquant.common.Asset
import org.roboquant.common.days
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import java.time.Instant
import java.time.temporal.TemporalAmount

/**
 * Feed that will generate events for a series of prices using the system time. It can be used to validate if a
 * strategy is behaving as expected given a known set of prices.
 *
 * @constructor Create new Test feed
 */
class HistoricTestFeed(
    vararg prices: Iterable<Number> = arrayOf(90..100, 100 downTo 90),
    start: Instant = Instant.parse("1970-01-01T12:00:00Z"),
    duration: TemporalAmount = 1.days,
    asset: Asset = Asset("TEST"),
    private val priceBar: Boolean = false,
    private val volume: Double = 1000.0
) : HistoricPriceFeed() {

    init {
        require(prices.isNotEmpty())
        var now = start
        for (range in prices) {
            for (price in range) {
                val action = getAction(asset, price.toDouble())
                add(now, action)
                now += duration
            }
        }
    }


    private fun getAction(asset: Asset, price: Double): PriceAction {
        return if (priceBar) {
            PriceBar(asset, price, price * 1.001, price * 0.999, price, volume)
        } else {
            TradePrice(asset, price, volume)
        }
    }

}