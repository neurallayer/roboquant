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

import org.roboquant.brokers.Account
import java.lang.Exception


/**
 * Wallet can contain amounts of different currencies at the same time. So for example a single instance of Wallet can
 * contain both USD and EURO amounts.
 *
 * You can add other currencies to a Wallet instance. If the currency is already contained in the Wallet instance, it will
 * be added to the existing amount, otherwise the currency and amount will be added.
 *
 * It is used throughout roboquant in order to support trading in multiple assets with different currency denominations.
 *
 * For storing monetary amounts internally it uses [Double], since it is accurate enough for trading while providing large
 * performance benefits over BigDecimal.
 *
 * Wallet itself will never convert the currencies it contains. However, an account can do this if required, provided the
 * appropriate conversion rates are available. See also [Account.convert] on how to convert a Wallet instance
 * to a single amount value.
 *
 */
class Wallet(vararg amounts: Amount) : Cloneable {

    private val data = mutableMapOf<Currency, Double>()

    init {
        for (amount in amounts) deposit(amount)
    }


    /**
     * Return the currencies that are hold in this Wallet object. Currencies with
     * zero balance will not be included.
     */
    val currencies: List<Currency>
        get() = data.keys.sortedBy { it.currencyCode }.toList()


    /**
     * Get the amount for a certain [currency]. If the currency is not
     * found, a zero amount will be returned.
     *
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
     * Is this cash instance empty, meaning it has zero entries with a non-zero balance.
     */
    fun isEmpty() = data.isEmpty()


    /**
     * Is this cash instance not empty, meaning it has at least one entry that has a non-zero balance.
     */
    fun isNotEmpty() = data.isNotEmpty()

    /**
     * Add operator + to allow for cash + cash
     */
    operator fun plus(other: Wallet): Wallet {
        val result = clone()
        result.deposit(other)
        return result
    }

    /**
     * Add operator - to allow for cash - cash
     */
    operator fun minus(other: Wallet): Wallet {
        val result = clone()
        result.withdraw(other)
        return result
    }


    /**
     * Set a monetary [amount]. If the currency already exist, its value
     * will be overwritten, otherwise a new entry will be created.
     */
    fun set(amount: Amount) {
        if (amount.value == 0.0)
            data.remove(amount.currency)
        else
            data[amount.currency] = amount.value
    }

    /**
     * Deposit a monetary [amount][Amount]. If the currency already exist, it
     * will be added to the existing value, otherwise a new entry will be created.
     */
    fun deposit(amount: Amount) {
        val value = getValue(amount.currency) + amount.value
        set(Amount(amount.currency, value))
    }


    /**
     * Deposit the cash hold in an [other] Wallet instance into this one.
     */
    fun deposit(other: Wallet) {
        for (amount in other.toAmounts()) { deposit(amount) }
    }


    /**
     * Withdraw  a monetary [amount][Amount]. If the currency already exist, it
     * will be deducted from the existing value, otherwise a new entry will be created.
     *
     * @param amount
     */
    fun withdraw(amount: Amount) {
        deposit(amount * -1)
    }


    fun toAmount() : Double {
        return when(currencies.size) {
            0 -> 0.0
            1 -> data.filter { it.value != 0.0 }.values.first()
            else -> throw Exception("Multicurrency account")
        }
    }


    /**
     * Withdraw the cash hold in an [other] Wallet instance into this one.
     */
    fun withdraw(other: Wallet) {
       for (amount in other.toAmounts()) { withdraw(amount) }
    }



    /**
     * Does the wallet contain multiple currencies with a non-zero balance.
     */
    fun isMultiCurrency(): Boolean {
        return currencies.size > 1
    }


    override fun clone(): Wallet {
        val result = Wallet()
        result.data.putAll(data)
        return result
    }


    /**
     * Clear this Wallet instance, removing all entries.
     */
    fun clear() {
        data.clear()
    }


    /**
     * Provide a map representation of the cash hold where the key is the [Currency] and the value is the amount.
     */
    fun toAmounts(): List<Amount> = data.map { Amount(it.key, it.value) }

    /**
     * Provide a map representation of the cash hold where the key is the [Currency] and the value is the amount.
     */
    fun toMap(): Map<Currency, Double> = data.toMap()


    /**
     * Create a string representation with respecting currency preferred settings when formatting the amounts.
     */
    override fun toString(): String {
        val sb = StringBuffer()
        for (amount in toAmounts()) {
            if (amount.value != 0.0)
                sb.append("$amount\n")
        }
        return sb.toString()
    }

    /**
     * Provide a short summary including all currencies, also the one that have a zero balance.
     */
    fun summary(header: String = "Cash"): Summary {
        val s = Summary(header)
        toAmounts().forEach {
            s.add(it.currency.displayName, it.formatValue())
        }
        return s
    }

    override fun equals(other: Any?) = if (other is Wallet) data == other.data else false

    override fun hashCode(): Int {
        return data.hashCode()
    }

    /**
     * Summary overview of the cash positions
     */
    fun summary(): Summary {
        val result = Summary("Cash")
        val fmt = "│%10s│%14s│"
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

}

