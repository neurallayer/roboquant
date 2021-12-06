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

package org.roboquant.orders


import org.roboquant.TestData
import java.time.Instant
import kotlin.test.*

internal class SingleOrderTest {

    @Test
    fun testMarketOrder() {
        val asset = TestData.usStock()
        val mo = MarketOrder(asset, 100.0)
        assertEquals(100.0, mo.quantity)
        assertTrue(mo.tif is GTC)

        val e = mo.execute(100.0, Instant.now())
        assertTrue { e.isNotEmpty() }
    }


    @Test
    fun testStopOrder() {
        val asset = TestData.usStock()
        val order = StopOrder(asset, -10.0, 99.0)
        assertEquals(-10.0, order.quantity)
        assertEquals(99.0, order.stop)
        val e1 = order.execute(100.0, Instant.now())
        assertTrue { e1.isEmpty() }
        assertFalse { order.executed }

        val e2 = order.execute(98.0, Instant.now())
        assertEquals(-10.0, e2.first().quantity)
        assertTrue { order.executed }
    }

    @Test
    fun testLimitOrder() {
        val asset = TestData.usStock()
        val order = LimitOrder(asset, -10.0, 101.0)
        assertEquals(-10.0, order.quantity)
        assertEquals(101.0, order.limit)
        val e1 = order.execute(100.0, Instant.now())
        assertTrue { e1.isEmpty() }
        assertFalse { order.executed }

        val e2 = order.execute(102.0, Instant.now())
        assertEquals(-10.0, e2.first().quantity)
        assertTrue { order.executed }
    }


    @Test
    fun testStopLimitOrder() {
        val asset = TestData.usStock()
        val order = StopLimitOrder(asset, -10.0, 99.0, 98.0)
        assertEquals(-10.0, order.quantity)
        assertEquals(98.0, order.limit)
        val e1 = order.execute(100.0, Instant.now())
        assertTrue { e1.isEmpty() }
        assertFalse { order.executed }

        val e2 = order.execute(98.5, Instant.now())
        assertTrue { e2.isEmpty() }
        assertFalse { order.executed }

        val e3 = order.execute(97.0, Instant.now())
        assertEquals(-10.0, e3.first().quantity)
        assertTrue { order.executed }
    }

}