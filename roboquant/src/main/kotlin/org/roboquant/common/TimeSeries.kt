/*
 * Copyright 2020-2025 Neural Layer
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

import org.roboquant.common.timeframe
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * An observation represents a single [value] of the type [Double] at a precise moment in [time].
 * Observations can be compared by their time.
 *
 * @property time the time the value was observed
 * @property value the value of the observation
 */
data class Observation(val time: Instant, val value: Double) : Comparable<Observation> {

    /**
     * Compare to [other] observation based on their [time], and NOT their [value]
     */
    override fun compareTo(other: Observation): Int {
        return time.compareTo(other.time)
    }

}

/**
 * Optimized implementation of time series data that allows for easy and fast calculations. The times are stored as
 * a [timeline] and the [values] are stored as a DoubleArray.
 *
 * @property timeline the timeline
 * @property values the array of values
 */
@Suppress("TooManyFunctions")
class TimeSeries(val timeline: Timeline, val values: DoubleArray) : Iterable<Observation> {

    constructor(values: List<Observation>) : this(
        values.map { it.time }, values.map { it.value }.toDoubleArray()
    )

    init {
        require(timeline.size == values.size) {"timeline and values should be of equal size"}
    }

    /**
     * Return the timeframe of this time-series
     */
    val timeframe
        get() = timeline.timeframe

    /**
     * Return the size of this time-series
     */
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

    /**
     * Iterate of this time-series
     */
    inline fun forEach(block: (Instant, Double) -> Unit) {
        for (i in values.indices) {
            block(timeline[i], values[i])
        }
    }

    /**
     * Add a number to all values in this timeseries
     */
    operator fun plus(other: Number) = TimeSeries(timeline, values + other)

    /**
     * Subtract a number to all values in this timeseries
     */
    operator fun minus(other: Number) = TimeSeries(timeline, values - other)

    /**
     * Multiply a number to all values in this timeseries
     */
    operator fun times(other: Number) = TimeSeries(timeline, values * other)

    /**
     * Divide a number to all values in this timeseries
     */
    operator fun div(other: Number) = TimeSeries(timeline, values / other)

    /**
     * Create the [n] returns for all values
     */
    fun returns(n:Int = 1) = TimeSeries(timeline.drop(n), values.returns(n))

    /**
     * Index the values by dividing all the values by the first value that is finite
     */
    fun index(start: Double = 1.0) = TimeSeries(timeline, values.index(start))

    /**
     * Normalize the values
     */
    fun normalize() = TimeSeries(timeline, values.normalize())

    /**
     * Return the observation that contains the maximum value.
     */
    fun max(): Observation {
        val idx = values.indexOfMax()
        return Observation(timeline[idx], values[idx])
    }

    /**
     * Return the observation that contains the minimum value.
     */
    fun min(): Observation {
        val idx = values.indexOfMin()
        return Observation(timeline[idx], values[idx])
    }

    /**
     * Return a clean time-series in which all values are finite.
     */
    fun clean(): TimeSeries {
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

    /**
     * Return the average of all values
     */
    fun average() = values.average()

    /**
     * Return the difference for all values
     */
    fun diff(n: Int = 1) = TimeSeries(timeline.drop(n), values.diff(n))

    /**
     * Return the sum over all values
     */
    fun sum() = values.sum()

    /**
     * Returns this timeseries grouped by the provided [period] and optional, the provided [zoneId].
     * If no [zoneId] is provided, [ZoneOffset.UTC] will be used.
     */
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
                throw IllegalArgumentException("Unsupported value for period: $period")
            }
        }
        formatter.timeZone = TimeZone.getTimeZone(zoneId)
        return toList().groupBy {
            val date = Date.from(it.time)
            formatter.format(date)
        }.mapValues { TimeSeries(it.value) }
    }

    /**
     * Returns an iterator containing the observations
     */
    override fun iterator(): Iterator<Observation> {
        return ObservationIterator(timeline, values)
    }

    /**
     * Returns the values as a DoubleArray.
     */
    fun toDoubleArray() = values

    /**
     * Returns a list containing the observations
     */
    fun toList(): List<Observation> {
        return values.zip(timeline).map { Observation(it.second, it.first) }
    }

    /**
     * Returns true if not empty, false otherwise.
     */
    fun isNotEmpty() = size > 0


}


/**
 * Normalize all the timeseries in his map
 */
fun Map<String, TimeSeries>.index(start: Double = 1.0): Map<String, TimeSeries> {
    return mapValues { it.value.index(start) }
}

/*
 * Convert a sorted by time collection to a [TimeSeries] object
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
 */
