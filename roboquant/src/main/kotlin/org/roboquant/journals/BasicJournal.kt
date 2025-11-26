package org.roboquant.journals

import org.roboquant.common.Account
import org.roboquant.common.Event
import org.roboquant.common.Order
import org.roboquant.common.Signal
import java.time.Instant
import kotlin.math.max

/**
 * Tracks basic progress metrics and optionally print them to the console. This journal has very low overhead and provides
 * basic insights into what is happening.
 */
class BasicJournal(private val printToConsole: Boolean = false) : Journal {

    /**
     * Number of items
     */
    var nItems: Long = 0L

    /**
     * Number of orders created
     */
    var nOrders: Long = 0L

    /**
     * Number of events
     */
    var nEvents: Long = 0L

    /**
     * Maximum number of open positions at the same time
     */
    var maxPositions: Int = 0

    /**
     * Number of signals generated
     */
    var nSignals: Long = 0L

    /**
     * Last time an event was received
     */
    var lastTime: Instant? = null

    override fun track(event: Event, account: Account, signals: List<Signal>, orders: List<Order>) {
        nItems += event.items.size
        nOrders += orders.size
        nSignals += signals.size
        nEvents += 1
        lastTime = event.time
        maxPositions = max(maxPositions, account.positions.size)

        if (printToConsole)
            println(this)
    }

    override fun toString(): String {
        return "time=$lastTime items=$nItems signals=$nSignals orders=$nOrders max-positions=$maxPositions"
    }

}
