package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction

/**
 * Interface for tracking progress during a run
 */
interface Journal {

    /**
     * track the progress of a particular run
     */
    fun track(event: Event, account: Account, instructions: List<Instruction>)

}


