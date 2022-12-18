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

import org.hipparchus.stat.descriptive.moment.*
import org.hipparchus.stat.descriptive.rank.Max
import org.hipparchus.stat.descriptive.rank.Min
import org.hipparchus.stat.descriptive.rank.Percentile
import java.lang.Integer.max
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import kotlin.math.absoluteValue

/********************************************************************************************************************
 * This file contains the extensions for classes that are part of standard Java and Kotlin libraries. Extensions for
 * classes that are part of roboquant should not be included in this file.
 *******************************************************************************************************************/

/**
 * Compare an instant to a [timeframe]. This operator takes into account if the timeframe is inclusive or not of the
 * defined end date.
 */
operator fun Instant.compareTo(timeframe: Timeframe): Int {
    return when(timeframe.inclusive) {
        true -> if (this > timeframe.end) 1 else if (this < timeframe.start) -1 else 0
        false -> if (this >= timeframe.end) 1 else if (this < timeframe.start) -1 else 0
    }
}

/**
 * Get the instant as ZonedDateTime UTC
 */
fun Instant.toUTC(): ZonedDateTime = atZone(ZoneId.of("UTC"))

/**
 * Create a summary for a collection of strings
 */
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

/**
 * Extension function to allow *numpy* like indexing for lists, for example someList[0..10..2]
 */
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

/**
 * Divide all elements in the array by [a] number
 */
operator fun DoubleArray.div(a: Number): DoubleArray {
    val result = clone()
    val n = a.toDouble()
    for (i in indices) result[i] /= n
    return result
}

/**
* Multiple all elements in the array by [a] number
*/
operator fun DoubleArray.times(a: Number): DoubleArray {
    val result = clone()
    val n = a.toDouble()
    for (i in indices) result[i] *= n
    return result
}

/**
* Subtract all elements in the array by [a] number
*/
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
 * Return the percentage change.  The resulting array size will be 1 smaller than the original one. Formula used is:
 *
 *      returns = (new - old) / old
 */
fun DoubleArray.percentageChange(): DoubleArray {
    if (size < 2) return DoubleArray(0)
    val result = DoubleArray(size - 1)
    for (n in 1..lastIndex) result[n - 1] = (get(n) - get(n - 1)) / get(n - 1)
    return result
}


/**
 * return the min and max values and [low], [mid] and [high] percentile. Passed percentiles should be between
 * 0.0 and 100.0. The default values are respectively: 5.0, 50.0 and 95.0
 */
fun DoubleArray.percentiles(
    low: Double = 5.0,
    mid: Double = 50.0,
    high: Double = 95.0
): List<Double> {
    val p = Percentile()
    p.data = this

    return listOf(
        min(),
        p.evaluate(low),
        p.evaluate(mid),
        p.evaluate(high),
        max()
    )
}


/**
 * Get the total percentage change. Formula used is
 *
 *      returns = (last - first) / first
 */
fun DoubleArray.totalPercentageChange(): Double {
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
 * Return a rounded number with the specified number of fractions as a BigDecimal
 */
fun Number.round(fractions: Int = 2): BigDecimal =
    BigDecimal.valueOf(toDouble()).setScale(fractions, RoundingMode.HALF_DOWN)

/**
 * Convert a string to a currency pair. Returns null if it could not determine the currencies.
 */
fun String.toCurrencyPair(): Pair<Currency, Currency>? {
    val codes = split('_', '-', ' ', '/', ':')
    return if (codes.size == 2) {
        val c1 = Currency.getInstance(codes.first().uppercase())
        val c2 = Currency.getInstance(codes.last().uppercase())
        Pair(c1, c2)
    } else if (codes.size == 1 && length == 6) {
        // Assume we have two currency-codes, each of length 3
        val c1 = Currency.getInstance(substring(0, 3).uppercase())
        val c2 = Currency.getInstance(substring(3, 6).uppercase())
        Pair(c1, c2)
    } else {
        null
    }
}

/**
 * Extension to use sumOf for [Amount] values. This implementation has an optimized path in case the sum is over
 * amounts of a single currency.
 *
 * example:
 *
 *      val realizedPNL = account.trades.sumOf { it.pnl }
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
            if (amount.currency === currency) {
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
