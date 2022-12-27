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

import org.roboquant.brokers.sim.PricingEngine
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.util.*
import kotlin.reflect.KClass

/**
 * Engine that simulates how orders are executed on financial markets. For any order to be executed, it needs a
 * corresponding [OrderExecutor] to be registered.
 *
 * @property pricingEngine pricing engine to use to determine the price
 * @constructor Create new Execution engine
 */
class ExecutionEngine(private val pricingEngine: PricingEngine) {

    /**
     * @suppress
     */
    companion object {

        /**
         * All the registered [OrderExecutorFactory]
         */
        val factories = mutableMapOf<KClass<*>, OrderExecutorFactory<Order>>()

        /**
         * Return the order executor for the provided [order]. This will throw an exception if no [OrderExecutorFactory]
         * is registered for the order::class.
         */
        internal fun <T : Order> getExecutor(order: T): OrderExecutor<T> {
            val factory = factories.getValue(order::class)

            @Suppress("UNCHECKED_CAST")
            return factory.getExecutor(order) as OrderExecutor<T>
        }

        /**
         * Return the order executor for the provided [order]. This will throw an exception if no [OrderExecutorFactory]
         * is registered for the order::class.
         */
        internal fun <T : CreateOrder> getCreateOrderExecutor(order: T): CreateOrderExecutor<T> {
            val factory = factories.getValue(order::class)

            @Suppress("UNCHECKED_CAST")
            return factory.getExecutor(order) as CreateOrderExecutor<T>
        }

        /**
         * Unregister the order executor factory for order type [T]
         */
        inline fun <reified T : Order> unregister() {
            factories.remove(T::class)
        }

        /**
         * Register a new order executor [factory] for order type [T]. If there was already a factory registered
         * for the same class it will be replaced.
         */
        inline fun <reified T : Order> register(factory: OrderExecutorFactory<T>) {
            @Suppress("UNCHECKED_CAST")
            factories[T::class] = factory as OrderExecutorFactory<Order>
        }

        init {
            // register all the default included order handlers

            // Single Order types
            register<MarketOrder> { MarketOrderExecutor(it) }
            register<LimitOrder> { LimitOrderExecutor(it) }
            register<StopLimitOrder> { StopLimitOrderExecutor(it) }
            register<StopOrder> { StopOrderExecutor(it) }
            register<TrailLimitOrder> { TrailLimitOrderExecutor(it) }
            register<TrailOrder> { TrailOrderExecutor(it) }

            // Advanced order types
            register<BracketOrder> { BracketOrderExecutor(it) }
            register<OCOOrder> { OCOOrderExecutor(it) }
            register<OTOOrder> { OTOOrderExecutor(it) }

            // Modify order types
            register<UpdateOrder> { UpdateOrderExecutor(it) }
            register<CancelOrder> { CancelOrderExecutor(it) }
        }

    }

    // Return the create-order executors
    private val createOrders = LinkedList<CreateOrderExecutor<*>>()

    // Return the modify-order executors
    private val modifyOrders = LinkedList<ModifyOrderExecutor<*>>()

    /**
     * Get the open order executors
     */
    private fun <T : OrderExecutor<*>> List<T>.open() = filter { it.status.open }

    /**
     * Remove all executors of closed orders, both create orders and modify orders
     */
    internal fun removeClosedOrders() {
        createOrders.removeIf { it.status.closed }
        modifyOrders.removeIf { it.status.closed }
    }

    /**
     * Return the order states of all executors
     */
    internal val orderStates
        get() = createOrders.map { Pair(it.order, it.status) } + modifyOrders.map { Pair(it.order, it.status) }


    /**
     * Add a new [order] to the execution engine. Orders can only be processed if there is a corresponding executor
     * registered for the order class.
     */
    internal fun add(order: Order): Boolean {

        return when (val executor = getExecutor(order)) {
            is ModifyOrderExecutor -> modifyOrders.add(executor)
            is CreateOrderExecutor -> createOrders.add(executor)
        }

    }


    /**
     * Add all [orders] to the execution engine.
     * @see [add]
     */
    internal fun addAll(orders: List<Order>) {
        for (order in orders) add(order)
    }


    /**
     * Execute all the orders that are not yet closed based on the [event] and return the resulting executions.
     *
     * Underlying Logic:
     *
     * 1. First process any open modify orders (like cancel or update)
     * 2. Then process any regular create order, but only if there is a price action in the event for the
     * underlying asses
     */
    internal fun execute(event: Event): List<Execution> {
        val time = event.time

        // We always first execute modify-orders. These are run even if there is no known price for the asset
        for (executor in modifyOrders.open()) {
            val createHandler = createOrders.firstOrNull { it.order.id == executor.createOrder.id }
            executor.execute(createHandler, time)
        }

        // Now execute the create-orders. These are only run if there is a known price
        val executions = mutableListOf<Execution>()
        val prices = event.prices
        for (executor in createOrders.open()) {
            val action = prices[executor.order.asset] ?: continue
            val pricing = pricingEngine.getPricing(action, time)
            val newExecutions = executor.execute(pricing, time)
            executions.addAll(newExecutions)
        }
        return executions
    }

    /**
     * Clear any state in the execution engine. All the pending (open) orders will be removed.
     * Only the registered execution factories will remain.
     */
    fun clear() {
        createOrders.clear()
        pricingEngine.clear()
    }

}


