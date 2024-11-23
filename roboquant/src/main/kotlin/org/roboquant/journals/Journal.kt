package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction

/**
 * Interface for tracking progress during a run
 */
interface Journal {

    /**
     * Track the progress of a particular run. This method is invoked at each step during a run.
     *
     * The passed instructions are only those instructions that were generated during this step.
     */
    fun track(event: Event, account: Account, instructions: List<Instruction>)

}


