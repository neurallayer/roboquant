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

package org.roboquant.orders


import org.roboquant.TestData
import org.roboquant.common.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SingleOrderTest {

    @Test
    fun testMarketOrder() {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, 100)
        assertEquals(100.0, order.size.toDouble())
        assertTrue(order.tif is GTC)
        assertTrue(order.buy)
        assertTrue(order.toString().isNotBlank())
    }

    @Test
    fun testOrderTag() {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, Size(100), tag = "test")
        assertEquals("test", order.tag)
    }



    @Test
    fun testStopOrder() {
        val asset = TestData.usStock()
        val order = StopOrder(asset, Size(-10), 99.0)
        assertEquals(-10.0, order.size.toDouble())
        assertEquals(99.0, order.stop)
        assertTrue(order.sell)
        assertTrue(order.toString().isNotBlank())
    }

    @Test
    fun testLimitOrder() {
        val asset = TestData.usStock()
        val order = LimitOrder(asset, Size(-10), 101.0)
        assertEquals(-10.0, order.size.toDouble())
        assertEquals(101.0, order.limit)
        assertTrue(order.sell)
        assertTrue(order.toString().isNotBlank())
    }


    @Test
    fun testStopLimitOrder() {
        val asset = TestData.usStock()
        val order = StopLimitOrder(asset, Size(-10), 99.0, 98.0)
        assertEquals(-10.0, order.size.toDouble())
        assertEquals(98.0, order.limit)
        assertTrue(order.sell)
        assertTrue(order.toString().isNotBlank())

    }

}