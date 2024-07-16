/*
 * Copyright 2020-2024 Neural Layer
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

import java.time.Instant
import java.util.*
import kotlin.math.absoluteValue


/**
 * A wallet holds the [Amount]s of different currencies. For example, a single instance of a Wallet can
 * hold both USD and EUR amounts.
 *
 * You can add other currencies to a Wallet instance. If the currency is already present in the Wallet, it
 * will be added to the existing amount, otherwise the currency and amount will be added.
 *
 * It is used throughout roboquant to support trading in assets with different currency denominations.
 *
 * Wallet by itself will never convert currencies when depositing or withdrawing amounts. But you can invoke
 * the [convert] method if you want to do so.
 */
@Suppress("TooManyFunctions")
class Wallet(private val data: IdentityHashMap<Currency, Double> = IdentityHashMap(1)) : Cloneable {

    /**
     * @suppress
     */
    companion object {

        /**
         * Create a Wallet based on the [amount]
         */
        operator fun invoke(amount: Amount): Wallet {
            val data = IdentityHashMap<Currency, Double>(1)
            data[amount.currency] = amount.value
            return Wallet(data)
        }

        /**
         * Create a Wallet based on the [amounts]
         */
        operator fun invoke(vararg amounts: Amount): Wallet {
            val wallet = Wallet()
            for (amount in amounts) wallet.deposit(amount)
            return wallet
        }

    }

    /**
     * Return the currencies that are hold in this wallet
     */
    val currencies: Set<Currency>
        get() = data.keys

    /**
     * Get the amount for a certain [currency]. If the currency is not found, a zero [Amount] will be returned.
     */
    fun getAmount(currency: Currency): Amount {
        return Amount(currency, get(currency))
    }

    /**
     * Return the value for a certain [currency]. If the currency is not found, 0.0 will be returned, so this method
     * will never return a null value.
     */
    operator fun get(currency: Currency): Double = data[currency] ?: 0.0

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
     * will be created. If the new value is zero, the entry will be removed since a wallet doesn't contain zero
     * values
     */
    fun set(currency: Currency, value: Double) {
        if (value.absoluteValue < Config.EPS)
            data.remove(currency)
        else
            data[currency] = value
    }

    /**
     * Deposit a monetary [amount][Amount]. If the currency already exists, it
     * will be added to the existing value, otherwise a new entry will be created.
     */
    fun deposit(amount: Amount) {
        val ccy = amount.currency
        val oldValue = data[ccy] ?: 0.0
        set(ccy, amount.value + oldValue)
    }

    /**
     * Deposit an [value] of a certain currency
     */
    fun deposit(currency: Currency, value: Double) {
        val oldValue = data[currency] ?: 0.0
        val newValue = value + oldValue
        if (newValue.absoluteValue < Config.EPS) data.remove(currency) else data[currency] = newValue
    }

    /**
     * Deposit the amount hold in an [other] Wallet instance into this one.
     */
    fun deposit(other: Wallet) {
        assert(other !== this)
        for ((c, v) in other.data) deposit(c, v)
    }

    /**
     * Withdraw a monetary [amount][Amount]. If the currency already exists, it
     * will be deducted from the existing value, otherwise a new entry will be created.
     */
    fun withdraw(amount: Amount) {
        deposit(amount.currency, -amount.value)
    }

    /**
     * Withdraw the amount hold in an [other] Wallet instance into this one.
     */
    fun withdraw(other: Wallet) {
        assert(other !== this)
        for ((c, v) in other.data) deposit(c, -v)
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
    @Suppress("UNCHECKED_CAST")
    public override fun clone(): Wallet = Wallet(data.clone() as IdentityHashMap<Currency, Double>)

    /**
     * Clear this Wallet instance, removing all the amounts it is holding.
     */
    fun clear() {
        data.clear()
    }

    /**
     * Provide a list representation of the amount hold in this wallet
     */
    private fun toAmounts(): List<Amount> = data.map { Amount(it.key, it.value) }

    /**
     * Returns a map where the key is the [Currency] and the value is the amount.
     */
    fun toMap(): Map<Currency, Double> = data.toMap()

    /**
     * Create a string representation of this wallet with respecting currency preferred settings when
     * formatting the values.
     * The amounts will be sorted by currency-code.
     */
    override fun toString(): String {
        return toAmounts().sortedBy { it.currency.currencyCode }.joinToString(" + ")
    }

    /**
     * A wallet equals another wallet if they hold the same currencies and corresponding amounts.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wallet) return false
        return toMap() == other.toMap()
    }

    /**
     * The hashcode of the wallet
     */
    override fun hashCode(): Int {
        return data.hashCode()
    }

    /**
     * Convert this Wallet into a single [currency] amount. Under the hood is uses [Amount.convert] to perform the
     * actual conversions. Optional a [time] can be provided, the default is [Instant.now].
     */
    fun convert(currency: Currency, time: Instant): Amount {

        // Optimization for single currency trading
        if (data.size == 1 && data.contains(currency)) return Amount(currency, data.getValue(currency))

        var sum = 0.0
        for (amount in toAmounts()) {
            sum += amount.convert(currency, time).value
        }
        return Amount(currency, sum)
    }

}

