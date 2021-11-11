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
import org.junit.Test
import java.time.Instant
import kotlin.test.assertTrue


internal class BracketOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = BracketOrder(
            MarketOrder(asset, 10.0),
            LimitOrder(asset, -10.0, 101.0),
            StopOrder(asset, -10.0, 99.0),
        )
        assertTrue(order.main is MarketOrder)

        val e = order.execute(100.0, Instant.now())
        assertTrue { e.isNotEmpty() }


        val order2 = BracketOrder.fromPercentage(asset, 10.0, 100.0, 0.01, 0.01)
        assertTrue(order2.loss is StopOrder)
        assertTrue(order2.profit is LimitOrder)
    }

}