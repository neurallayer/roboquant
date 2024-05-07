package org.roboquant.journals

import org.roboquant.common.TimeSeries

/**
 *
 * @property fn Function1<String, MemoryJournal>
 * @property journals MutableMap<String, MemoryJournal>
 * @constructor
 */
class MultiRunJournal(private val fn: (String) -> MetricsJournal) {

    private val journals = mutableMapOf<String, MetricsJournal>()

    companion object {
        private var cnt = 0
    }

    @Synchronized
    fun getJournal(run: String = "run-${cnt++}"): MetricsJournal {
        if (run !in journals) {
            val journal = fn(run)
            journals[run] = journal
        }
        return journals.getValue(run)
    }

    /**
     * Load existing runs
     * @param runs List<String>
     */
    fun load(runs: List<String>) {
        for (run in runs) {getJournal(run)}
    }

    /**
     * Get the currently available runs
     * @return Set<String>
     */
    fun getRuns() : Set<String> = journals.keys

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
