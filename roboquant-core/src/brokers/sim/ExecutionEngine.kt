package org.roboquant.brokers.sim

import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.util.*


class ExecutionEngine(private val pricingEngine: PricingEngine) {

    companion object {

        /**
         * Get a new command for a cerain order
         */
        fun getOrderCommand(order: Order, orderCommands: List<OrderCommand<*>> = emptyList()): OrderCommand<*> {
            @Suppress("UNCHECKED_CAST")
            return when (order) {
                is MarketOrder -> MarketOrderCommand(order)
                is LimitOrder -> LimitOrderCommand(order)
                is StopLimitOrder -> StopLimitOrderCommand(order)
                is StopOrder -> StopOrderCommand(order)
                is TrailLimitOrder -> TrailLimitOrderCommand(order)
                is TrailOrder -> TrailOrderCommand(order)
                is BracketOrder -> BracketOrderCommand(order)
                is OneCancelsOtherOrder -> OCOOrderCommand(order)
                is OneTriggersOtherOrder -> OTOOrderCommand(order)
                is UpdateOrder -> UpdateOrderCommand(order, orderCommands)
                is CancelOrder -> CancelOrderCommand(order, orderCommands )
                else -> throw Exception("Unsupported Order type $order")
            }

        }

    }

    // Currently active order commands
    internal val orderCommands = LinkedList<OrderCommand<*>>()

    // Add a new order to the execution engine
    fun add(order: Order) = orderCommands.add(getOrderCommand(order, orderCommands))


    // Add a new order to the execution engine
    fun addAll(orders: List<Order>) {
        for (order in orders) orderCommands.add(getOrderCommand(order, orderCommands))
    }


    fun execute(event: Event): List<Execution> {
        val executions = mutableListOf<Execution>()

        // Now run the trade order commands
        val prices = event.prices
        for (exec in orderCommands.toList()) {

            if (exec.status.closed) {
                orderCommands.remove(exec)
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
        orderCommands.clear()
        pricingEngine.clear()
    }

}


