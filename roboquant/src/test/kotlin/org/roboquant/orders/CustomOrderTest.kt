/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.orders

import org.roboquant.TestData
import org.roboquant.common.Asset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CustomOrderTest {

    private class MySpecialOrder(asset: Asset) : Order(asset, "")

    @Test
    fun basic() {
        val asset = TestData.usStock()
        val order = MySpecialOrder(asset)
        assertTrue(order.info().isEmpty())
        assertTrue(order.toString().isNotBlank())
        assertEquals("MYSPECIAL", order.type)
        assertEquals(asset, order.asset)
    }

    @Test
    fun state() {
        val asset = TestData.usStock()
        val order = MySpecialOrder(asset)
        assertEquals(OrderStatus.CREATED, order.status)
        assertEquals("", order.id)

    }

}
