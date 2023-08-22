/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant

import org.roboquant.common.Asset
import org.roboquant.common.days
import org.roboquant.common.plus
import org.roboquant.feeds.Event
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.util.HistoricTestFeed
import java.time.Instant

/**
 * Test data used in unit tests
 */
internal object TestData {

    private fun usStock() = Asset("XYZ")

    fun feed(): HistoricFeed {
        return HistoricTestFeed(90..110, 110 downTo 80, 80..125, priceBar = true, asset = usStock())
    }

    private fun priceAction(asset: Asset = usStock()) = TradePrice(asset, 10.0)

    private fun priceBar(asset: Asset = usStock()) = PriceBar(asset, 10.0, 11.0, 9.0, 10.0, 1000.0)

    private fun time(): Instant = Instant.parse("2020-01-03T12:00:00Z")

    fun event(time: Instant = time()) = Event(listOf(priceAction()), time)

    fun event2(time: Instant = time()) = Event(listOf(priceBar()), time)

    fun events(n: Int = 100, asset: Asset = usStock()): List<Event> {
        val start = time()
        val result = mutableListOf<Event>()
        repeat(n) {
            val action = TradePrice(asset, it + 100.0)
            val event = Event(listOf(action), start + it.days)
            result.add(event)
        }
        return result
    }

    val feed = RandomWalkFeed.lastYears(1, 2)

}
