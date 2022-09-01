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
@file:Suppress("TooManyFunctions")

package org.roboquant.common

import org.apache.commons.math3.stat.descriptive.moment.*
import org.apache.commons.math3.stat.descriptive.rank.Max
import org.apache.commons.math3.stat.descriptive.rank.Min
import java.lang.Integer.max
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit
import kotlin.math.absoluteValue

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
 * Compare an instant to a [timeframe].
 */
operator fun Instant.compareTo(timeframe: Timeframe): Int {
    return if (this >= timeframe.end) 1 else if (this < timeframe.start) -1 else 0
}

/**
 * Subtract a number of [millis] seconds to the instant
 */
operator fun Instant.minus(millis: Int): Instant = minusMillis(millis.toLong())

/**
 * Get the instant as ZonedDateTime UTC
 */
fun Instant.toUTC(): ZonedDateTime = atZone(ZoneId.of("UTC"))

fun Collection<String>.summary(header: String = "values"): Summary {
    val result = Summary(header)
    forEach { result.add(it) }
    return result
}

/**
 * Extension function to allow *numpy* like indexing for lists. In order to stay close to Kotlin, the end value is
 * inclusive. Returns a view on the original list, so no copy is made.
 *
 * ## Example
 *
 *      a = someList[0..10]
 *
 * @param T
 * @param range
 * @return
 */
operator fun <T> List<T>.get(range: IntRange): List<T> = subList(max(0, range.first), min(this.size, range.last + 1))

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

operator fun DoubleArray.times(a: Number): DoubleArray {
    val result = clone()
    val n = a.toDouble()
    for (i in indices) result[i] *= n
    return result
}

operator fun DoubleArray.minus(a: Number): DoubleArray {
    val result = clone()
    val n = a.toDouble()
    for (i in indices) result[i] -= n
    return result
}

/**
 * Add a [number] to all elements in a double array and return the result
 */
operator fun DoubleArray.plus(number: Number): DoubleArray {
    val result = clone()
    val n = number.toDouble()
    for (i in indices) result[i] += n
    return result
}

/**
 * Subtract two arrays
 */
operator fun DoubleArray.minus(a: DoubleArray): DoubleArray {
    require(a.size == size) { "Arrays have to be of equal size" }
    val result = clone()
    for (i in indices) result[i] -= a[i]
    return result
}

/**
 * Multiply two arrays
 */
operator fun DoubleArray.times(a: DoubleArray): DoubleArray {
    require(a.size == size) { "Arrays have to be of equal size" }
    val result = clone()
    for (i in indices) result[i] *= a[i]
    return result
}

/**
 * Divide two arrays
 */
operator fun DoubleArray.div(a: DoubleArray): DoubleArray {
    require(a.size == size) { "Arrays have to be of equal size" }
    val result = clone()
    for (i in indices) result[i] /= a[i]
    return result
}

/**
 * Add two arrays
 */
operator fun DoubleArray.plus(a: DoubleArray): DoubleArray {
    require(a.size == size) { "Arrays have to be of equal size" }
    val result = clone()
    for (i in indices) result[i] += a[i]
    return result
}

/**
 * Return the max value from the array
 */
fun DoubleArray.max(): Double {
    return Max().evaluate(this)
}

/**
 * Return the min value from the array
 */
fun DoubleArray.min(): Double {
    return Min().evaluate(this)
}

/**
 * Return the mean value from the array
 */
fun DoubleArray.mean(): Double {
    return Mean().evaluate(this)
}

/**
 * Return the standard deviation value from the array
 */
fun DoubleArray.std(): Double {
    return StandardDeviation().evaluate(this)
}

/**
 * Return the variance value from the array
 */
fun DoubleArray.variance(): Double {
    return Variance().evaluate(this)
}

/**
 * Return the skewness value from the array
 */
fun DoubleArray.skewness(): Double {
    return Skewness().evaluate(this)
}

/**
 * Return the kurtosis value from the array
 */
fun DoubleArray.kurtosis(): Double {
    return Kurtosis().evaluate(this)
}

/**
 * Remove non-finite values from a DoubleArray and return this new array. The removed values include Inf and NaN values.
 */
fun DoubleArray.clean() = filter { it.isFinite() }.toDoubleArray()

/**
 * Get the returns (as a percentage). Formula used is
 *
 *      returns = (new -old) / old
 */
fun DoubleArray.returns(): DoubleArray {
    if (size < 2) return DoubleArray(0)
    val result = DoubleArray(size - 1)
    for (n in 1..lastIndex) result[n - 1] = (get(n) - get(n - 1)) / get(n - 1)
    return result
}

/**
 * Get the total return (as a percentage). Formula used is
 *
 *      returns = (last - first) / first
 */
fun DoubleArray.totalReturns(): Double {
    return if (size < 2)
        0.0
    else
        (last() - first()) / first()
}

// Values smaller than this value are considered to be zero
private const val EPS = 0.0000001

/**
 * Is this value zero, allows for small rounding errors.
 */
val Double.iszero
    get() = this.absoluteValue < EPS

