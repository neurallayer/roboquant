package org.roboquant.brokers

import org.roboquant.common.Component
import org.roboquant.feeds.Event
import org.roboquant.orders.Order

/**
 * Interface for any broker, both simulated and real live brokers.
 *
 */
interface Broker : Component {

    /**
     * The client account
     */
    val account: Account

    /**
     * Place new orders at this broker. After processing them, this method returns an instance of the
     * updated account. It is important that the placed orders are indeed included in the account, as either open or
     * closed.
     *
     * See also [Order]
     *
     * @param orders list of orders to be placed at the broker
     * @return the updated account that reflects the latest state
     */
    fun place(orders: List<Order>, event: Event): Account



}
