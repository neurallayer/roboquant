package org.roboquant.common

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import java.time.Instant


/**
 * Timeline is an ordered list of [Instant] instances, sorted from old to new. Every [Instant] is unique.
 *
 * Currently, it is just a typealias for List<Instant>, but this might change in the future.
 */
typealias Timeline = List<Instant>

/**
 * Observation contains a double [value] at a certain point in [time]. Observation can be compared based on
 * their [time].
 */
class Observation(val time: Instant, val value: Double) : Comparable<Observation> {
    override fun compareTo(other: Observation): Int = time.compareTo(other.time)
}

/**
 * Timeserie is an ordered list of [Observation] sorted from oldest to newest. There can be no observations with the
 * same [Observation.time].
 */
typealias Timeserie = List<Observation>

/**
 * Return the values of the timeserie as an double array
 */
fun Timeserie.toDoubleArray() : DoubleArray = map { it.value }.toDoubleArray()

/**
 * Return the Correlation between two timeseries. Only observations at the same time will be taken into account and
 * correlations are only calculated if there are at least [minObservations] observations.
 */
fun correlation(a: Timeserie, b: Timeserie, minObservations: Int = 3) : Double {
    require(a.isNotEmpty() && b.isNotEmpty())
    var offset1 = 0
    var offset2 = 0
    val data1 = mutableListOf<Double>()
    val data2 = mutableListOf<Double>()
    while (true) {
        val obs1 = a[offset1]
        val obs2 = b[offset2]
        when {
            obs1 > obs2 -> offset2++
            obs2 > obs1 -> offset1++
            else -> {
                data1.add(obs1.value)
                data2.add(obs2.value)
                offset1++
                offset2++
            }
        }
        if (offset1 > a.lastIndex || offset2 > b.lastIndex) break
    }

    return if (data1.size >= minObservations) {
        val calc = PearsonsCorrelation()
        calc.correlation(data1.toDoubleArray(), data2.toDoubleArray())
    } else {
        Double.NaN
    }
}

/**
 * Return the correlations for the timeseries, ensuring that at least [minObservations] are available for calculating
 * the correlation. By default the correlation for the same asset are excluded
 */
fun Map<Asset, Timeserie>.correlation(minObservations: Int = 3, excludeSame : Boolean = true): Map<Pair<Asset, Asset>, Double> {
    val result = mutableMapOf<Pair<Asset, Asset>, Double>()
    for ((asset1, timeserie1) in this) {
        for ((asset2, timeserie2) in this) {
            if (excludeSame && asset1 == asset2) continue
            val pair = Pair(asset2, asset1)
            if (pair !in result) {
                val corr = correlation(timeserie1, timeserie2, minObservations)
                result[Pair(asset1, asset2)] = corr
            }

        }
    }
    return result
}


/**
 * Return the [Timeline] of the [Timeserie]
 */
fun Timeserie.timeline() : Timeline = map { it.time }


/**
 * Return the index of the time that is closets to the provided time but doesn't exceed it. So it is the most recent
 * time but without looking into the future.
 *
 * If no such time is found return null
 */
fun Timeline.latestNotAfter(time: Instant): Int? {
    var idx = binarySearch(time)
    idx = if (idx < 0) -idx - 2 else idx
    return if (idx >= 0) idx else null
}

/**
 * Return the index of the time that is closets to the provided time but doesn't proceed it.
 *
 * If no such time is found return null
 */
fun Timeline.earliestNotBefore(time: Instant): Int? {
    var idx = binarySearch(time)
    idx = if (idx < 0) -idx - 1 else idx
    return if (idx < size) idx else null
}

/**
 * Return the timeframe for this timeline. If the timeline is empty, an exception will be thrown.
 */
val Timeline.timeframe
    get() = Timeframe.inclusive(first(), last())

/**
 * Split the timeline in chunks of [size]
 */
fun Timeline.split(size: Int): List<Timeframe> {
    require(size > 1) {"Minimum requires 2 elements in timeline"}
    val chunks = chunked(size)
    val result = mutableListOf<Timeframe>()
    for (chunk in chunks) {
        if (size > 1) result.add(Timeframe(chunk.first(), chunk.last()) )
    }
    result[result.lastIndex] = result.last().inclusive
    return result
}
