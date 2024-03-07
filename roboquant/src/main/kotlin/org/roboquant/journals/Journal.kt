package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

/**
 * Interface for tracking progress during a run
 */
interface Journal {

    /**
     * track progress
     */
    fun track(event: Event, account: Account, signals: List<Signal>, orders: List<Order>)

}


