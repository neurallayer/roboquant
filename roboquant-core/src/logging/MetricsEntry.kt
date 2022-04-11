/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
data class MetricsEntry(val metric: String, val value: Double, val info: RunInfo) : Comparable<MetricsEntry> {

    /**
     * Get a key that unique defines the metric, run and episode.
     */
    val group
        get() = """${metric}/${info.run}/${info.episode}"""

    /**
     * Validate if another entry is from the same recorded episode.
     *
     * @param other
     * @return
     */
    fun sameEpisode(other: MetricsEntry): Boolean {
        val i = other.info
        return i.phase == info.phase && i.run == info.run && i.episode == info.episode
    }

    override fun compareTo(other: MetricsEntry): Int {
         return value.compareTo(other.value)
    }

}

fun Collection<MetricsEntry>.max() = maxByOrNull { it }!!
fun Collection<MetricsEntry>.min() = minByOrNull { it }!!
fun Collection<MetricsEntry>.high(n:Int = 10) = sortedBy { it.value }.takeLast(n)
fun Collection<MetricsEntry>.low(n:Int = 10) = sortedBy { it.value }.take(n)

fun Collection<MetricsEntry>.diff() : List<MetricsEntry> {
    val result = mutableListOf<MetricsEntry>()
    var first = true
    var prev = 0.0
    for (entry in this) {
        if (first) {
            prev = entry.value
            first = false
        } else {
            val newValue = entry.value - prev
            val newEntry = entry.copy(value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}


fun Collection<MetricsEntry>.perc() : List<MetricsEntry> {
    val result = mutableListOf<MetricsEntry>()
    var first = true
    var prev = 0.0
    for (entry in this) {
        if (first) {
            prev = entry.value
            first = false
        } else {
            val newValue = 100.0 * (entry.value - prev) / prev
            val newEntry = entry.copy(value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}


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