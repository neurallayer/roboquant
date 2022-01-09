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

@file:Suppress("unused")

package org.roboquant.common

import java.math.BigDecimal
import java.time.Instant
import kotlin.math.absoluteValue

data class Amount(val currency: Currency, val value: Double) : Comparable<Number> {

    operator fun times(d: Number): Amount = Amount(currency, value * d.toDouble())
    operator fun plus(d: Number): Amount = Amount(currency, value + d.toDouble())
    operator fun div(d: Number): Amount = Amount(currency, value / d.toDouble())
    operator fun minus(d: Number): Amount = Amount(currency, value - d.toDouble())
    operator fun plus(other: Amount): Wallet = Wallet(this, other)

    val absoluteValue
        get() = Amount(currency, value.absoluteValue)


    /**
     * Format the value hold in this amount based on the currency. For example USD would have two fraction digits
     * by default while JPY would have none.
     */
    fun formatValue(fractionDigits: Int = currency.defaultFractionDigits) = toBigDecimal(fractionDigits).toString()

    /**
     * Convert the value to BigDecimal using the number of digits defined for the currency. Internally roboquant
     * doesn't use BigDecimals, but this method is used to enable a nicer display of currency amounts.
     */
    fun toBigDecimal(fractionDigits: Int = currency.defaultFractionDigits): BigDecimal =
        BigDecimal(value).setScale(fractionDigits, Currency.roundingMode)

    override fun toString(): String = "${currency.currencyCode} ${formatValue()}"

    override fun compareTo(other: Number): Int =  value.compareTo(other.toDouble())

    /**
     * Convert this amount [to] a different currency. If no currency is provided, the [Config.baseCurrency] is used.
     * Optional you can provide a [time] at which the conversion should be calculated. If no time is proviedd the
     * current time is used.
     */
    fun convert(to: Currency = Config.baseCurrency, time: Instant = Instant.now()): Amount {
        return Config.exchangeRates.convert(this, to, time)
    }

}

// Some extensions to make it easier to create amounts for common currencies
val Number.EUR
    get() = Amount(Currency.EUR, toDouble())

val Number.USD
    get() = Amount(Currency.USD, toDouble())

val Number.JPY
    get() = Amount(Currency.JPY, toDouble())

val Number.GBP
    get() = Amount(Currency.GBP, toDouble())

val Number.CHF
    get() = Amount(Currency.CHF, toDouble())

val Number.AUD
    get() = Amount(Currency.AUD, toDouble())

val Number.CAD
    get() = Amount(Currency.CAD, toDouble())

val Number.CNY
    get() = Amount(Currency.CNY, toDouble())

val Number.HKD
    get() = Amount(Currency.HKD, toDouble())

val Number.NZD
    get() = Amount(Currency.NZD, toDouble())

fun Iterable<Amount>.sum(): Wallet {
    val result = Wallet()
    for (amount in this) result.deposit(amount)
    return result
}
