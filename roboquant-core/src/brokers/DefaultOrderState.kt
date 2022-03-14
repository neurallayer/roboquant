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

import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Default order state that implements the [OrderState] interface and is used by roboquant in most brokers
 * implementations.
 */
data class DefaultOrderState(
    override val order: Order,
    override val status: OrderStatus = OrderStatus.INITIAL,
    override val openedAt: Instant = Instant.MIN,
    override val closedAt: Instant = Instant.MAX
) : OrderState {

    /**
     * Update the order state and return the new order state (if applicable)
     *
     * @param time
     * @param newStatus
     * @return
     */
    fun update(time: Instant, newStatus: OrderStatus = OrderStatus.ACCEPTED) : DefaultOrderState {
        return if (newStatus === OrderStatus.ACCEPTED && status === OrderStatus.INITIAL) {
            DefaultOrderState(order, newStatus, time)
        } else if (newStatus.closed && status.open) {
            val openTime = if (openedAt === Instant.MIN) time else openedAt
            DefaultOrderState(order, newStatus, openTime, time)
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
    putOrder(DefaultOrderState(order, OrderStatus.REJECTED, time, time))
}

/**
 * Accept an order
 *
 * @param order
 * @param time
 */
fun InternalAccount.acceptOrder(order: Order, time: Instant) {
    putOrder(DefaultOrderState(order, OrderStatus.ACCEPTED, time, time))
}


val Collection<Order>.initialOrderState
    get() = map { DefaultOrderState(it, OrderStatus.INITIAL) }

