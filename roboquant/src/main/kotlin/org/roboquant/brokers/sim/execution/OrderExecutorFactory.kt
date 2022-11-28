package org.roboquant.brokers.sim.execution

import org.roboquant.orders.Order

/**
 * Factory that creates an [OrderExecutor] for an order. The provided executor is responsible for simulating
 * the executing of the order.
 *
 * @param T type of order
 */
fun interface OrderExecutorFactory<T : Order> {

    /**
     * Get an executor for the provided [order]
     */
    fun getExecutor(order: T): OrderExecutor<T>
}