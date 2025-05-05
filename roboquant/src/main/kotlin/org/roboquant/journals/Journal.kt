package org.roboquant.journals

import org.roboquant.common.Account
import org.roboquant.common.Event
import org.roboquant.common.Order
import org.roboquant.common.Signal

/**
 * Interface for tracking progress during a run
 */
interface Journal {

    /**
     * Track the progress of a particular run. This method is invoked at each step during a run.
     *
     * The passed instructions are only those instructions that were generated during this step.
     */
    fun track(event: Event, account: Account, signals: List<Signal>, orders: List<Order>)

}


