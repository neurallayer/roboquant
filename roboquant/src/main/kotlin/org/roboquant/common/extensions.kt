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
@file:Suppress("TooManyFunctions")

package org.roboquant.common

import kotlinx.coroutines.delay
import org.roboquant.common.Config.EPS
import java.lang.Integer.max
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.ln


/********************************************************************************************************************
 * This file contains the extensions for classes that are part of standard Java and Kotlin libraries. Extensions for
 * classes that are part of roboquant should not be included in this file.
 *******************************************************************************************************************/

/**
 * Compare an instant to a [timeframe]. This operator takes into account if the timeframe is inclusive or not of the
 * defined end date.
 */
operator fun Instant.compareTo(timeframe: Timeframe): Int {
    return when (timeframe.inclusive) {
        true -> if (this > timeframe.end) 1 else if (this < timeframe.start) -1 else 0
        false -> if (this >= timeframe.end) 1 else if (this < timeframe.start) -1 else 0
    }
}

/**
 * Delay until this time is reached
 */
suspend fun Instant.delayUntil() {
    val now = Instant.now()
    delay(now.until(this, ChronoUnit.MILLIS).coerceAtLeast(0L))
}

/**
 * Get the instant as ZonedDateTime UTC
 */
fun Instant.toUTC(): ZonedDateTime = atZone(ZoneOffset.UTC)

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
 *```
 * val a = someList[0..10]
 *```
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
 * Remove non-finite values from a DoubleArray and return this new array. The removed values include Inf and NaN values.
 */
fun DoubleArray.clean() = filter { it.isFinite() }.toDoubleArray()

/**
 * Return the returns over the provided [n] period.
 * The resulting array size will be one smaller than the original.
 * Formula used is:
 * ```
 * return = new/old - 1.0
 * ```
 */
fun DoubleArray.returns(n: Int = 1): DoubleArray {
    if (size <= n) return DoubleArray(0)
    val result = DoubleArray(size - n)
    for (i in n..lastIndex) result[i - n] = get(i) / get(i - n) - 1.0
    return result
}

/**
 * Returns an array with the difference between the elements over the provided [n] period.
 */
fun DoubleArray.diff(n: Int = 1): DoubleArray {
    if (size <= n) return DoubleArray(0)
    val result = DoubleArray(size - n)
    for (i in n..lastIndex) result[i - n] = get(i) - get(i - n)
    return result
}

/**
 * Returns the index of the first maximum value
 */
fun DoubleArray.indexOfMax(): Int {
    if (isEmpty()) return -1
    var maxAt = 0
    for (i in indices) if (get(i) > get(maxAt)) maxAt = i
    return maxAt
}

/**
 * Allows manipulating strings as if they were paths. It will automatically normalize the path if required.
 *
 * Example:
 * ```
 * val feed = CSVFeed("some-dir" / "some-sub-dir")
 * ```
 */
operator fun String.div(other: String): String {
    val p = java.nio.file.Path.of(this, other).normalize()
    return p.toString()
}

/**
 * Returns the index of the first minimum value
 */
fun DoubleArray.indexOfMin(): Int {
    if (isEmpty()) return -1
    var minAt = 0
    for (i in indices) if (get(i) < get(minAt)) minAt = i
    return minAt
}

/**
 * Return the growth rates.
 * The resulting array size will be one smaller than the original.
 * Formula used is:
 * ```
 * growthRate = new/old
 * ```
 */
fun DoubleArray.growthRates(n: Int = 1): DoubleArray {
    if (size <= n) return DoubleArray(0)
    val result = DoubleArray(size - n)
    for (i in n..lastIndex) result[i - n] = get(i) / get(i - n)
    return result
}

/**
 * Return the log growth rates.
 * The resulting array size will be one smaller than the original.
 * The used formula is:
 *```
 * logGrowthRate = ln(new/old)
 * ```
 */
fun DoubleArray.logGrowthRates(n: Int = 1): DoubleArray {
    if (size <= n) return DoubleArray(0)
    val result = DoubleArray(size - n)
    for (i in 1..lastIndex) result[i - n] = ln(get(i) / get(i - n))
    return result
}

/**
 * Get the total return. Formula used is
 * ```
 * return = last/first - 1.0
 * ```
 */
fun DoubleArray.totalReturn(): Double {
    return if (size < 2)
        0.0
    else
        last() / first() - 1.0
}


/**
 * Is this value zero, this implementation allows for small rounding errors.
 */
val Double.iszero: Boolean
    get() = this.absoluteValue < EPS

/**
 * Return true if this is a non-zero number, allows for small rounding errors.
 */
val Double.nonzero: Boolean
    get() = this.absoluteValue >= EPS

/**
 * The number as percentage. For example, `10.percent` equals `0.01`
 */
val Number.percent: Double
    get() = this.toDouble() / 100.0

/**
 * The number as bips. For example, `10.bips` equals `0.0001`
 */
val Number.bips: Double
    get() = this.toDouble() / 10_000.0

/**
 * Return a rounded number with the specified number of fractions as a BigDecimal
 */
fun Number.round(fractions: Int = 2): BigDecimal =
    BigDecimal.valueOf(toDouble()).setScale(fractions, RoundingMode.HALF_DOWN)

/**
 * Convert a string to a currency pair. Returns null if it could not determine the currencies.
 */
fun String.toCurrencyPair(): Pair<Currency, Currency>? {
    val codes = split('_', '-', ' ', '/', '.', ':')
    return if (codes.size == 2) {
        val c1 = Currency.getInstance(codes.first().uppercase())
        val c2 = Currency.getInstance(codes.last().uppercase())
        Pair(c1, c2)
    } else if (codes.size == 1 && length == 6) {
        // Assume we have two currency-codes, each of lengths 3
        val c1 = Currency.getInstance(substring(0, 3).uppercase())
        val c2 = Currency.getInstance(substring(3, 6).uppercase())
        Pair(c1, c2)
    } else {
        null
    }
}

/**
 * Extension to use sumOf for [Amount] values.
 * The result is of type [Wallet], even if summing over a single currency
 *
 * This implementation has an optimized path in case the sum is over amounts of a single currency.
 *
 * Example:
 * ```
 * val totalPNL = account.trades.sumOf { it.pnl }
 * ```
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
    return if (singleCurrency) Amount(currency, value).toWallet() else result
}
