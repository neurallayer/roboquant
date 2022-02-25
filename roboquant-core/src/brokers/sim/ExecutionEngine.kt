package org.roboquant.brokers.sim

import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.util.*

/**
 * Executes the orders.
 *
 * @property pricingEngine
 * @constructor Create empty Execution engine
 */
class ExecutionEngine(private val pricingEngine: PricingEngine = NoSlippagePricing()) {

    companion object {

        /**
         * Get a new command for a cerain order
         */
        fun getHandler(order: Order, orderHandlers: List<OrderHandler<*>> = emptyList()): OrderHandler<*> {
            @Suppress("UNCHECKED_CAST")
            return when (order) {
                is MarketOrder -> MarketOrderHandler(order)
                is LimitOrder -> LimitOrderHandler(order)
                is StopLimitOrder -> StopLimitOrderHandler(order)
                is StopOrder -> StopOrderHandler(order)
                is TrailLimitOrder -> TrailLimitOrderHandler(order)
                is TrailOrder -> TrailOrderHandler(order)
                is BracketOrder -> BracketOrderHandler(order)
                is OneCancelsOtherOrder -> OCOOrderHandler(order)
                is OneTriggersOtherOrder -> OTOOrderHandler(order)
                is UpdateOrder -> UpdateOrderHandler(order, orderHandlers)
                is CancelOrder -> CancelOrderHandler(order, orderHandlers )
                else -> throw Exception("Unsupported Order type $order")
            }
        }

    }

    // Currently active order commands
    private val orderHandlers = LinkedList<OrderHandler<*>>()

    /**
     * Latetst Order states
     */
    val orderStates
        get() = orderHandlers.map { it.state }

    // Add a new order to the execution engine
    fun add(order: Order) = orderHandlers.add(getHandler(order, orderHandlers))


    // Add a new order to the execution engine
    fun addAll(orders: List<Order>) {
        for (order in orders) add(order)
    }


    fun execute(event: Event): List<Execution> {
        val executions = mutableListOf<Execution>()

        // Now run the trade order commands
        val prices = event.prices
        for (exec in orderHandlers.toList()) {

            if (exec.status.closed) {
                orderHandlers.remove(exec)
                continue
            }

            val action = prices[exec.order.asset] ?: continue
            val pricing = pricingEngine.getPricing(action, event.time)
            val newExecutions = exec.execute(pricing, event.time)
            executions.addAll(newExecutions)

        }
        return executions
    }


    fun clear() {
        orderHandlers.clear()
        pricingEngine.clear()
    }

}


