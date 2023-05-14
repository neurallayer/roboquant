/*
 * Copyright 2020-2023 Neural Layer
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

import java.time.Instant

/**
 * Single metric entry ([time] and [value]). This is a read-only class.
 *
 * @property value the value of the metric
 * @property time the time of the metric
 * @constructor Create a new instance of Metrics entry
 */
data class MetricsEntry(val value: Double, val time: Instant) : Comparable<MetricsEntry> {


    /**
     * Compare two metric entries based on their [value]
     */
    override fun compareTo(other: MetricsEntry): Int {
        return value.compareTo(other.value)
    }

}



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
            val newEntry = entry.copy(value = newValue)
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
            val newEntry = entry.copy(value = newValue)
            result.add(newEntry)
            prev = entry.value
        }
    }
    return result
}

