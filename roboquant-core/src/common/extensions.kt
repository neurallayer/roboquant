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
import java.lang.Integer.max
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.*
import kotlin.math.absoluteValue
import kotlin.math.round

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

/**
 * Add a numer of [millis] seconds to the instant
 */
operator fun Instant.plus(millis: Int): Instant = plusMillis(millis.toLong())

/**
 * Compare an instant to a [timeframe].
 */
operator fun Instant.compareTo(timeframe: Timeframe): Int {
    return if (this >= timeframe.end) 1 else if (this < timeframe.start) -1 else 0
}


/**
 * Subtract a numer of [millis] seconds to the instant
 */
operator fun Instant.minus(millis: Int): Instant = minusMillis(millis.toLong())

/**
 * Get the instant as ZonedDateTime UTC
 */
fun Instant.toUTC(): ZonedDateTime = atZone(ZoneId.of("UTC"))


fun Collection<String>.summary(header: String = "Values"): Summary {
    val result = Summary(header)
    forEach { result.add(it) }
    return result
}

/**
 * Extension function to allow *numpy* like indexing for lists.
 *
 * ## Example
 *
 *      a = someList[0..10]
 *
 * @param T
 * @param range
 * @return
 */
operator fun <T> List<T>.get(range: IntRange): List<T> = subList(max(0, range.first), min(this.size, range.last+1))


operator fun IntRange.rangeTo(i: Int): IntProgression = IntProgression.fromClosedRange(first, last, i)

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

/**
 * Get the returns (as a percentage). Forumla used is
 *
 *      returns = (new -old) / old
 */
fun DoubleArray.returns() : DoubleArray {
    if (size < 2) return DoubleArray(0)
    val result = DoubleArray(size - 1)
    for (n in 1..lastIndex) result[n-1] = (get(n) - get(n-1)) / get(n-1)
    return result
}


private const val EPS = 0.0000001

/**
 * Is this value zero, allows for small rounding errors.
 */
val Double.iszero
    get() = this.absoluteValue < EPS

/**
 * is this a non zero number, allows for small rounding errors.
 */
val Double.nonzero
    get() = this.absoluteValue > EPS

/**
 * is this a non zero number, allows for small rounding errors.
 */
val Double.zeroOrMore
    get() = if (this > EPS) this else 0.0


fun Number.round(fractions: Int = 2): BigDecimal = BigDecimal.valueOf(toDouble()).setScale(fractions, RoundingMode.HALF_DOWN)


/**
 * Convert a Double as a rounded positive integer.
 */
val Double.absInt: Int
    get() = round(this).toInt().absoluteValue


/**
 * Deals with nicely formatting fractional quantities. Don't use decimals if not required.
 */
val Double.asQuantity : BigDecimal
    get() {
        val pf = DecimalFormat("############")
        pf.minimumFractionDigits = 0
        pf.maximumFractionDigits = 4
        return pf.format(this).toBigDecimal()
    }


/**
 * Try to convert a string to a currency pair. Return null if not successed
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


/**
 * Extesnion to use sumOf for Amounts
 */
inline fun <T> Iterable<T>.sumOf(
    selector: (T) -> Amount
): Wallet {
    val result = Wallet()
    forEach {
        val amount = selector(it)
        result.deposit(amount)
    }
    return result
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
