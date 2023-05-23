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


class Observation(val time: Instant, val value: Double)

/**
 * Optimized implementation of time series data that allows for easy and fast calculations. The times are stored as
 * a [timeline] and the [values] are stored as a DoubleArray.
 */
class TimeSeries(val timeline: Timeline, val values: DoubleArray) : Iterable<Observation> {

    constructor(values: List<Observation>) : this(
        values.map { it.time }, values.map { it.value }.toDoubleArray()
    )

    init {
        assert(timeline.size == values.size)
    }

    val timeframe
        get() = timeline.timeframe

    val size
        get() = values.size

    private class ObservationIterator(private val times: List<Instant>, private val values: DoubleArray) :
        Iterator<Observation> {

        var count = 0

        override fun hasNext(): Boolean {
            return count < values.size
        }

        override fun next(): Observation {
            if (count >= values.size) throw NoSuchElementException()
            val result = Observation(times[count], values[count])
            count++
            return result
        }

    }

    fun shuffle(): TimeSeries {
        val newValues = values.copyOf()
        newValues.shuffle(Config.random)
        return TimeSeries(timeline, newValues)
    }


    inline fun forEach(block: (Instant, Double) -> Unit) {
        for (i in values.indices) {
            block(timeline[i], values[i])
        }
    }

    init {
        require(values.size == timeline.size) { "values and times need to have the same size" }
    }

    operator fun plus(other: Number) = TimeSeries(timeline, values + other)

    operator fun minus(other: Number) = TimeSeries(timeline, values - other)

    operator fun times(other: Number) = TimeSeries(timeline, values * other)

    operator fun div(other: Number) = TimeSeries(timeline, values / other)

    fun returns() = TimeSeries(timeline.drop(1), values.returns())

    fun max(): Observation {
        val idx = values.indexOfMax()
        return Observation(timeline[idx], values[idx])
    }

    fun min(): Observation {
        val idx = values.indexOfMin()
        return Observation(timeline[idx], values[idx])
    }


    fun clean() : TimeSeries {
        val x = ArrayList<Instant>(size)
        val y = ArrayList<Double>(size)
        for (i in values.indices) {
            val value = values[i]
            if (value.isFinite()) {
                x.add(timeline[i])
                y.add(value)
            }
        }
        return TimeSeries(x, y.toDoubleArray())
    }

    fun average() = values.average()

    fun diff()  = TimeSeries(timeline.drop(1), values.diff())

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
        return toList().groupBy {
            val date = Date.from(it.time)
            formatter.format(date)
        }.mapValues { TimeSeries(it.value) }
    }

    fun runningFold(initial: Double): TimeSeries {
        val newData = values.runningFold(initial) { last, current -> (current + 1.0) * last }.drop(1)
        return TimeSeries(timeline, newData.toDoubleArray())
    }

    fun growthRates() = TimeSeries(timeline.drop(1), values.growthRates())


    override fun iterator(): Iterator<Observation> {
        return ObservationIterator(timeline, values)
    }


    fun toDoubleArray() = values

    fun toList(): List<Observation> {
        return values.zip(timeline).map { Observation(it.second, it.first) }
    }

    fun isNotEmpty() = size > 0


}

/**
 * Flatten a Map of TimeSeries to a single TimeSeries sorted by their time. If there is overlap in time between runs and
 * [noOverlap] is set to true, the earlier run observations will be used and later runs observations that overlap will
 * be ignored.
 */
fun Map<String, TimeSeries>.flatten(noOverlap: Boolean = true): TimeSeries {
    val sortedTimeSeries = values.sortedBy { it.timeline.first() }
    val result = mutableListOf<Observation>()
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
    return TimeSeries(times, values)
}