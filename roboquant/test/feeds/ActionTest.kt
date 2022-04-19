/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.feeds

import org.junit.jupiter.api.Test
import kotlin.test.*
import org.roboquant.TestData

internal class ActionTest {

    @Test
    fun corporateAction() {
        val asset = TestData.euStock()
        val action = CorporateAction(asset, "SPLIT", 2.0)
        assertEquals("SPLIT", action.type)
    }

    @Test
    fun priceBar() {
        val asset = TestData.euStock()
        val action = PriceBar(asset, 10, 12, 8, 11, 1000)
        assertEquals(10.0, action.getPrice("OPEN"))
        assertEquals(12.0, action.getPrice("HIGH"))
        assertEquals(8.0, action.getPrice("LOW"))
        assertEquals(11.0, action.getPrice("CLOSE"))
        assertEquals(1000.0, action.volume)
    }

    @Test
    fun priceAction() {
        val asset = TestData.euStock()
        val pb = PriceBar.fromAdjustedClose(asset, 2, 1, 1, 1, 0.5, 100)
        assertEquals(1.0, pb.open)
        assertEquals(200.0, pb.volume)

        val values = pb.values
        val pb2 = PriceBar.fromValues(pb.asset, values)
        assertEquals(pb.asset, pb2.asset)
        assertEquals(pb.values, pb2.values)
    }

    @Test
    fun orderbook() {
        val asset = TestData.euStock()
        val action = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(100.0, 10.0), OrderBook.OrderBookEntry(100.0, 10.0)),
            listOf(OrderBook.OrderBookEntry(100.0, 9.0), OrderBook.OrderBookEntry(100.0, 9.0))
        )

        val values = action.values
        val action2 = OrderBook.fromValues(action.asset, values)
        assertEquals(action, action2)
    }

    @Test
    fun newsAction() {
        val item = NewsAction.NewsItem("Some text", mapOf("source" to "TWITTER"))
        val action = NewsAction(listOf(item))
        assertEquals(1, action.items.size)
        assertEquals("Some text", action.items.first().content)
        assertEquals(1, action.items[0].meta.size)
        assertFalse(action.items.isEmpty())
    }
}