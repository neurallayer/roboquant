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

package org.roboquant.brokers

import org.roboquant.common.Summary
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus

/**
 * Optimized container for storing orders used by the Account.

 * @see Order
 *
 * @constructor Create new Orders container
 */
class Orders : ArrayList<Order>() {


    /**
     * Closed orders, any order that is in a state that cannot be further processed
     */
    val closed
        get() = filter { it.status.closed }

    /**
     * Open orders, these are orders that can still be processed
     * TODO: Optimize the acces to open orders since that is called often and small subset of all orders in a large backtest
     */
    val open
        get() = filter { it.status.open }

    /**
     * Orders that are in [OrderStatus.ACCEPTED] state, and so they are ready for execution
     */
    val accepted
        get() = filter { it.status === OrderStatus.ACCEPTED }


    /**
     * Provide a summary for the orders, split by open and closed orders
     *
     * @return
     */
    fun summary(): Summary {
        val s = Summary("Orders")

        val c = Summary("closed")
        for (order in closed) c.add("$order")
        if (closed.isEmpty()) c.add("EMPTY")
        s.add(c)

        val o = Summary("open")
        for (order in open) o.add("$order")
        if (open.isEmpty()) o.add("EMPTY")
        s.add(o)

        return s
    }

    /**
     *
     * @param orders
     */
    internal fun put(orders: Orders) {
        addAll(orders.map { it.clone() })
    }

}
