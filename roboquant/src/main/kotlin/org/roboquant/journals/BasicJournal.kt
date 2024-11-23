package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import java.time.Instant
import kotlin.math.max

/**
 * Tracks basic progress metrics and optionally print them to the console. This journal has very low overhead and provides
 * basic insights into what is happening.
 */
class BasicJournal(private val printToConsole: Boolean = false) : Journal {

    var nItems = 0L
    var nOrders = 0L
    var nEvents = 0L
    var maxPositions = 0
    var lastTime: Instant? = null

    override fun track(event: Event, account: Account, instructions: List<Instruction>) {
        nItems += event.items.size
        nOrders += instructions.size
        nEvents += 1
        lastTime = event.time
        maxPositions = max(maxPositions, account.positions.size)

        if (printToConsole)
            println(this)
    }

    override fun toString(): String {
        return "time=$lastTime items=$nItems orders=$nOrders max-positions=$maxPositions"
    }

}
