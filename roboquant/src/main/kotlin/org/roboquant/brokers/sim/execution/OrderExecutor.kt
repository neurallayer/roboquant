/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.sim.Pricing
import org.roboquant.orders.CreateOrder
import org.roboquant.orders.ModifyOrder
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Interface for any order executor. This is a sealed interface and there are only
 * two sub interfaces:
 *
 * 1. [ModifyOrderExecutor] for orders that modify other orders, but don't generate trades themselves
 * 2. [CreateOrderExecutor] for new orders that (possibly) generate trades
 *
 * Any change to the status or order should only be done it the OrderExecutor itself, thus keep this logic isolated to
 * that particular implementation.
 */
sealed interface OrderExecutor<T: Order> {

    /**
     * The order to be executed
     */
    val order: T

    /**
     * The current status of the order execution
     */
    val status: OrderStatus

}

/**
 * Interface for orders that update another order. These orders don't generate trades by themselves. Also, important
 * to note that the following logic applies:
 *
 *  - they are executed first, before any [CreateOrderExecutor] orders are executed
 *  - they are always executed, even if there is no known price for the underlying asset at that moment in time
 *
 */
interface ModifyOrderExecutor<T : ModifyOrder> : OrderExecutor<T> {

    /**
     * The create-order that needs be modified
     */
    val createOrder: CreateOrder

    /**
     * Modify the order of the provided [executor] and [time].
     *
     * Implementations need to handle the fact that no executor is found and a null is passed instead. Typically, they
     * would set their own status to REJECTED.
     */
    fun execute(executor: CreateOrderExecutor<*>?, time: Instant)

}

/**
 * Interface for orders that (might) generate trades.
 */
interface CreateOrderExecutor<T : CreateOrder> : OrderExecutor<T> {

    /**
     * Execute the order for the provided [pricing] and [time] and return zero or more [Execution]
     */
    fun execute(pricing: Pricing, time: Instant): List<Execution>

    /**
     * Update the order, return true if the update was successful, false otherwise
     */
    fun update(order: CreateOrder, time: Instant) : Boolean

    /**
     * Cancel the order, return true if the cancellation was successful, false otherwise
     */
    fun cancel(time: Instant) : Boolean

}



