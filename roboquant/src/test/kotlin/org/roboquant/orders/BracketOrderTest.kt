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

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.Size
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
    fun predefined() {
        val asset = TestData.usStock()
        val size = Size(10)
        val order = BracketOrder.marketTrailStop(asset, size, 100.0, 0.05, 0.01)
        assertTrue(order.entry is MarketOrder)
        assertEquals(100.0 * 0.99, (order.stopLoss as StopOrder).stop)
        assertEquals(0.05, (order.takeProfit as TrailOrder).trailPercentage)
    }

}