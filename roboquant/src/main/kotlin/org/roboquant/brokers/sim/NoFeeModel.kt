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

import org.roboquant.brokers.sim.execution.Execution
import java.time.Instant

/**
 * Fee model that adds no additional fee or commissions to trade executions.
 */
class NoFeeModel : FeeModel {

    /**
     * @see FeeModel.calculate
     */
    override fun calculate(execution: Execution, time: Instant, trades: List<Trade>): Double = 0.0

}
