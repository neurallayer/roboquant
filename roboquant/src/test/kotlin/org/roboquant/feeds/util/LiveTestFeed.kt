/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.feeds.util

import kotlinx.coroutines.delay
import org.roboquant.common.Asset
import org.roboquant.common.Event
import org.roboquant.common.Item
import org.roboquant.common.PriceBar
import org.roboquant.common.Stock
import org.roboquant.common.TradePrice
import org.roboquant.feeds.*
import java.time.Instant
import kotlin.random.Random

/**
 * Feed that will generate events for a series of prices using the system time. It can be used to validate if a
 * strategy is behaving as expected given a known set of prices.
 *
 * @property prices the prices to use, expressed as one or more int progressions
 * @property asset
 * @property delayInMillis How much delay between two events, default is 1000ms
 * @constructor Create a new Test feed
 */
class LiveTestFeed(
    private vararg val prices: Iterable<Number> = arrayOf(90..100, 100 downTo 90),
    private val asset: Asset = Stock("TEST"),
    private val delayInMillis: Int = 1000,
    private val priceBar: Boolean = false,
    private val volume: Double = 1000.0
) : LiveFeed() {

    init {
        require(delayInMillis > 0) { "delayInMillis should be larger than zero" }
        require(prices.isNotEmpty()) { "prices cannot be empty" }
    }

    /**
     * utility methods to create instances
     */
    companion object {

        /**
         * Create a LiveTestFeed based on random data.
         */
        fun random(startPrice: Double, size: Int, symbol: String = "XYZ", delayInMillis: Int = 1000): LiveTestFeed {
            var prev = startPrice
            val randomPrices = (1..size).map {
                val next = (prev + Random.Default.nextDouble() - 0.5).coerceAtLeast(0.01)
                prev = next
                next
            }
            return LiveTestFeed(randomPrices, asset = Stock(symbol), delayInMillis = delayInMillis)
        }

    }

    private fun getAction(price: Double): Item {
        return if (priceBar) {
            PriceBar(asset, price, price * 1.001, price * 0.999, price, volume)
        } else {
            TradePrice(asset, price, volume)
        }
    }

    /**
     * See [Feed.play]
     *
     * @param channel
     */
    override suspend fun play(channel: EventChannel) {
        for (intRange in prices) {
            for (price in intRange) {
                val action = getAction(price.toDouble())
                val event = Event(Instant.now(), listOf(action))
                channel.send(event)
                delay(delayInMillis.toLong())
            }
        }
    }

}
