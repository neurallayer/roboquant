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
import org.junit.jupiter.api.assertThrows
import org.roboquant.TestData
import org.roboquant.common.Size
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OCOOrderTest {

    val asset = TestData.usStock()

    @Test
    fun test() {

        val order = OCOOrder(
            LimitOrder(asset, Size(100), 10.9),
            StopOrder(asset, Size(100), 11.1)
        )
        assertEquals(order.first.asset, order.asset)
        assertTrue(order.toString().isNotBlank())

    }

    @Test
    fun testOCOFails() {
        val order1 = LimitOrder(asset, Size(100), 90.0)
        val order2 = MarketOrder(asset, 50)
        assertThrows<IllegalArgumentException> {
            OCOOrder(order1, order2)
        }
    }

}