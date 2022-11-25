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

package org.roboquant.brokers.sim

import org.roboquant.orders.OrderState
import java.time.Instant

/**
 * Interface for any order handler. This is a sealed interface and there are only
 * two sub interfaces:
 *
 * 1. [ModifyOrderHandler] for orders that modify other orders
 * 2. [TradeOrderHandler] for orders that generate trades
 *
 */
sealed interface OrderHandler {

    /**
     * What is the order state
     */
    var state: OrderState

    /**
     * Convenience attribute to access the status
     */
    val status
        get() = state.status

}

/**
 * Interface for orders that update another order. These orders don't generate trades by themselves. Also, important
 * to note that:
 *
 *  - they are executed first, before the [TradeOrderHandler] orders are executed
 *  - they are always executed, even if there is no known price for the underlying asset at that moment in time
 *
 */
interface ModifyOrderHandler : OrderHandler {

    /**
     * Modify the orders for the provided [handlers] and [time]
     */
    fun execute(handlers: List<OrderHandler>, time: Instant)

}

/**
 * Interface for orders that might generate trades.
 */
interface TradeOrderHandler : OrderHandler {

    /**
     * Execute the orders for the provided [pricing] and [time]
     */
    fun execute(pricing: Pricing, time: Instant): List<Execution>

}



