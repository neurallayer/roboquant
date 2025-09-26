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

import org.roboquant.brokers.ExchangeRates
import org.roboquant.brokers.NoExchangeRates
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.util.*
import kotlin.math.absoluteValue

/**
 * An amount holds the monetary [value] for a single [currency].
 *
 * For storing monetary amounts internally it uses [Double], since it is accurate enough for trading while providing
 * performance benefits over types like BigDecimal.
 *
 * @property currency the currency of the amount
 * @property value the value of the amount
 */
class Amount(val currency: Currency, val value: Double)  {

    /**
     * Create an Amount instance based on provided [currency] and [value].
     * The value will be converted to a [Double], which might involve rounding.
     */
    constructor(currency: Currency, value: Number) : this(currency, value.toDouble())

    /**
     * Create an Amount instance based on provided [currencyCode] and [value].
     * The value will be converted to a [Double], which might involve rounding.
     */
    constructor(currencyCode: String, value: Number) : this(Currency.getInstance(currencyCode), value.toDouble())

    // Common operators that make working with Amounts more convenient

    /** @suppress */
    operator fun times(d: Number): Amount = Amount(currency, value * d.toDouble())

    /** @suppress */
    operator fun plus(d: Number): Amount = Amount(currency, value + d.toDouble())

    /** @suppress */
    operator fun div(d: Number): Amount = Amount(currency, value / d.toDouble())

    /** @suppress */
    operator fun minus(d: Number): Amount = Amount(currency, value - d.toDouble())

    /** @suppress */
    operator fun plus(other: Amount): Wallet = Wallet(this, other)

    /** @suppress */
    operator fun minus(other: Amount): Wallet = Wallet(this, -other)

    /** @suppress */
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
     * Format the value hold in this amount based on the currency. For example, USD would have two fraction digits
     * by default while JPY would have none.
     */
    fun formatValue(fractionDigits: Int = currency.defaultFractionDigits): String {
        // We don't use default locale to make output more reproducible
        val formatEN = NumberFormat.getInstance(Locale.ENGLISH)
        formatEN.minimumFractionDigits = fractionDigits
        formatEN.maximumFractionDigits = fractionDigits
        return formatEN.format(value)
    }

    /**
     * Convert the value to BigDecimal using the number of digits defined for the currency. Internally, roboquant
     * doesn't use BigDecimals, but this method is used to enable a nicer display of currency amounts.
     */
    fun toBigDecimal(fractionDigits: Int = currency.defaultFractionDigits): BigDecimal =
        BigDecimal.valueOf(value).setScale(fractionDigits, RoundingMode.HALF_DOWN)

    /** @suppress **/
    override fun toString(): String = "${currency.currencyCode} ${formatValue()}"

    /**
     * Convert this amount [to] a different currency. Optional you can provide a [time] at which the conversion
     * should be calculated. If no time is provided, the current time is used.
     */
    fun convert(to: Currency, time: Instant): Amount {
        if (currency == to) return this
        if (value == 0.0) return Amount(to, 0.0)
        return converter.convert(this, to, time)
    }

    /** @suppress **/
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Amount) return false
        if (currency != other.currency) return false
        return value == other.value
    }

    /** @suppress **/
    override fun hashCode(): Int = 31 * currency.hashCode() + value.hashCode()

    /**
     * Convert this amount to a [Wallet] instance.
     */
    fun toWallet(): Wallet {
        return Wallet(this)
    }

    companion object {

        var converter: ExchangeRates = NoExchangeRates()

        fun registerConvertor(converter: ExchangeRates) {
            Amount.converter = converter

        }
    }


}

// Extensions to make it easier to create amounts for common currencies

/**
 * Amount in [Currency.EUR]
 */
val Number.EUR
    get() = Amount(Currency.EUR, toDouble())

/**
 * Amount in [Currency.USD]
 */
val Number.USD
    get() = Amount(Currency.USD, toDouble())

/**
 * Amount in [Currency.JPY]
 */
val Number.JPY
    get() = Amount(Currency.JPY, toDouble())

/**
 * Amount in [Currency.GBP]
 */
val Number.GBP
    get() = Amount(Currency.GBP, toDouble())

/**
 * Amount in [Currency.CHF]
 */
val Number.CHF
    get() = Amount(Currency.CHF, toDouble())

/**
 * Amount in [Currency.AUD]
 */
val Number.AUD
    get() = Amount(Currency.AUD, toDouble())

/**
 * Amount in [Currency.CAD]
 */
val Number.CAD
    get() = Amount(Currency.CAD, toDouble())

/**
 * Amount in [Currency.CNY]
 */
val Number.CNY
    get() = Amount(Currency.CNY, toDouble())

/**
 * Amount in [Currency.HKD]
 */
val Number.HKD
    get() = Amount(Currency.HKD, toDouble())

/**
 * Amount in [Currency.NZD]
 */
val Number.NZD
    get() = Amount(Currency.NZD, toDouble())

/**
 * Amount in [Currency.RUB]
 */
val Number.RUB
    get() = Amount(Currency.RUB, toDouble())

/**
 * Amount in [Currency.INR]
 */
val Number.INR
    get() = Amount(Currency.INR, toDouble())

/**
 * Amount in [Currency.KRW]
 */
val Number.KRW
    get() = Amount(Currency.KRW, toDouble())

// Extensions to make it easier to create amounts for common cryptocurrencies

/**
 * Amount in [Currency.BTC]
 */
val Number.BTC
    get() = Amount(Currency.BTC, toDouble())

/**
 * Amount in [Currency.ETH]
 */
val Number.ETH
    get() = Amount(Currency.ETH, toDouble())

/**
 * Amount in [Currency.USDT]
 */
val Number.USDT
    get() = Amount(Currency.USDT, toDouble())

/**
 * Add all the amounts together and return the resulting wallet.
 */
fun Collection<Amount>.toWallet(): Wallet {
    return sumOf { it }
}

