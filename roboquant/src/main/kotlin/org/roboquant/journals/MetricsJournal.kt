package org.roboquant.journals

import org.roboquant.brokers.Account
import org.roboquant.common.TimeSeries
import org.roboquant.feeds.Event
import org.roboquant.metrics.Metric
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant
import java.util.*

/**
 *
 * @property metrics Array<out Metric>
 * @property history MutableList<Entry>
 * @constructor
 */
class MetricsJournal(private vararg val metrics: Metric) : Journal {

    private val history = TreeMap<Instant, Map<String, Double>>()

    override fun track(event: Event, account: Account, signals: List<Signal>, orders: List<Order>) {
        val result = mutableMapOf<String, Double>()
        for (metric in metrics) {
            val values = metric.calculate(account, event)
            result.putAll(values)
        }
        history[event.time] = result
    }

    override fun reset() {
        history.clear()
    }

    fun getMetricNames() : Set<String> {
        return history.values.map { it.keys }.flatten().toSet()
    }

    /**
     *
     * @param name String
     * @return TimeSeries
     */
    fun getMetric(name: String): TimeSeries {
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

