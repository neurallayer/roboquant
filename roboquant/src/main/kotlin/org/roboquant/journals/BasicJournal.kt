package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant

/**
 * Tracks some basic progress metrics and optionally print them to the console
 */
class BasicJournal(private val printToConsole: Boolean = false) : Journal {

    var nItems = 0L
    var nSignals = 0L
    var nOrders = 0L
    var nEvents = 0L
    var lastTime: Instant? = null

    override fun track(event: Event, account: Account, signals: List<Signal>, orders: List<Order>) {
        nItems += event.actions.size
        nSignals += signals.size
        nOrders += orders.size
        nEvents += 1
        lastTime = event.time

        if (printToConsole)
            println(this)
    }

    override fun toString(): String {
        return "time=$lastTime items=$nItems signals=$nSignals orders=$nOrders"
    }

}
