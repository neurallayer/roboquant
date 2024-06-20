package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import org.roboquant.strategies.Signal

/**
 * Interface for tracking progress during a run
 */
interface Journal {

    /**
     * track the progress of a particular run
     */
    fun track(event: Event, account: Account, signals: List<Signal>, instructions: List<Instruction>)

    /**
     * reset the state
     */
    fun reset() {
        // NOP
    }

}


