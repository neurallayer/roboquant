package org.roboquant.brokers.sim

import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.util.*


typealias OrderHandlerFactory = (order: Order, engine: ExecutionEngine) -> OrderHandler<*>

class ExecutionEngine(private val pricingEngine: PricingEngine) {

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


        private val handlers = mutableMapOf<String, OrderHandlerFactory>()

        fun register(orderType: String, factory: OrderHandlerFactory) {
            handlers[orderType] = factory
        }

        fun getHandler2(order: Order, engine: ExecutionEngine): OrderHandler<*> {
            val handler = handlers[order.type]!!
            return handler(order, engine)
        }

        init {
            register("MarketOrder") { order, _ -> MarketOrderHandler(order as MarketOrder)}

        }

    }

    // Currently active order commands
    internal val orderHandlers = LinkedList<OrderHandler<*>>()

    // Add a new order to the execution engine
    fun add(order: Order) = orderHandlers.add(getHandler(order, orderHandlers))


    // Add a new order to the execution engine
    fun addAll(orders: List<Order>) {
        for (order in orders) orderHandlers.add(getHandler(order, orderHandlers))
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


