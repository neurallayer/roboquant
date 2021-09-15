package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Component
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

/**
 * A policy is responsible for creating [Order]s, typically based on the [Signal]s it receives from the strategy.
 *
 * Besides, turning signals into orders, a policy can also be used for:
 *
 * * order management, for example how to deal with open orders
 * * portfolio construction, for example re-balancing of the portfolio
 * * risk management, for example limit exposure to certain sectors
 *
 * Please note that the brokers who receive these orders might not support all the different order types.
 */
interface Policy : Component {

    /**
     * Act on the received signals and the account, and create zero or more orders for the broker to process.
     *
     * @param signals list of signals
     * @param account the account
     * @param event the step associated with this moment in time
     * @return The list of orders that will be sent to the broker
     */
    fun act(signals: List<Signal>, account: Account, event: Event): List<Order>


}