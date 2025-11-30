package org.roboquant.journals

import org.roboquant.common.TimeSeries

/**
 * Utility class to make it easier to use a MetricsJournal in a multi-run setup.
 *
 * @property fn function to return a MetricsJournal based on the run name
 * @constructor
 */
class MultiRunJournal(private val fn: (String) -> MetricsJournal) {

    private val journals = mutableMapOf<String, MetricsJournal>()

    /**
     * Companion object to generate run names
     */
    companion object {
        private var cnt = 0

        /**
         * Generate the next run name in the format run-<n>
         */
        @Synchronized
        fun nextRun(): String = "run-${cnt++}"
    }

    /**
     * Get the journal for a specific [run]. If no journal exists yet for the run, a new one will be created
     * using the provided function at construction time.
     */
    @Synchronized
    fun getJournal(run: String = nextRun()): MetricsJournal {
        if (run !in journals) {
            val journal = fn(run)
            journals[run] = journal
        }
        return journals.getValue(run)
    }

    /**
     * Load potentially existing runs from MetricsJournals that are persistent
     * @param runs List<String>
     */
    fun load(runs: Collection<String>) {
        for (run in runs) {getJournal(run)}
    }

    /**
     * Get the currently available runs
     * @return Set<String>
     */
    fun getRuns() : Set<String> = journals.keys

    /**
     * Get the metric with the given [name] for all runs
     */
    fun getMetric(name: String) : Map<String, TimeSeries> {
        return journals.mapValues { it.value.getMetric(name) }
    }

    /**
     * Get the metric with the given [name] for a specific [run]
     */
    fun getMetric(name: String, run: String) : TimeSeries {
        return journals[run]?.getMetric(name) ?: TimeSeries(listOf(), doubleArrayOf())
    }

    /**
     * Get the names of all available metrics across all runs
     */
    fun getMetricNames() : Set<String> {
        return journals.values.map { it.getMetricNames() }.flatten().toSet()
    }

    /**
     * Reset the state of this multi-run, removing already registered runs
     */
    @Synchronized
    fun reset() {
        journals.clear()
    }

}
