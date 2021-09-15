package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.Phase
import org.roboquant.common.Summary
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.util.*

/**
 * Store metric results in memory. Very convenient in a Jupyter notebook when you want to inspect or visualize
 * some results after a run. This is also the default metric logger if none is specified.
 *
 * If you log large amounts of data, this could cause memory issues in the JVM. But for normal back testing this should
 * not pose a problem.
 *
 * @property showProgress Display a progress bar, default is true
 * @property maxHistorySize The maximum history kept in memory, default is Int.MAX_VALUE
 *
 * @constructor Create new MemoryLogger instance
 */
class MemoryLogger(var showProgress: Boolean = true, private val maxHistorySize: Int = Int.MAX_VALUE) : MetricsLogger {

    val history = LinkedList<MetricsEntry>()
    private var progressBar = ProgressBar()

    @Synchronized
    override fun log(metrics: Map<String, Number>, info: RunInfo) {
        if (showProgress) progressBar.update(info)

        for ((t, u) in metrics) {
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
     * Provided a summary of the recorded metrics for last events (default is 1)
     *
     * @param last
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
     * Get available episodes for a run
     *
     * @param run
     */
    fun getEpisodes(run: Int) = history.filter { it.info.run == run }.map { it.info.episode }.distinct().sorted()


    /**
     * Get the unique list of metric names that have been captured
     *
     * @return
     */
    fun getMetricNames(): List<String> {
        return history.map { it.metric }.distinct()
    }

    /**
     * Get metric for all the runs
     *
     * @param name
     * @return
     */
    fun getMetric(name: String): List<MetricsEntry> {
        return history.filter { it.metric == name }
    }

}


fun Collection<MetricsEntry>.groupBy(period: String): Map<String, Collection<MetricsEntry>> {
    return when(period) {
        "year" -> groupBy { it.info.time.atOffset(ZoneOffset.UTC).year.toString() }
        "month" -> groupBy {
            val d = it.info.time.atOffset(ZoneOffset.UTC)
            val mStr = String.format("%02d", d.month.value)
            "${d.year}-$mStr"
        }
        "week" -> groupBy {
            val d = it.info.time.atOffset(ZoneOffset.UTC)
            val wStr = String.format("%02d", d.get(ChronoField.ALIGNED_WEEK_OF_YEAR))
            "${d.year}-$wStr"
        }
        else -> throw Exception("Not supported $period")
    }
}

fun Collection<MetricsEntry>.toDoubleArray() = map { it.value.toDouble() }.toDoubleArray()

/**
 * remove non-finite values from a DoubleArray. These include Inf and NaN values.
 */
fun DoubleArray.clean() = filter { it.isFinite() }.toDoubleArray()

fun Collection<MetricsEntry>.getName(): String {
    return map { it.metric }.distinct().joinToString("/") { it.replace('.', ' ') }
}