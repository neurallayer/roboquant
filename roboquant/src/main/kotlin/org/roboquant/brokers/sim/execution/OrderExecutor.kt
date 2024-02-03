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

package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.sim.Pricing
import org.roboquant.orders.CreateOrder
import org.roboquant.orders.ModifyOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant


/**
 * Interface for orders that can generate trades.
 */
interface OrderExecutor<T : CreateOrder> {

    /**
     * The order to be executed
     */
    val order: T

    /**
     * The current status of the order execution
     */
    val status: OrderStatus

    /**
     * Execute the order for the provided [pricing] and [time] and return zero or more [Execution]
     */
    fun execute(pricing: Pricing, time: Instant): List<Execution>

    /**
     * Modify the order, return true if it was successful, false otherwise. Default is to return false
     */
    fun modify(modifyOrder: ModifyOrder, time: Instant): Boolean = false

}



