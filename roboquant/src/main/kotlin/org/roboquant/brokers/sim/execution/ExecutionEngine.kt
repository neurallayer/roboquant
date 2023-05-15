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
        val factories = mutableMapOf<KClass<*>, OrderExecutorFactory<CreateOrder>>()

        /**
         * Return the order executor for the provided [order]. This will throw an exception if no [OrderExecutorFactory]
         * is registered for the order::class.
         */
        internal fun <T : CreateOrder> getExecutor(order: T): OrderExecutor<T> {
            val factory = factories.getValue(order::class)

            @Suppress("UNCHECKED_CAST")
            return factory.getExecutor(order) as OrderExecutor<T>
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
        inline fun <reified T : CreateOrder> register(factory: OrderExecutorFactory<T>) {
            @Suppress("UNCHECKED_CAST")
            factories[T::class] = factory as OrderExecutorFactory<CreateOrder>
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

        }

    }

    private class ModifyOrderState(val order: ModifyOrder, var status: OrderStatus = OrderStatus.INITIAL)
    private val modifiers = LinkedList<ModifyOrderState>()

    // Return the create-order executors
    private val executors = LinkedList<OrderExecutor<*>>()



    /**
     * Get the open order executors
     */
    private fun <T : OrderExecutor<*>> List<T>.open() = filter { it.status.open }

    /**
     * Remove all executors of closed orders, both create orders and modify orders
     */
    internal fun removeClosedOrders() {
        executors.removeIf { it.status.closed }
        modifiers.removeIf { it.status.closed }
    }

    /**
     * Return the order states of all executors
     */
    internal val orderStates
        get() = executors.map { Pair(it.order, it.status) } + modifiers.map { Pair(it.order, it.status) }


    /**
     * Add a new [order] to the execution engine. Orders can only be processed if there is a corresponding executor
     * registered for the order class.
     */
    internal fun add(order: Order): Boolean {
        return when(order) {
            is ModifyOrder -> modifiers.add(ModifyOrderState(order, OrderStatus.INITIAL))
            is CreateOrder -> executors.add(getExecutor(order))
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
        for (modifyOrderState in modifiers) {
            val modifyOrder = modifyOrderState.order
            modifyOrderState.status = OrderStatus.ACCEPTED
            val createHandler = executors.firstOrNull { it.order.id == modifyOrder.order.id }
            if (createHandler != null && createHandler.modify(modifyOrder, time)) {
                modifyOrderState.status = OrderStatus.COMPLETED
            } else {
                modifyOrderState.status = OrderStatus.REJECTED
            }
        }

        // Now execute the create-orders. These are only run if there is a known price
        val openCreateOrders = executors.open()

        // Return if nothing to do to avoid creation of event.prices
        if (openCreateOrders.isEmpty()) return emptyList()

        val executions = mutableListOf<Execution>()
        val prices = event.prices
        for (executor in openCreateOrders) {
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
        executors.clear()
        modifiers.clear()
        pricingEngine.clear()
    }

}


