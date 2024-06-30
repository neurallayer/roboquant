package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import java.time.Instant
import kotlin.math.max

/**
 * Tracks basic progress metrics and optionally print them to the console
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

    override fun reset() {
        nItems = 0L
        nOrders = 0L
        nEvents = 0L
        maxPositions = 0
        lastTime = null
    }

}
