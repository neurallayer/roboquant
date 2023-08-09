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
package org.roboquant.brokers.sim

import org.roboquant.brokers.Trade
import org.roboquant.brokers.sim.execution.Execution
import org.roboquant.common.percent
import java.time.Instant
import kotlin.math.absoluteValue

/**
 * The PercentageFeeModel defines a percentage of total value as the fee. For every trade it will calculate to the
 * total value and then use the [fee] as the fee.
 *
 * @property fee fee as a fraction of total execution cost. Default is 0.percent
 * @constructor Create a new percentage fee model
 */
class PercentageFeeModel(
    private val fee: Double = 0.percent,
) : FeeModel {

    init {
        require(fee in 0.0..1.0) {  "Fee should be between zero and one" }
    }

    override fun calculate(execution: Execution, time: Instant, trades: List<Trade>): Double {
        return execution.value.absoluteValue * fee
    }

}