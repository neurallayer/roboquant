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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CostModelTest {

    @Test
    fun testDefaultCostModel() {
        val model = DefaultCostModel()
        val order = TestData.usMarketOrder()
        val exec = Execution(order, order.quantity, 100.0)
        val (price, fee) = model.calculate(order, exec)
        assertTrue { price > exec.price }
        assertEquals(0.0, fee)
    }

    @Test
    fun testCommissionBasedCostModel() {
        val model = CommissionBasedCostModel()
        val order = TestData.usMarketOrder()
        val exec = Execution(order, order.quantity, 100.0)
        val (price, fee) = model.calculate(order, exec)
        assertTrue { price > exec.price }
        assertTrue { fee > 0.0 }
    }

}