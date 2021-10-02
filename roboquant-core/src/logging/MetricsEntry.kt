package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.common.Summary
import org.roboquant.common.clean
import org.roboquant.common.std

/**
 * Single metric entry with the metadata. This is a read-only class.
 *
 * @property metric
 * @property value
 * @property info
 * @constructor Create empty Metrics entry
 */
class MetricsEntry(val metric: String, val value: Number, val info: RunInfo) {

    /**
     * Validate if another entry is from the same recorded episode.
     *
     * @param other
     * @return
     */
    fun sameEpisode(other: MetricsEntry): Boolean {
        val i = other.info
        return i.phase == info.phase && i.run == info.run && i.episode == info.episode && i.name == info.name
    }
}

fun Collection<MetricsEntry>.max() = maxOf { it.value.toDouble() }
fun Collection<MetricsEntry>.min() = minOf { it.value.toDouble() }

fun Collection<MetricsEntry>.summary(): Summary {
    val result = Summary("Metrics")
    val m = groupBy { it.metric }
    for ((name, values) in m) {
        val child = Summary(name)
        child.add("size", values.size)
        if (values.isNotEmpty()) {
            val arr = values.toDoubleArray().clean()
            child.add("min", arr.minOrNull())
            child.add("max", arr.maxOrNull())
            child.add("avg", arr.average())
            child.add("std", arr.std())
            child.add("runs", values.map { it.info.run }.distinct().size)
            val timeline = map { it.info.time }.sorted()
            child.add("first time", timeline.first())
            child.add("last time", timeline.last())
        }
        result.add(child)
    }
    return result
}