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
 * Order handler factory creates an [OrderHandler] for an order. The provided handler is responsible for simulating
 * the executing the order.
 *
 * @param T
 * @constructor Create empty Order handler factory
 */
fun interface OrderHandlerFactory<T : Order> {

    /**
     * Get a handler for the provided order
     *
     * @param order
     * @return
     */
    fun getHandler(order: T): OrderHandler
}

/**
 * Engine that simulates how orders are executed on financial markets. For any order to be executed, it needs a
 * corresponding [OrderHandler] to be registered.
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
         * All the registered [OrderHandlerFactory]
         */
        val factories = mutableMapOf<KClass<*>, OrderHandlerFactory<Order>>()

        /**
         * Return the order handler for the provided [order]. This will throw an exception if no [OrderHandlerFactory]
         * is registered for the order::class.
         */
        fun getHandler(order: Order): OrderHandler {
            val factory = factories.getValue(order::class)
            return factory.getHandler(order)
        }

        /**
         * Unregister the order handler for order type [T]
         */
        inline fun <reified T : Order> unregister() {
            factories.remove(T::class)
        }

        /**
         * Register a new order handler [factory] for order type [T]. If there was already an order handler registered
         * for the same class it will be replaced.
         */
        inline fun <reified T : Order> register(factory: OrderHandlerFactory<T>) {
            @Suppress("UNCHECKED_CAST")
            factories[T::class] = factory as OrderHandlerFactory<Order>
        }

        init {
            // register all the default included order handlers

            // Single Order types
            register<MarketOrder> { MarketOrderHandler(it) }
            register<LimitOrder> { LimitOrderHandler(it) }
            register<StopLimitOrder> { StopLimitOrderHandler(it) }
            register<StopOrder> { StopOrderHandler(it) }
            register<TrailLimitOrder> { TrailLimitOrderHandler(it) }
            register<TrailOrder> { TrailOrderHandler(it) }

            // Advanced order types
            register<BracketOrder> { BracketOrderHandler(it) }
            register<OCOOrder> { OCOOrderHandler(it) }
            register<OTOOrder> { OTOOrderHandler(it) }

            // Modify order types
            register<UpdateOrder> { UpdateOrderHandler(it) }
            register<CancelOrder> { CancelOrderHandler(it) }
            register<CancelAllOrder> { CancelAllOrderHandler(it) }
        }

    }

    // Return the create-handlers
    private val createHandlers = LinkedList<CreateOrderHandler>()

    // Return the modify-handlers
    private val modifyHandlers = LinkedList<ModifyOrderHandler>()

    /**
     * Get the open order handlers
     */
    private fun <T : OrderHandler> List<T>.open() = filter { it.status.open }

    /**
     * Remove all handlers of closed orders
     */
    internal fun removeClosedOrders() {
        createHandlers.removeIf { it.state.status.closed }
        modifyHandlers.removeIf { it.state.status.closed }
    }

    /**
     * Return the order states of all handlers
     */
    val orderStates
        get() = createHandlers.map { it.state } + modifyHandlers.map { it.state }

    /**
     * Add a new [order] to the execution engine. Orders can only be processed if there is a corresponding handler
     * registered for the order class.
     */
    fun add(order: Order): Boolean {

        return when (val handler = getHandler(order)) {
            is ModifyOrderHandler -> modifyHandlers.add(handler)
            is CreateOrderHandler -> createHandlers.add(handler)
        }

    }


    /**
     * Add all [orders] to the execution engine.
     * @see [add]
     */
    fun addAll(orders: List<Order>) {
        for (order in orders) add(order)
    }

    /**
     * Execute all the handlers of orders that are not yet closed based on the [event].
     *
     * Underlying Logic:
     *
     * 1. First process any open modify orders (like cancel or update)
     * 2. Then process any regular order but only if there is a price action in the event for the underlying asses
     */
    fun execute(event: Event): List<Execution> {

        // We always first execute modify orders. These are run even if there is
        // no price for the asset known
        for (handler in modifyHandlers.open()) {
            handler.execute(createHandlers, event.time)
        }

        // Now run the create order commands
        val executions = mutableListOf<Execution>()
        val prices = event.prices
        for (handler in createHandlers.open()) {
            val action = prices[handler.state.asset] ?: continue
            val pricing = pricingEngine.getPricing(action, event.time)
            val newExecutions = handler.execute(pricing, event.time)
            executions.addAll(newExecutions)
        }
        return executions
    }

    /**
     * Clear any state in the execution engine. All the pending open orders will be removed.
     */
    fun clear() {
        createHandlers.clear()
        pricingEngine.clear()
    }

}