/**
 * is this a non-zero number, allows for small rounding errors.
 */
val Double.nonzero
    get() = this.absoluteValue >= EPS

/**
 * is this a non-zero number, allows for small rounding errors.
 */
val Double.zeroOrMore
    get() = if (this >= EPS) this else 0.0

/**
 * Return a rounded number with the specified number of fractions as a BigDecimal
 */
fun Number.round(fractions: Int = 2): BigDecimal =
    BigDecimal.valueOf(toDouble()).setScale(fractions, RoundingMode.HALF_DOWN)

/**
 * Convert a string to a currency pair. Return null if not succeeded.
 */
fun String.toCurrencyPair(): Pair<Currency, Currency>? {
    val codes = split('_', '-', ' ', '/', ':')
    return if (codes.size == 2) {
        val c1 = Currency.getInstance(codes.first().uppercase())
        val c2 = Currency.getInstance(codes.last().uppercase())
        Pair(c1, c2)
    } else if (codes.size == 1 && length == 6) {
        val c1 = Currency.getInstance(substring(0, 3).uppercase())
        val c2 = Currency.getInstance(substring(3, 6).uppercase())
        Pair(c1, c2)
    } else {
        null
    }
}

/**
 * Extension to use sumOf for [Amount]. This implementation has an optimized implementation in case the sum is over
 * amounts of a single currency.
 */
inline fun <T> Collection<T>.sumOf(
    selector: (T) -> Amount
): Wallet {
    val result = Wallet()
    if (isEmpty()) return result
    val currency = selector(first()).currency
    var value = 0.0
    var singleCurrency = true
    forEach {
        val amount = selector(it)
        if (singleCurrency) {
            if (amount.currency == currency) {
                value += amount.value
            } else {
                singleCurrency = false
                result.deposit(Amount(currency, value))
                result.deposit(amount)
            }
        } else {
            result.deposit(amount)
        }
    }
    if (singleCurrency) result.deposit(Amount(currency, value))
    return result
}

/*********************************************************************************************
 * Extensions on Integer type to make instantiation of periods or duration more convenient
 *********************************************************************************************/

/*
val Int.years : Period
    get() = Period.ofYears(this)

val Int.months : Period
    get() = Period.ofMonths(this)

val Int.weeks : Period
    get() = Period.ofWeeks(this)

val Int.days : Period
    get() = Period.ofDays(this)

val Int.hours : Duration
    get() = Duration.ofHours(this.toLong())

val Int.minutes : Duration
    get() = Duration.ofMinutes(this.toLong())

val Int.seconds : Duration
    get() = Duration.ofSeconds(this.toLong())

val Int.millis : Duration
    get() = Duration.ofMillis(this.toLong())
*/

/**
 * Unified approach to the different types of temporalAmounts found in Java: Period and Duration.
 */
class ZonedPeriod(private val a: TemporalAmount, private val zoneId: ZoneId = Config.defaultZoneId) : TemporalAmount {

    override fun get(unit: TemporalUnit): Long {
        return a.get(unit)
    }

    override fun getUnits(): MutableList<TemporalUnit> {
        return a.units
    }

    fun toDays() = a.get(ChronoUnit.DAYS)

    fun toHours() = (a as Duration).toHours()

    fun toMinutes() = (a as Duration).toMinutes()

    override fun addTo(temporal: Temporal): Temporal {
        val result = Instant.from(temporal).atZone(zoneId).plus(a)
        return if (temporal is Instant) result.toInstant() else result
    }

    override fun subtractFrom(temporal: Temporal): Temporal {
        val result = Instant.from(temporal).atZone(zoneId).minus(a)
        return if (temporal is Instant) result.toInstant() else result
    }

    fun atZone(zoneId: ZoneId): ZonedPeriod {
        return ZonedPeriod(a, zoneId)
    }

}

/**
 * Convert number to years
 */
val Number.years: ZonedPeriod
    get() = ZonedPeriod(Period.ofYears(this.toInt()))

/**
 * Convert number to months
 */
val Number.months: ZonedPeriod
    get() = ZonedPeriod(Period.ofMonths(this.toInt()))

/**
 * Convert number to weeks
 */
val Number.weeks: ZonedPeriod
    get() = ZonedPeriod(Period.ofWeeks(this.toInt()))

/**
 * Convert number to days
 */
val Number.days: ZonedPeriod
    get() = ZonedPeriod(Period.ofDays(this.toInt()))

/**
 * Convert number to hours
 */
val Number.hours: ZonedPeriod
    get() = ZonedPeriod(Duration.ofHours(this.toLong()))

/**
 * Convert number to minutes
 */
val Number.minutes: ZonedPeriod
    get() = ZonedPeriod(Duration.ofMinutes(this.toLong()))

/**
 * Convert number to seconds
 */
val Number.seconds: ZonedPeriod
    get() = ZonedPeriod(Duration.ofSeconds(this.toLong()))

/**
 * Convert number to millis
 */
val Number.millis: ZonedPeriod
    get() = ZonedPeriod(Duration.ofMillis(this.toLong()))

