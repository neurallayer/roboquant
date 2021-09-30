package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Component
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

/**
 * A policy is responsible for creating [Order]s, typically based on the [Signal]s it receives from a [Strategy].
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
     * Act on the received [signals], the [account] and the last [event], create zero or more orders for the broker
     * to process.
     *
     */
    fun act(signals: List<Signal>, account: Account, event: Event): List<Order>


}