package org.roboquant.common

import org.apache.commons.math3.stat.descriptive.moment.*
import org.apache.commons.math3.stat.descriptive.rank.Max
import org.apache.commons.math3.stat.descriptive.rank.Min
import org.apache.commons.text.StringEscapeUtils
import java.lang.Integer.max
import java.lang.Integer.min
import java.time.*

/******************************************************************************
 * This file contains the extensions for classes that are part of standard
 * Java and Kotlin libraries.
 *
 * Extensions for classes that are part of roboquant should not be included in
 * this file.
 */

/**
 * Make using string buffers a bit more pleasant
 *
 * @param s
 */
operator fun StringBuffer.plusAssign(s: String) {
    append(s)
}

operator fun Instant.plus(i: Int): Instant = plusMillis(i.toLong())
fun Instant.toUTC(): ZonedDateTime = atZone(ZoneId.of("UTC"))


fun Collection<String>.summary(header: String = "Values") : Summary {
    val result = Summary(header)
    forEach { result.add(it) }
    return result
}

/**
 * Extension function to allow *numpy* alike indexing for lists.
 *
 * ## Example
 *
 *      a = someList[0..10]
 *
 * @param T
 * @param range
 * @return
 */
operator fun <T> List<T>.get(range: IntRange): List<T> = this.subList(max(0, range.first), min(this.size, range.last))


/**
 * Add an element to a mutable collection, but only if it is not null
 *
 * @param T
 * @param elem The element to be added
 * @return True is the collection has been modified, false otherwise
 */
fun <T> MutableCollection<T>.addNotNull(elem: T?) : Boolean {
    return if (elem !== null) add(elem) else false
}

/**
 * Get index of the time that is closets to the provided time
 * but doesn't exceed it. So it is the most recent time but without
 * looking into the future.
 *
 * If no such time is found return null
 *
 * @param now
 * @return
 */
fun List<Instant>.latestNotAfter(now: Instant): Int? {
    var idx = binarySearch(now)
    idx = if (idx < 0) -idx - 2 else idx
    return if (idx >= 0) idx else null
}

fun List<Instant>.earliestNotBefore(now: Instant): Int? {
    var idx = binarySearch(now)
    idx = if (idx < 0) -idx - 1 else idx
    return if (idx < size) idx else null
}

/**
 * Get the timeframe for this timeline
 */
val List<Instant>.timeFrame
    get() = TimeFrame(first(), last() + 1)


fun List<Instant>.split(period: Period): List<TimeFrame> {
    val result = mutableListOf<TimeFrame>()
    val utc = ZoneOffset.UTC
    var start = first()
    var stop = (LocalDateTime.ofInstant(start, utc) + period).toInstant(utc)
    for (now in this) {
        if (now > stop) {
            val tf = TimeFrame(start, now)
            result.add(tf)
            start = now
            stop = (LocalDateTime.ofInstant(start, utc) + period).toInstant(utc)
        }
    }
    val tf = TimeFrame(start, stop)
    result.add(tf)
    return result
}

fun List<Instant>.split(size: Int): List<TimeFrame> {
    return chunked(size).map { TimeFrame(it.first(), it.last()) }
}


/***********************************************************
 * Make working with Double Arrays a bit more fun
 ***********************************************************/


operator fun DoubleArray.div(a: Number): DoubleArray {
    val result = clone()
    val n = a.toDouble()
    for (i in 0 until size) result[i] /= n
    return result
}


fun DoubleArray.max(): Double {
    return Max().evaluate(this)
}

fun DoubleArray.min(): Double {
    return Min().evaluate(this)
}

fun DoubleArray.mean(): Double {
    return Mean().evaluate(this)
}

fun DoubleArray.std(): Double {
    return StandardDeviation().evaluate(this)
}

fun DoubleArray.variance(): Double {
    return Variance().evaluate(this)
}

fun DoubleArray.skewness(): Double {
    return Skewness().evaluate(this)
}

fun DoubleArray.kurtosis(): Double {
    return Kurtosis().evaluate(this)
}

/**
 * Remove non-finite values from a DoubleArray and return this new array. The removed values include Inf and NaN values.
 */
fun DoubleArray.clean() = filter { it.isFinite() }.toDoubleArray()


fun Pair<List<Double>, List<Double>>.clean(): Pair<DoubleArray, DoubleArray> {
    val max = max(first.size, second.size)
    val r1 = mutableListOf<Double>()
    val r2 = mutableListOf<Double>()
    for (i in 0 until max) {
        if (first[i].isFinite() && second[i].isFinite()) {
            r1.add(first[i])
            r2.add(second[i])
        }
    }
    return Pair(r1.toDoubleArray(), r2.toDoubleArray())
}


fun String?.escapeHtml() : String  = StringEscapeUtils.escapeHtml4(this ?: "")
