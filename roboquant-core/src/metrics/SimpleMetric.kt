package org.roboquant.metrics

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

/**
 * This is the abstract base class for many type of metrics that can calculate a metric fast and immediately return
 * the results. The only method required to implement is [calc]
 *
 * More advanced type of metrics might prefer to implement the [Metric] interface directly.
 *
 * @constructor Create empty Simple metric
 */
abstract class SimpleMetric : Metric {

    private var values: MetricResults = mapOf()

    /**
     * Calculate the metric given the account and step. This method will be invoked at the
     * end of each step in a run. The result is [Map] with keys being a unique metric name and the value
     * the calculated metric value. If no metrics are calculated, an empty map should be returned instead.
     *
     * When metrics rely on state, it is important they override the Component methods to ensure they reset their
     * state when appropriate.
     *
     * @param account
     * @param event
     * @return
     */
    override fun calculate(account: Account, event: Event) {
        values = calc(account, event)
    }

    /**
     * Based on the provided account and event, calculate any metrics and return them. This method needs to be implemented
     * in sub-classes.
     *
     * @param account
     * @param event
     * @return
     */
    abstract fun calc(account: Account, event: Event) : MetricResults

    override fun getMetrics(): MetricResults {
        val result = values
        values = emptyMap()
        return result
    }

    override fun start(phase: Phase) {
        values = emptyMap()
    }

    override fun reset() {
        values = emptyMap()
    }
}