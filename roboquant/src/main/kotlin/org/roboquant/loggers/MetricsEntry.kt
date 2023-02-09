/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.loggers

import org.roboquant.RunInfo
import org.roboquant.common.Summary
import org.roboquant.common.clean

/**
 * Single metric entry ([name] and [value]) with the associated [RunInfo]. This is a read-only class.
 *
 * @property name
 * @property value
 * @property info
 * @constructor Create empty Metrics entry
 */
data class MetricsEntry(val name: String, val value: Double, val info: RunInfo) : Comparable<MetricsEntry> {

    /**
     * Get a key that uniquely defines the metric across a run.
     */
    internal val groupId
        get() = "${name}/${info.run}/${info.phase}"

    /**
     * Compare two metric entries based on their [value]
     */
    override fun compareTo(other: MetricsEntry): Int {
        return value.compareTo(other.value)
    }

}

/**
 * Group a collection of metrics by their unique combination of name, run and phase.
 */
fun Collection<MetricsEntry>.group(): Map<String, List<MetricsEntry>> = groupBy { it.groupId }

/**
 * Get the [n] highest entries, default being 10
 */
fun Collection<MetricsEntry>.high(n: Int = 10) = sortedBy { it }.takeLast(n)

/**
 * Get the [n] lowest entries, default being 10
 */
fun Collection<MetricsEntry>.low(n: Int = 10) = sortedBy { it }.take(n)

/**
 * Return the difference (new - old) of the values
 */
fun Collection<MetricsEntry>.diff(): List<MetricsEntry> {
    val result = mutableListOf<MetricsEntry>()
    var first = true
    var prev = 0.0
    for (entry in this) {
        if (first) {
            prev = entry.value
            first = false
        } else {
            val newValue = entry.value - prev
            val newName = entry.name + ".diff"
            val newEntry = entry.copy(name = newName, value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}

/**
 * Return the percentage difference (new - old) / old  of the values
 */
fun Collection<MetricsEntry>.perc(): List<MetricsEntry> {
    val result = mutableListOf<MetricsEntry>()
    var first = true
    var prev = 0.0
    for (entry in this) {
        if (first) {
            prev = entry.value
            first = false
        } else {
            val newValue = 100.0 * (entry.value - prev) / prev
            val newName = entry.name + ".perc"
            val newEntry = entry.copy(name = newName, value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}

/**
 * Provide a summary for the metrics
 */
fun Collection<MetricsEntry>.summary(name: String = "metrics"): Summary {
    val result = Summary(name)
    val m = groupBy { it.name }
    for ((metricName, values) in m) {
        val child = Summary(metricName)
        child.add("size", values.size)
        if (values.isNotEmpty()) {
            val arr = values.toDoubleArray().clean()
            child.add("min", arr.minOrNull())
            child.add("max", arr.maxOrNull())
            child.add("avg", arr.average())
            child.add("runs", values.map { it.info.run }.distinct().size)
            val timeline = map { it.info.time }.sorted()
            child.add("first time", timeline.first())
            child.add("last time", timeline.last())
        }
        result.add(child)
    }
    return result
}