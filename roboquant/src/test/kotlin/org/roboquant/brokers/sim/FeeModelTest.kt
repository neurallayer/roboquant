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

package org.roboquant.brokers.sim

import org.roboquant.TestData
import org.roboquant.brokers.sim.execution.Execution
import org.roboquant.common.Size
import org.roboquant.common.percent
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FeeModelTest {

    private val trades = TestData.internalAccount().toAccount().trades

    @Test
    fun testDefaultCostModel() {
        val model = PercentageFeeModel(fee = 1.percent)
        val order = TestData.usMarketOrder()
        val fee = model.calculate(Execution(order, Size(5), 100.0), Instant.now(), trades)
        assertEquals(5.0, fee)
    }

    @Test
    fun noCostModel() {
        val model = NoFeeModel()
        val order = TestData.usMarketOrder()
        val fee = model.calculate(Execution(order, Size(100), 10.0), Instant.now(), trades)
        assertEquals(0.0, fee)
    }

}
