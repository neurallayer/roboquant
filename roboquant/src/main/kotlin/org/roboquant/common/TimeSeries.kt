/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.common

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*


class MetricEntry(val time: Instant, val value: Double)

/**
 * Optimized implementation of data TimeSeries that allows for easy and fast calculations of metric results.
 */
class TimeSeries(private val values: DoubleArray, private val timeline: Timeline) : Iterable<MetricEntry> {

    constructor(values: List<MetricEntry>) : this(
        values.map { it.value }.toDoubleArray(), values.map { it.time }
    )

    val timeframe
        get() = timeline.timeframe

    val size
        get() = values.size

    private class MetricEntriesIterator(private val values: DoubleArray, private val times: List<Instant>) :
        Iterator<MetricEntry> {

        var count = 0

        override fun hasNext(): Boolean {
            return count < values.size
        }

        override fun next(): MetricEntry {
            if (count >= values.size) throw NoSuchElementException()
            val result = MetricEntry(times[count], values[count])
            count++
            return result
        }

    }

    fun shuffle(): TimeSeries {
        val newValues = values.copyOf()
        newValues.shuffle(Config.random)
        return TimeSeries(newValues, timeline)
    }

    init {
        require(values.size == timeline.size) { "values and times need to have the same size" }
    }

    operator fun plus(other: Number) = TimeSeries(values + other, timeline)

    operator fun minus(other: Number) = TimeSeries(values - other, timeline)

    operator fun times(other: Number) = TimeSeries(values * other, timeline)

    operator fun div(other: Number) = TimeSeries(values / other, timeline)

    fun returns() = TimeSeries(values.returns(), timeline.drop(1))

    fun max() = toList().maxBy { it.value }

    fun min() = toList().minBy { it.value }

    fun average() = values.average()

    fun diff()  = TimeSeries(values.diff(), timeline.drop(1))

    fun sum() = values.sum()

    fun groupBy(
        period: ChronoUnit,
        zoneId: ZoneId = ZoneOffset.UTC
    ): Map<String, TimeSeries> {

        val formatter = when (period) {
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
        formatter.timeZone = TimeZone.getTimeZone(zoneId)
        return groupBy {
            val date = Date.from(it.time)
            formatter.format(date)
        }.mapValues { TimeSeries(it.value) }
    }

    fun runningFold(initial: Double): TimeSeries {
        val newData = values.runningFold(initial) { last, current -> (current + 1.0) * last }.drop(1)
        return TimeSeries(newData.toDoubleArray(), timeline)
    }

    fun growthRates() = TimeSeries(values.growthRates(), timeline.drop(1))

    override fun iterator(): Iterator<MetricEntry> {
        return MetricEntriesIterator(values, timeline)
    }

    fun toDoubleArray() = values

    fun toList(): List<MetricEntry> {
        return values.zip(timeline).map { MetricEntry(it.second, it.first) }
    }

    fun isNotEmpty() = size > 0


}

/**
 * Flatten a map of TimeSeries to a single TimeSeries sorted by their time. If there is overlap in time between runs and
 * [noOverlap] is set to true, the earlier run will win and later runs entries that overlap will be ignored.
 */
fun Map<String, TimeSeries>.flatten(noOverlap: Boolean = true): TimeSeries {
    val sortedTimeSeries = values.sortedBy { it.first().time }
    val result = mutableListOf<MetricEntry>()
    var last = Instant.MIN
    for (timeSeries in sortedTimeSeries) {
        for (entry in timeSeries) {
            if (noOverlap && entry.time <= last) continue
            result.add(entry)
            last = entry.time
        }

    }
    return TimeSeries(result)
}

/**
 * Convert a sorted by time collection to a [TimeSeries] object
 */
inline fun <T> Collection<T>.toTimeSeries(block: (T) -> Pair<Instant, Double>): TimeSeries {
    val values = DoubleArray(size)
    val times = ArrayList<Instant>(size)
    forEachIndexed { cnt, elem ->
        val (t, v) = block(elem)
        values[cnt] = v
        times.add(t)
    }
    return TimeSeries(values, times)
}