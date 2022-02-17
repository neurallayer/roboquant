package org.roboquant.brokers.sim

import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.util.*


internal class ExecutionEngine(private val pricingEngine: PricingEngine)  {

    companion object {

        /**
         * Get a new command for a cerain order
         */
        fun getOrderCommand(order: Order) : OrderCommand {
            return when (order) {
                is MarketOrder -> MarketOrderCommand(order)
                is LimitOrder -> LimitOrderCommand(order)
                is StopLimitOrder -> StopLimitOrderCommand(order)
                is StopOrder -> StopOrderCommand(order)
                is BracketOrder -> BracketOrderCommand(order)
                is OneCancelsOtherOrder -> OCOOrderCommand(order)
                else -> throw Exception("Unsupported Order type $order")
            }

        }

    }

    // Currently active order commands
    private val orderCommands = LinkedList<OrderCommand>()

    // Add a new order to the execution engine
    fun add(order: Order) = orderCommands.add(getOrderCommand(order))


    // Add a new order to the execution engine
    fun addAll(orders: List<Order>) {
        for (order in orders) orderCommands.add(getOrderCommand(order))
    }


    fun execute(event: Event) : List<Execution> {
        val executions = mutableListOf<Execution>()
        // First run the modify order commands

        // Now run the trade order commands
        val prices = event.prices
        for (cmd in orderCommands.toList()) {
            val order = cmd.order

            if (order.status.closed) {
                orderCommands.remove(cmd)
                continue
            }

            val action = prices[order.asset] ?: continue
            val pricing = pricingEngine.getPricing(action, event.time)

            if (order.status == OrderStatus.INITIAL) {
                order.state.status = OrderStatus.ACCEPTED
                order.state.placed = event.time
            }

            val newExecutions = cmd.execute(pricing, event.time)
            executions.addAll(newExecutions)

            if (order.status.closed) orderCommands.remove(cmd)
        }
        return executions
    }


    fun clear() {
        orderCommands.clear()
        pricingEngine.clear()
    }

}


