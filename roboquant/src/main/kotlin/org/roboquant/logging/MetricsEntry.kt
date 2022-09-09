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

package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.common.Summary
import org.roboquant.common.clean
import org.roboquant.common.std

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
     * Get a key that uniquely defines the metric across a run and episode.
     */
    internal val groupId
        get() = """${name}/${info.run}/${info.episode}"""

    /**
     * Compare two metric entries based on their [value]
     */
    override fun compareTo(other: MetricsEntry): Int {
        return value.compareTo(other.value)
    }

}

/**
 * Group a collection of metrics by their unique name, run and episode
 */
fun Collection<MetricsEntry>.group(): Map<String, List<MetricsEntry>> = groupBy { it.groupId }

fun Map<String, List<MetricsEntry>>.max(): Map<String, MetricsEntry> = mapValues { it.value.max() }

fun Map<String, List<MetricsEntry>>.min(): Map<String, MetricsEntry> = mapValues { it.value.min() }

/**
 * Get the [n] highest entries, default being 10
 */
fun Collection<MetricsEntry>.high(n: Int = 10) = sortedBy { it }.takeLast(n)

/**
 * Get the [n] lowest entries, default being 10
 */
fun Collection<MetricsEntry>.low(n: Int = 10) = sortedBy { it }.take(n)

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
            val newEntry = entry.copy(value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}

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
            val newEntry = entry.copy(value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}

fun Collection<MetricsEntry>.summary(): Summary {
    val result = Summary("Metrics")
    val m = groupBy { it.name }
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