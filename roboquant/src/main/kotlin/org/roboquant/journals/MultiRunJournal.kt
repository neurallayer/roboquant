package org.roboquant.journals

import org.roboquant.common.TimeSeries

/**
 *
 * @property fn Function1<String, MetricsJournal>
 * @property journals MutableMap<String, MetricsJournal>
 * @constructor
 */
class MultiRunJournal(private val fn: (String) -> MetricsJournal) {

    private val journals = mutableMapOf<String, MetricsJournal>()

    fun getJournal(run: String): MetricsJournal {
        if (run !in journals) {
            val journal = fn(run)
            journals[run] = journal
        }
        return journals.getValue(run)
    }

    fun getRuns() = journals.keys.toList()

    fun getMetric(name: String) : Map<String, TimeSeries> {
        return journals.mapValues { it.value.getMetric(name) }
    }

    fun getMetric(name: String, run: String) : TimeSeries {
        return journals[run]?.getMetric(name) ?: TimeSeries(listOf(), doubleArrayOf())
    }

    fun getMetricNames() : Set<String> {
        return journals.values.map { it.getMetricNames() }.flatten().toSet()
    }

}
