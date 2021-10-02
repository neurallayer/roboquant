package org.roboquant.logging

import org.roboquant.Phase
import org.roboquant.RunInfo
import org.roboquant.common.Summary
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Store metric results in memory. Very convenient in a Jupyter notebook when you want to inspect or visualize
 *  metric results after a run. This is also the default metric logger for roboquant if none is specified.
 *
 * If you log very large amounts of metric data, this could cause memory issues. But for normal back testing
 * this should not pose a problem. By specifying [maxHistorySize] you can further limit the amount stored in memory.
 *
 * By default, a bar will be shown that shows the progress of a run, but by setting [showProgress] to false this can be
 * disabled.
 */
class MemoryLogger(var showProgress: Boolean = true, private val maxHistorySize: Int = Int.MAX_VALUE) : MetricsLogger {

    val history = LinkedList<MetricsEntry>()
    private var progressBar = ProgressBar()

    @Synchronized
    override fun log(results: Map<String, Number>, info: RunInfo) {
        if (showProgress) progressBar.update(info)

        for ((t, u) in results) {
            if (history.size >= maxHistorySize) history.removeFirst()
            val entry = MetricsEntry(t, u, info)
            history.add(entry)
        }
    }

    override fun start(phase: Phase) {
        if (showProgress) progressBar.reset()
    }

    override fun end(phase: Phase) {
        if (showProgress) progressBar.done()
    }

    /**
     * Clear the history that is kept in the logger
     */
    override fun reset() {
        history.clear()
        progressBar.reset()
    }


    /**
     * Provided a summary of the recorded metrics for [last] events (default is 1)
     */
    fun summary(last: Int = 1): Summary {
        val s = Summary("Metrics")
        val lastEntry = history.lastOrNull()
        if (lastEntry == null) {
            s.add("No Data")
        } else {
            val lastStep = lastEntry.info.step - last
            val map = history.filter {
                it.info.step > lastStep && lastEntry.sameEpisode(it)
            }.groupBy { it.info.time }.toSortedMap()
            for ((time, entries) in map) {
                val t = Summary("$time")
                for (entry in entries) {
                    t.add(entry.metric, entry.value)
                }
                s.add(t)
            }
        }
        return s
    }

    /**
     * Get all the recorded runs in this logger
     */
    fun getRuns() = history.map { it.info.run }.distinct().sorted()

    /**
     * Get available episodes for a specific [run]
     */
    fun getEpisodes(run: Int) = history.filter { it.info.run == run }.map { it.info.episode }.distinct().sorted()


    /**
     * Get the unique list of metric names that have been captured
     */
    fun getMetricNames(): List<String> {
        return history.map { it.metric }.distinct()
    }

    /**
     * Get results for a metric specified by its [name]. It will include all the runs and episodes.
     */
    fun getMetric(name: String): List<MetricsEntry> {
        return history.filter { it.metric == name }
    }

}

internal fun Collection<MetricsEntry>.groupBy(period: ChronoUnit): Map<String, Collection<MetricsEntry>> {
    val formatter = when(period) {
        ChronoUnit.YEARS -> SimpleDateFormat("yyyy")
        ChronoUnit.MONTHS -> SimpleDateFormat("yyyy-MM")
        ChronoUnit.WEEKS -> SimpleDateFormat("yyyy-ww")
        ChronoUnit.DAYS -> SimpleDateFormat("yyyy-DDD")
        ChronoUnit.HOURS -> SimpleDateFormat("yyyy-DDD-HH")
        ChronoUnit.MINUTES -> SimpleDateFormat("yyyy-DDD-HH-mm")
        else -> {
            throw IllegalArgumentException("Unsupported value $period")
        }
    }
    return groupBy {
        formatter.format(Date.from(it.info.time))
    }
}


fun Collection<MetricsEntry>.toDoubleArray() = map { it.value.toDouble() }.toDoubleArray()


fun Collection<MetricsEntry>.getName(): String {
    return map { it.metric }.distinct().joinToString("/") { it.replace('.', ' ') }
}