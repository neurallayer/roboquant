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

import java.time.Instant

/**
 * Wallet can contain amounts of different currencies at the same time. So for example a single instance of Wallet can
 * contain both USD and EURO amounts.
 *
 * You can add other currencies to a Wallet instance. If the currency is already contained in the Wallet instance, it
 * will be added to the existing amount, otherwise the currency and amount will be added.
 *
 * It is used throughout roboquant in order to support trading in multiple assets with different currency denominations.
 *
 * Wallet by itself will never convert currencies when depositing or withdrawing amounts. But you can invoke
 * the [convert] method if you want to do so.
 */
@Suppress("TooManyFunctions")
class Wallet(vararg amounts: Amount) : Cloneable {

    // Contains the data of the wallet
    private val data = mutableMapOf<Currency, Double>()

    init {
        for (amount in amounts) deposit(amount)
    }


    /**
     * Return the currencies that are hold in this wallet sorted by [Currency.currencyCode]
     */
    val currencies: List<Currency>
        get() = data.keys.sortedBy { it.currencyCode }.toList()


    /**
     * Get the amount for a certain [currency]. If the currency is not
     * found, a zero amount will be returned.
     */
    fun getAmount(currency: Currency): Amount {
        val value = data.getOrDefault(currency, 0.0)
        return Amount(currency, value)
    }

    /**
     * Get the value for a certain [currency]. If the currency is not
     * found, 0.0 will be returned.
     */
    fun getValue(currency: Currency): Double = data.getOrDefault(currency, 0.0)


    /**
     * Is this wallet instance empty
     */
    fun isEmpty() = data.isEmpty()


    /**
     * Is this wallet instance not empty
     */
    fun isNotEmpty() = data.isNotEmpty()

    /**
     * Add operator + to allow for wallet + wallet
     */
    operator fun plus(other: Wallet): Wallet {
        val result = clone()
        result.deposit(other)
        return result
    }

    /**
     * Minus operator to allow for wallet - wallet
     */
    operator fun minus(other: Wallet): Wallet {
        val result = clone()
        result.withdraw(other)
        return result
    }


    /**
     * Plus operator to allow for wallet + amount. This method is different from [deposit] in that this method doesn't
     * update the current wallet and returns a new wallet instead.
     */
    operator fun plus(amount: Amount): Wallet {
        val result = clone()
        result.deposit(amount)
        return result
    }

    /**
     * Minus operator to allow for wallet - amount. This method is different from [withdraw] in that this method doesn't
     * update the current wallet and returns a new wallet instead.
     */
    operator fun minus(amount: Amount): Wallet {
        val result = clone()
        result.withdraw(amount)
        return result
    }

    /**
     * Times operator to allow for wallet * number. It will multiply all the amounts in the wallet by [n].
     */
    operator fun times(n: Number): Wallet {
        val result = clone()
        for ((k, v) in result.data) result.data[k] = v * n.toDouble()
        return result
    }

    /**
     * Div operator to allow for wallet / number. It will divide all the amounts in the wallet by [n].
     */
    operator fun div(n: Number): Wallet {
        val result = clone()
        for ((k, v) in result.data) result.data[k] = v / n.toDouble()
        return result
    }

    /**
     * Set a monetary [value]. If the [currency] already exist, its value will be overwritten, otherwise a new entry
     * will be created. If the new value is zero, the enntry will be removed since a wallet doesn't contain zero
     * values
     */
    fun set(currency: Currency, value: Double) {
        if (value == 0.0)
            data.remove(currency)
        else
            data[currency] = value
    }

    /**
     * Deposit a monetary [amount][Amount]. If the currency already exist, it
     * will be added to the existing value, otherwise a new entry will be created.
     */
    fun deposit(amount: Amount) {
        val value = (data[amount.currency] ?: 0.0) + amount.value
        set(amount.currency, value)
    }


    /**
     * Deposit the amounts hold in an [other] Wallet instance into this one.
     */
    fun deposit(other: Wallet) {
        for (amount in other.toAmounts()) { deposit(amount) }
    }


    /**
     * Withdraw  a monetary [amount][Amount]. If the currency already exist, it
     * will be deducted from the existing value, otherwise a new entry will be created.
     */
    fun withdraw(amount: Amount) {
        deposit(- amount)
    }


    /**
     * Withdraw the amounts hold in an [other] Wallet instance into this one.
     */
    fun withdraw(other: Wallet) {
       for (amount in other.toAmounts()) { withdraw(amount) }
    }


    /**
     * Does the wallet contain multiple currencies.
     */
    fun isMultiCurrency(): Boolean {
        return currencies.size > 1
    }

    /**
     * Create a clone of this wallet
     */
    public override fun clone(): Wallet {
        val result = Wallet()
        result.data.putAll(data)
        return result
    }


    /**
     * Clear this Wallet instance, removing all amounts it is holding.
     */
    fun clear() {
        data.clear()
    }


    /**
     * Provide a list representation of the amounts hold in this wallet
     */
    fun toAmounts(): List<Amount> = data.map { Amount(it.key, it.value) }

    /**
     * Provide a map representation of the amounts hold where the key is the [Currency] and the value is the amount.
     */
    fun toMap(): Map<Currency, Double> = data.toMap()


    /**
     * Create a string representation of this wallet with respecting currency preferred settings when
     * formatting the values.
     */
    override fun toString(): String {
        val sb = StringBuffer()
        for (amount in toAmounts()) sb.append("$amount, ")
        return sb.toString().removeSuffix(", ")
    }


    /**
     * A wallet only equals another wallet if they hold the same currencies and corresponding amounts.
     */
    override fun equals(other: Any?) = if (other is Wallet) data == other.data else false

    /**
     * The hashcode of the wallet
     */
    override fun hashCode(): Int {
        return data.hashCode()
    }

    /**
     * Summary overview of the wallet
     */
    fun summary(title: String = "cash"): Summary {
        val result = Summary(title)
        val fmt = "%8s│%14s│"
        val header = String.format(fmt, "currency", "amount")
        result.add(header)
        val currencies = currencies
        for (currency in currencies.distinct().sortedBy { it.displayName }) {
            val t =  getAmount(currency).formatValue()
            val line = String.format(fmt,  currency.currencyCode, t)
            result.add(line)
        }
        return result
    }

    /**
     * Convert a [Wallet] value into a single currency amount. Under the hood is uses [Amount.convert] to perfrom the
     * actual conversions.
     *
     * @param toCurrency The currency to convert the cash to, default is the baseCurrency of the account
     * @param time The time to use for the exchange rate, default is the last update time of the account
     * @return The converted amount as a Double
     */
    fun convert(toCurrency: Currency = Config.baseCurrency, time: Instant = Instant.now()): Amount {
        var sum = 0.0
        for (amount in toAmounts()) {
            sum += amount.convert(toCurrency, time).value
        }
        return Amount(toCurrency, sum)
    }

}

