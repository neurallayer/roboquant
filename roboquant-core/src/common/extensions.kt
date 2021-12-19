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

package org.roboquant.common

import org.apache.commons.math3.stat.descriptive.moment.*
import org.apache.commons.math3.stat.descriptive.rank.Max
import org.apache.commons.math3.stat.descriptive.rank.Min
import org.apache.commons.text.StringEscapeUtils
import java.lang.Integer.max
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import kotlin.math.pow

/********************************************************************************************************************
 * This file contains the extensions for classes that are part of standard Java and Kotlin libraries. Extensions for
 * classes that are part of roboquant should not be included in this file.
 *******************************************************************************************************************/

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


fun Collection<String>.summary(header: String = "Values"): Summary {
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
fun <T> MutableCollection<T>.addNotNull(elem: T?): Boolean {
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
 * Get the timeframe for this timeline, it assumes a sorted list
 */
val List<Instant>.timeFrame
    get() = TimeFrame(first(), last() + 1)

fun List<Instant>.split(period: Period): List<TimeFrame> {
    val result = mutableListOf<TimeFrame>()
    val zone = ZoneOffset.UTC
    var start = first()
    var stop = (LocalDateTime.ofInstant(start, zone) + period).toInstant(zone)
    for (now in this) {
        if (now > stop) {
            val tf = TimeFrame(start, now)
            result.add(tf)
            start = now
            stop = (LocalDateTime.ofInstant(start, zone) + period).toInstant(zone)
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
    for (i in indices) result[i] /= n
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


fun String?.escapeHtml(): String = StringEscapeUtils.escapeHtml4(this ?: "")


/**
 * Provide a [Summary] for a collection of any objects
 */
fun Collection<Any?>.summary(): Summary {
    val result = Summary("${this.javaClass.simpleName}s")
    for (obj in this) result.add(obj?.toString() ?: "NULL")
    return result
}

fun Number.round(fractions: Int = 2): BigDecimal = BigDecimal(toDouble()).setScale(fractions, RoundingMode.HALF_DOWN)


/**
 * Annualize an amount based on the duration of this time frame.
 */
fun Number.annualize(tf: TimeFrame): Double {
    val period = tf.duration.toMillis()
    val years = (365.0 * 24.0 * 3600.0 * 1000.0) / period
    return (1.0 + toDouble()).pow(years) - 1.0
}

/**
 * Convert a string to a currency pair. Return null if not possible
 */
fun String.toCurrencyPair() : Pair<Currency, Currency>? {
    val codes = split('_', '-', ' ', '/', ':')
    if (codes.size == 2) {
        val c1 = Currency.getInstance(codes.first().uppercase())
        val c2 = Currency.getInstance(codes.last().uppercase())
        return Pair(c1, c2)
    } else if (codes.size == 1 && length == 6) {
        val c1 = Currency.getInstance(substring(0, 3).uppercase())
        val c2 = Currency.getInstance(substring(3, 6).uppercase())
        return Pair(c1, c2)
    }
    return null
}



/*********************************************************************************************
 * Extensions on Integer type to make instantiation of periods or duration more convenient
 *********************************************************************************************/


val Int.years
    get() = Period.ofYears(this)

val Int.days
    get() = Period.ofDays(this)

val Int.months
    get() = Period.ofMonths(this)

val Int.weeks
    get() = Period.ofWeeks(this)

val Int.hours
    get() = Duration.ofHours(this.toLong())

val Int.minutes
    get() = Duration.ofMinutes(this.toLong())

val Int.seconds
    get() = Duration.ofSeconds(this.toLong())

val Int.millis
    get() = Duration.ofMillis(this.toLong())