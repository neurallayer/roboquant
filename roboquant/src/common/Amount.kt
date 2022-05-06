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
import java.math.RoundingMode
import java.time.Instant
import kotlin.math.absoluteValue

/**
 * An amount can hold the [value] for a single [currency].
 *
 * For storing monetary amounts internally it uses [Double], since it is accurate enough for trading while providing
 * large performance benefits over BigDecimal.
 */
data class Amount(val currency: Currency, val value: Double) : Comparable<Number> {

    constructor(currency: Currency, value: Number) : this(currency, value.toDouble())
    constructor(currencyCode: String, value: Number) : this(Currency.getInstance(currencyCode), value.toDouble())

    // Common operators that make working with Amounts more pleasant
    operator fun times(d: Number): Amount = Amount(currency, value * d.toDouble())
    operator fun plus(d: Number): Amount = Amount(currency, value + d.toDouble())
    operator fun div(d: Number): Amount = Amount(currency, value / d.toDouble())
    operator fun minus(d: Number): Amount = Amount(currency, value - d.toDouble())
    operator fun plus(other: Amount): Wallet = Wallet(this, other)
    operator fun minus(other: Amount): Wallet = Wallet(this, -other)
    operator fun unaryMinus(): Amount = Amount(currency, -value)

    /**
     * Does this amount contain a positive value
     */
    val isPositive: Boolean
        get() = value > 0.0

    /**
     * Return a new amount containing the absolute value of this amount.
     */
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
        BigDecimal.valueOf(value).setScale(fractionDigits, RoundingMode.HALF_DOWN)

    override fun toString(): String = "${currency.currencyCode} ${formatValue()}"

    /**
     * Compare the [value] in this amount to an [other] number
     */
    override fun compareTo(other: Number): Int = value.compareTo(other.toDouble())

    /**
     * Convert this amount [to] a different currency. If no currency is provided, the [Config.baseCurrency] is used.
     * Optional you can provide a [time] at which the conversion should be calculated. If no time is proviedd the
     * current time is used.
     */
    fun convert(to: Currency = Config.baseCurrency, time: Instant = Instant.now()): Amount {
        return Config.exchangeRates.convert(this, to, time)
    }

    /**
     * Convert this amount to a [Wallet]
     */
    fun toWallet(): Wallet {
        return Wallet(this)
    }

}

// Extensions to make it easier to create amounts for common currencies
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

val Number.RUB
    get() = Amount(Currency.RUB, toDouble())

val Number.INR
    get() = Amount(Currency.INR, toDouble())

val Number.BTC
    get() = Amount(Currency.BTC, toDouble())

val Number.ETH
    get() = Amount(Currency.ETH, toDouble())

val Number.USDT
    get() = Amount(Currency.USDT, toDouble())

/**
 * Add all the amounts together and return the resulting wallet.
 */
fun Iterable<Amount>.sum(): Wallet {
    val result = Wallet()
    for (amount in this) result.deposit(amount)
    return result
}

