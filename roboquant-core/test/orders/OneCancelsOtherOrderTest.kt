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
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OneCancelsOtherOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = OneCancelsOtherOrder(
            LimitOrder(asset, 100.0, 10.9),
            LimitOrder(asset, 200.0, 11.1)
        )
        assertTrue(order.first is LimitOrder)
        order.execute(11.0, Instant.now())

        assertFalse(order.first.executed)
        assertTrue(order.second.executed)
        assertTrue(order.first.status.aborted)
        assertEquals(OrderStatus.COMPLETED, order.second.status)

    }

}