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

package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.common.Summary
import org.roboquant.common.summary
import org.roboquant.orders.OrderStatus.INITIAL
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * The order-state keeps track of a placed order. This is a read-only class and any [update] will generate
 * a new instance (also sometimes referred to as the Immutable State Pattern).
 *
 * @property order the order that is tracked
 * @property status the actual status or the order
 * @property openedAt when was the order first opened, if not yet known the value is [Instant.MAX]
 * @property closedAt  when was the order closed, if not yet known the value is [Instant.MAX]
 */
class OrderState private constructor(
    val order: Order,
    val status: OrderStatus,
    val openedAt: Instant,
    val closedAt: Instant
) {

    /**
     * Create a new OrderState instance with the status set to [OrderStatus.INITIAL]. This is the only way
     * to create a new OrderState. Sequential updates should be done using the [update] method.
     */
    constructor(order: Order) : this(order, INITIAL, Instant.MAX, Instant.MAX)

    /**
     * Returns true the order status is open, false otherwise
     */
    val open: Boolean
        get() = status.open

    /**
     * Returns true the order status is closed, false otherwise
     */
    val closed: Boolean
        get() = status.closed

    /**
     * Returns the asset of the order
     */
    val asset: Asset
        get() = order.asset

    /**
     * Returns the id of the order
     */
    val orderId: Int
        get() = order.id

    /**
     * Update the order state with a [newStatus] and [time] and optionally a new [order]. Return the updated version.
     * The original order state won't be modified.
     *
     * This method will take care that the right state transition happens. And if it is not allowed, it will throw an
     * `IllegalStateException`.
     */
    fun update(newStatus: OrderStatus, time: Instant, newOrder: Order = order): OrderState {
        check(!status.closed) { "cannot update a closed order, status=$status" }
        val newOpenedAt = if (openedAt == Instant.MAX) time else openedAt
        val newClosedAt = if (newStatus.closed) time else closedAt
        return OrderState(newOrder, newStatus, newOpenedAt, newClosedAt)
    }

}

private fun Instant.toPrettyString(): String {
    return if (this == Instant.MAX) "-" else this.truncatedTo(ChronoUnit.SECONDS).toString()
}

/*
class Table {
    var header: List<String> = emptyList()
    val rows = mutableListOf<List<Any>>()
}
*/

fun Collection<OrderState>.lines(): List<List<Any>> {
    val lines = mutableListOf<List<Any>>()
    lines.add(listOf("symbol", "type", "status", "id", "opened at", "closed at", "details"))
    forEach {
        with(it) {
            val infoString = order.info().map { entry -> "${entry.key}=${entry.value}" }.joinToString(" ")

            lines.add(
                listOf(
                    asset.symbol,
                    order.type,
                    status,
                    order.id,
                    openedAt.toPrettyString(),
                    closedAt.toPrettyString(),
                    infoString
                )
            )
        }
    }
    return lines
}

/**
 * Provide a summary for the collection of OrderState
 */
@JvmName("summaryOrders")
fun Collection<OrderState>.summary(name: String = "Orders"): Summary {
    val s = Summary(name)
    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val lines = lines()
        return lines.summary(name)
    }
    return s
}

/**
 * Returns true is the collection of orderStates contains at least one for [asset], false otherwise.
 */
operator fun Collection<OrderState>.contains(asset: Asset) = any { it.asset == asset }

/**
 * Create the required [CancelOrder]s to cancel the orders. Only [OrderStatus.open] orders of the type [CreateOrder]
 * can be cancelled and will be included in the returned cancellations.
 */
fun Collection<OrderState>.createCancelOrders(): List<CancelOrder> =
    filter { it.status.open && it.order is CreateOrder }.map { CancelOrder(it) }

/**
 * Get the unique assets for a collection of order states
 */
val Collection<OrderState>.assets: Set<Asset>
    get() = map { it.asset }.distinct().toSet()

/**
 * Return list of CancelOrder for any open orders in the collection
 */
fun Collection<OrderState>.cancel(): List<CancelOrder> {
    return this.filter { it.status.open }.map { CancelOrder(it) }
}
