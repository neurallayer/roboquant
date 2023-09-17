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

package org.roboquant.orders

import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import org.roboquant.TestData
import org.roboquant.common.Size
import org.roboquant.common.percent
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BracketOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val size = Size(10)
        val order = BracketOrder(
            MarketOrder(asset, size),
            LimitOrder(asset, -size, 101.0),
            StopOrder(asset, -size, 99.0),
        )

        assertTrue(order.entry is MarketOrder)
        assertTrue(order.stopLoss is StopOrder)
        assertTrue(order.toString().isNotBlank())
    }

    @Test
    fun fail() {
        val asset = TestData.usStock()
        val asset2 = TestData.euStock()
        val size = Size(10)

        assertThrows<IllegalArgumentException> {
            BracketOrder(
                MarketOrder(asset, size),
                LimitOrder(asset2, -size, 101.0),
                StopOrder(asset, -size, 99.0),
            )
        }

        assertThrows<IllegalArgumentException> {
            BracketOrder(
                MarketOrder(asset, size),
                LimitOrder(asset2, Size(-11), 101.0),
                StopOrder(asset, -size, 99.0),
            )
        }

    }

    @Test
    fun predefined() {
        val asset = TestData.usStock()
        val size = Size(10)
        val order = BracketOrder.marketTrailStop(asset, size, 100.0, 0.05, 0.01)
        assertTrue(order.entry is MarketOrder)
        // assertTrue(order.stopLoss is StopOrder)
        // assertTrue(order.takeProfit is TrailOrder)
        assertEquals(100.0 * 0.99, (order.stopLoss as StopOrder).stop)
        assertEquals(5.percent, (order.takeProfit as TrailOrder).trailPercentage)

        val order2 = BracketOrder.limitTrailStop(asset, size, 100.0, 0.05, 0.01)
        // assertTrue(order2.entry is LimitOrder)
        assertEquals(100.0, (order2.entry as LimitOrder).limit)
        assertTrue(order2.stopLoss is StopOrder)
        assertTrue(order2.takeProfit is TrailOrder)
    }

}
