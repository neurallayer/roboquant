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
 * Policy that just buys or sells a fixed [quantity] of an asset, generating only [MarketOrder]s
 *
 * Because of the simple deterministic behavior, useful during testing/debugging a strategy. But it should not be used
 * in live trading of realistic back-tests.
 */
class TestPolicy(private val quantity: Double = 1.0) : BasePolicy() {

    /**
     * Create a buys or sells [MarketOrder] for an asset based on the received [signals]. It ignores the [account]
     * and [event] parameters.
     *
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