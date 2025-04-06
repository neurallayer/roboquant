package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.common.TimeSeries
import org.roboquant.feeds.Event
import org.roboquant.metrics.Metric
import org.roboquant.common.Order
import java.time.Instant
import java.util.*

/**
 * Base interface for journals that can be configured using metrics.
 */
interface MetricsJournal: Journal {

    /**
     * Return all the metrics that are contained in this journal
     * @return Set<String>
     */
    fun getMetricNames() : Set<String>

    /**
     * Return a metric
     * @param name String
     * @return TimeSeries
     */
    fun getMetric(name: String): TimeSeries
}

/**
 * This journal stores the results of one or more metrics in memory
 *
 * @property metrics Array<out Metric>
 * @constructor
 */
class MemoryJournal(private vararg val metrics: Metric) : MetricsJournal {

    private val history = TreeMap<Instant, Map<String, Double>>()

    override fun track(event: Event, account: Account, instructions: List<Order>) {
        val result = mutableMapOf<String, Double>()
        for (metric in metrics) {
            val values = metric.calculate(account, event)
            result.putAll(values)
        }
        history[event.time] = result
    }

    override fun getMetricNames() : Set<String> {
        return history.values.map { it.keys }.flatten().toSet()
    }

    /**
     *
     * @param name String
     * @return TimeSeries
     */
    override fun getMetric(name: String): TimeSeries {
        val timeline = mutableListOf<Instant>()
        val values = mutableListOf<Double>()
        for ( (t, d) in history) {
            if (name in d) {
                timeline.add(t)
                values.add(d.getValue(name))
            }

        }
        return TimeSeries(timeline, values.toDoubleArray())
    }

}

