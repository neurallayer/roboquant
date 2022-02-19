package org.roboquant.brokers

import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant




/**
 *
 */
data class OrderState<T: Order>(
    val order: T, // The initiating order
    // val status: OrderStatus = OrderStatus.INITIAL,
    val placed: Instant = Instant.MIN,
    val closed: Instant = Instant.MAX
) : Order by order


val List<Order>.orderSlips
    get() = map {OrderSlip(it)}

/**
 *
 */
data class OrderSlip<T: Order>(
    val order: T, // The original order
    // val status: OrderStatus = OrderStatus.INITIAL,
    val placed: Instant = Instant.MIN,
    val closed: Instant = Instant.MAX
) : Order by order

