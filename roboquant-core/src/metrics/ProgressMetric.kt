package org.roboquant.metrics

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Standard set of metric values that capture the progress of the run. The captured metrics are aggregated since
 * the start of a phase.
 *
 * The following metrics are captured:
 *
 * - The number of actions
 * - The number of events (or steps)
 * - The number of trades
 * - The number of orders
 * - The wall time
 */
class ProgressMetric : SimpleMetric() {

    private var startTime = Instant.now()
    private var actions = 0
    private var steps = 0

    override fun calc(account: Account, event: Event): MetricResults {
        actions += event.actions.size
        return mapOf(
            "progress.actions" to actions,
            "progress.events" to ++steps,
            "progress.trades" to account.trades.size,
            "progress.orders" to account.orders.size,
            "progress.wallTime" to (Instant.now().toEpochMilli() - startTime.toEpochMilli()),
        )
    }

    override fun start(phase: Phase) {
        startTime = Instant.now()
        actions = 0
        steps = 0
    }

}