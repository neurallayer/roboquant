package org.roboquant.brokers.sim.execution

import org.roboquant.orders.Order

/**
 * Factory creates an [OrderExecutor] for an order. The provided handler is responsible for simulating
 * the executing of the order.
 *
 * @param T type of order
 */
fun interface OrderExecutorFactory<T : Order> {

    /**
     * Get a handler for the provided order
     *
     * @param order
     * @return
     */
    fun getHandler(order: T): OrderExecutor<T>
}