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
import java.time.Instant

/**
 * Calculate the broker fee/commissions to be used for executed trades.
 */
interface FeeModel {

    /**
     * Returns the fees or commissions applicable for the provided [execution]. The returned value is denoted in the
     * currency of the underlying asset.
     *
     * More advanced fee models can look at past [trades] to avoid over-billing in case of a multi-fill order and
     * use [time] to perform relevant currency conversions
     *
     * Typically, a fee should be a positive value, unless you want to model rebates and other rewards.
     */
    fun calculate(execution: Execution, time: Instant, trades: List<Trade>): Double

}

