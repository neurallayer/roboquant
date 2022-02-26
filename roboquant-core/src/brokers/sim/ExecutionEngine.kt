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
        fun getHandler(order: Order): OrderHandler {
            @Suppress("UNCHECKED_CAST")
            return when (order) {
                is MarketOrder -> MarketOrderHandler(order)
                is LimitOrder -> LimitOrderHandler(order)
                is StopLimitOrder -> StopLimitOrderHandler(order)
                is StopOrder -> StopOrderHandler(order)
                is TrailLimitOrder -> TrailLimitOrderHandler(order)
                is TrailOrder -> TrailOrderHandler(order)
                is BracketOrder -> BracketOrderHandler(order)
                is OCOOrder -> OCOOrderHandler(order)
                is OTOOrder -> OTOOrderHandler(order)
                is UpdateOrder -> UpdateOrderHandler(order)
                is CancelOrder -> CancelOrderHandler(order)
                else -> throw Exception("Unsupported Order type $order")
            }
        }

    }

    // Currently active order commands
    private val tradeHandlers = LinkedList<TradeOrderHandler<*>>()

    // Currently active order commands
    private val modifyHandlers = LinkedList<ModifyOrderHandler>()


    internal fun removeClosedOrders() {
        tradeHandlers.removeIf { it.state.status.closed }
        modifyHandlers.removeIf { it.state.status.closed }
    }


    /**
     * Latetst Order states
     */
    val orderStates
        get() = tradeHandlers.map { it.state } + modifyHandlers.map { it.state }



    // Add a new order to the execution engine
    fun add(order: Order): Boolean {

        return when(val handler = getHandler(order)) {
            is ModifyOrderHandler ->  modifyHandlers.add(handler)
            is TradeOrderHandler<*> -> tradeHandlers.add(handler)
        }

    }


    // Add a new order to the execution engine
    fun addAll(orders: List<Order>) {
        for (order in orders) add(order)
    }




    fun execute(event: Event): List<Execution> {

        // We always first execute the modify orders. These are run even if there is
        // no price for the asset known
        for (handler in modifyHandlers) handler.execute(tradeHandlers, event.time)

        // Now run the trade order commands
        val executions = mutableListOf<Execution>()
        val prices = event.prices
        for (exec in tradeHandlers.toList()) {
            if (exec.state.status.closed) continue
            val action = prices[exec.order.asset] ?: continue
            val pricing = pricingEngine.getPricing(action, event.time)
            val newExecutions = exec.execute(pricing, event.time)
            executions.addAll(newExecutions)

        }
        return executions
    }


    fun clear() {
        tradeHandlers.clear()
        pricingEngine.clear()
    }

}


