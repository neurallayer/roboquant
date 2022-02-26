package org.roboquant.brokers.sim

import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.util.*
import kotlin.reflect.KClass


fun interface OrderHandlerFactory<T : Order> {

    fun getHandler(order: T): OrderHandler
}

/**
 * Executes the orders.
 *
 * @property pricingEngine
 * @constructor Create empty Execution engine
 */
class ExecutionEngine(private val pricingEngine: PricingEngine = NoSlippagePricing()) {

    companion object {

        val factories = mutableMapOf<KClass<*>, OrderHandlerFactory<Order>>()

        /**
         * Get the order handler for the [order]
         *
         * @param order
         * @return
         */
        fun getHandler(order: Order): OrderHandler {
            val factory = factories[order::class]!!
            return factory.getHandler(order)
        }

        /**
         * Unregister th eorder handler for order type [T]
         */
        inline fun <reified T : Order> unregister() {
            factories.remove(T::class)
        }

        /**
         * Register a order handler factory for order type [T]
         *
         * @param T
         * @param factory
         */
        inline fun <reified T : Order> register(factory: OrderHandlerFactory<T>) {
            @Suppress("UNCHECKED_CAST")
            factories[T::class] = factory as OrderHandlerFactory<Order>
        }

        init {
            // All the default included order handlers
            register<MarketOrder> { MarketOrderHandler(it) }
            register<LimitOrder> { LimitOrderHandler(it) }
            register<StopLimitOrder> { StopLimitOrderHandler(it) }
            register<StopOrder> { StopOrderHandler(it) }
            register<TrailLimitOrder> { TrailLimitOrderHandler(it) }
            register<TrailOrder> { TrailOrderHandler(it) }
            register<BracketOrder> { BracketOrderHandler(it) }
            register<OCOOrder> { OCOOrderHandler(it) }
            register<OTOOrder> { OTOOrderHandler(it) }
            register<UpdateOrder> { UpdateOrderHandler(it) }
            register<CancelOrder> { CancelOrderHandler(it) }
        }

    }


    // Currently active order commands
    private val tradeHandlers = LinkedList<TradeOrderHandler>()

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

        return when (val handler = getHandler(order)) {
            is ModifyOrderHandler -> modifyHandlers.add(handler)
            is TradeOrderHandler -> tradeHandlers.add(handler)
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
            val action = prices[exec.state.asset] ?: continue
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


