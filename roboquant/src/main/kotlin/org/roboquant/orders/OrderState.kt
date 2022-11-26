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

package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.common.Summary
import org.roboquant.common.summary
import org.roboquant.orders.OrderStatus.*
import java.time.Instant
import java.time.temporal.ChronoUnit


interface OrderState {
    val order: Order
    val status: OrderStatus
    val openedAt: Instant
    val closedAt: Instant

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
     * Returns the underlying asset
     */
    val asset: Asset
       get() = order.asset

    /**
     * Returns the id od the order
     */
    val orderId: Int
        get() = order.id
}


/**
 * The status an order can be in. The flow is straight forward and is adhered to by all broker implementations, even
 * brokers that have more order statuses.
 *
 *  - [INITIAL] -> [ACCEPTED] -> [COMPLETED] | [CANCELLED] | [EXPIRED]
 *  - [INITIAL] -> [REJECTED]
 *
 *  At any given time an [OrderState] is either [open] or [closed] state. Once an order reaches a [closed] state,
 *  it cannot be opened again and will not be further processed.
 */
enum class OrderStatus {

    /**
     * State of an order that has just been created. It will remain in this state until it is either
     * [REJECTED] or [ACCEPTED] by the broker.
     */
    INITIAL,

    /**
     * The order has been received, validated and accepted by the broker. The order remains in this state until it
     * goes to one of the end-states.
     */
    ACCEPTED,

    /**
     * The order has been successfully completed. This is an end-state.
     */
    COMPLETED,

    /**
     * The order was cancelled, normally by a [CancelOrder]. This is an end-state.
     */
    CANCELLED,

    /**
     *  The order has expired, normally triggered by a [TimeInForce] policy. This is an end-state.
     */
    EXPIRED,

    /**
     *  The order has been rejected. This is an end-state and could occur when:
     *  - The order is not valid, for example you try to short an asset while that is not allowed
     *  - You don't have enough buyingPower to place a new order
     *  - The order type is not supported by the broker
     *  - The provided asset is not recognised or cannot be traded on your account
     */
    REJECTED;

    /**
     * Returns true if the order has been aborted. That implies it is in one of the following three "error" end-states:
     * [CANCELLED], [EXPIRED], [REJECTED]
     */
    val aborted: Boolean
        get() = this === CANCELLED || this === EXPIRED || this === REJECTED

    /**
     * Returns true if the order closed. This means it has reached an end-state that doesn't allow for any more trading.
     * This implies it is in one of these four possible end-states: [COMPLETED], [CANCELLED], [EXPIRED] or [REJECTED].
     */
    val closed: Boolean
        get() = this === COMPLETED || this === CANCELLED || this === EXPIRED || this === REJECTED

    /**
     * Returns true if the order in an open state, so either [INITIAL] or [ACCEPTED]
     */
    val open: Boolean
        get() = this === INITIAL || this === ACCEPTED

}


/**
 * Provide a summary for the orders
 */
@JvmName("summaryOrders")
fun Collection<OrderState>.summary(name: String = "Orders"): Summary {
    val s = Summary(name)
    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val lines = mutableListOf<List<Any>>()
        lines.add(listOf("type", "symbol", "ccy", "status", "id", "opened at", "closed at", "details"))
        forEach {
            with(it) {
                val t1 = openedAt.truncatedTo(ChronoUnit.SECONDS)
                val t2 = closedAt.truncatedTo(ChronoUnit.SECONDS)
                val infoString = order.info().toString().removeSuffix("}").removePrefix("{")

                lines.add(
                    listOf(
                        order.type,
                        asset.symbol,
                        asset.currency,
                        status,
                        order.id,
                        t1,
                        t2,
                        infoString
                    )
                )
            }
        }
        return lines.summary(name)
    }
    return s
}


/**
 * Returns true is the collection of orderStates contains at least one for [asset], false otherwise.
 */
operator fun Collection<OrderState>.contains(asset: Asset) = any { it.asset == asset}

/**
 * Create the required [CancelOrder]s to cancel the orders. Only [OrderStatus.open] orders of the type [CreateOrder]
 * can be cancelled and will be returned.
 */
fun Collection<OrderState>.createCancelOrders() : List<CancelOrder> =
    filter { it.order is CreateOrder && it.status.open }.map { CancelOrder(it) }


/**
 * Get the unique assets for a collection of order states
 */
val Collection<OrderState>.assets : Set<Asset>
    get() = map { it.asset }.distinct().toSet()


