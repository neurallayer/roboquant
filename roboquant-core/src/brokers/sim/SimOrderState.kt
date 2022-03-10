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

import org.roboquant.brokers.InternalAccount
import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Part of order processing that can change
 */
data class SimOrderState(
    override val order: Order,
    override val status: OrderStatus = OrderStatus.INITIAL,
    override val openedAt: Instant = Instant.MIN,
    override val closedAt: Instant = Instant.MAX
) : OrderState {



    override val open
        get() = status.open

    override val closed
        get() = status.closed

    override val asset
        get() = order.asset

    override val id
        get() = order.id

    /**
     * Update the order state and return the new order state (if applicable)
     *
     * @param time
     * @param newStatus
     * @return
     */
    fun update(time: Instant, newStatus: OrderStatus = OrderStatus.ACCEPTED) : SimOrderState {
        return if (newStatus === OrderStatus.ACCEPTED && status === OrderStatus.INITIAL) {
            SimOrderState(order, newStatus, time)
        } else if (newStatus.closed && status.open) {
            val openTime = if (openedAt === Instant.MIN) time else openedAt
            SimOrderState(order, newStatus, openTime, time)
        } else {
            this
        }
    }

}

/**
 * Reject an order
 *
 * @param order
 * @param time
 */
fun InternalAccount.rejectOrder(order: Order, time: Instant) {
    putOrder(SimOrderState(order, OrderStatus.REJECTED, time, time))
}

/**
 * Accept an order
 *
 * @param order
 * @param time
 */
fun InternalAccount.acceptOrder(order: Order, time: Instant) {
    putOrder(SimOrderState(order, OrderStatus.ACCEPTED, time, time))
}


val Collection<Order>.initialOrderState
    get() = map { SimOrderState(it, OrderStatus.INITIAL) }

