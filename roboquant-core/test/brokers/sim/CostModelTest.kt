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

package org.roboquant.brokers.sim

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.feeds.TradePrice
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CostModelTest {

    @Test
    fun testDefaultCostModel() {
        val model = DefaultCostModel(feePercentage = 0.01)
        val order = TestData.usMarketOrder()
        val price = model.calculatePrice(order, TradePrice(order.asset, 100.0))
        assertTrue { price > 100.0 }
        val fee = model.calculateFee(Execution(order, 5.0, price))
        assertTrue { fee > 4.9 && fee < 5.1 }
    }

    @Test
    fun noCostModel() {
        val model = NoCostModel()
        val order = TestData.usMarketOrder()
        val price = model.calculatePrice(order, TradePrice(order.asset, 100.0))
        assertTrue { price == 100.0 }
        val fee = model.calculateFee(Execution(order, 100.0, price))
        assertEquals(0.0, fee)
    }

}