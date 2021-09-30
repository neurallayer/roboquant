package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.SingleOrder
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.resolve

/**
 * Policy that just buys or sells a fixed [quantity] of an asset based on the received signals. Useful
 * during testing a strategy, but should not be used in live trading of realistic back-tests.
 *
 */
class TestPolicy(private val quantity: Double = 1.0) : BasePolicy() {

    /**
     * @see Policy.act
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<SingleOrder>()
        for (signal in signals.resolve()) {
            val order: MarketOrder? = when (signal.rating) {
                Rating.BUY, Rating.OUTPEFORM -> MarketOrder(signal.asset, quantity)
                Rating.SELL, Rating.UNDERPERFORM -> MarketOrder(signal.asset, -quantity)
                Rating.HOLD -> null
            }
            orders.addNotNull(order)
        }
        return orders
    }
}