package org.roboquant.common

import org.roboquant.brokers.Account


/**
 * Cash can contain amounts of multiple currencies at the same time. So for example a single instance of Cash can
 * contain both USD and EURO amounts.
 *
 * You can add other currencies to a Cash instance. If the currency is already contained in the Cash instance, it will
 * be added to the existing amount, otherwise the currency and amount will be added.
 *
 * It is used throughout roboquant in order to support trading in multiple assets with different currency denominations.
 *
 * For storing monetary amounts internally it uses [Double], since it is accurate enough for trading while providing large
 * performance benefits over BigDecimal.
 *
 * Cash itself will never convert the currencies it contains. However, an account can do this if required, provided the
 * appropriate conversion rates are available. See also [Account.convertToCurrency] on how to convert a Cash instance
 * to a single amount value.
 *
 */
class Cash(vararg amounts: Pair<Currency, Double>) {

    private val data = mutableMapOf<Currency, Double>()

    init {
        amounts.forEach { deposit(it.first, it.second) }
    }


    /**
     * Return the currencies that are hold in this Cash object. Currencies with
     * zero balance will not be included.
     */
    val currencies: List<Currency>
        get() = data.filter { it.value != 0.0 }.keys.toList()


    /**
     * Get the amount for a certain [currency]. If the currency is not
     * found, 0.0 will be returned.
     *
     */
    fun getAmount(currency: Currency): Double {
        return data.getOrDefault(currency, 0.0)
    }

    /**
     * Is this cash instance empty, meaning it has zero entries with a non-zero balance.
     */
    fun isEmpty(): Boolean {
        return !isNotEmpty()
    }

    /**
     * Is this cash instance not empty, meaning it has at least one entry that has a non-zero balance.
     */
    fun isNotEmpty(): Boolean {
        return data.any { it.value != 0.0 }
    }

    /**
     * Deposit a monetary [amount] denominated in teh specified [currency]. If the currency already exist, it
     * will be added to the existing amount, otherwise a new entry will be created.
     */
    fun deposit(currency: Currency, amount: Double) {
        data[currency] = data.getOrDefault(currency, 0.0) + amount
    }


    /**
     * Deposit the cash hold in an [other] Cash instance into this one.
     */
    fun deposit(other: Cash) {
        other.data.forEach { deposit(it.key, it.value) }
    }

    /**
     * Withdraw  a monetary [amount] denominated in the specified [currency]. If the currency already exist, it
     * will be deducted from the existing amount, otherwise a new entry will be created.
     *
     * @param currency
     * @param amount
     */
    fun withdraw(currency: Currency, amount: Double) {
        deposit(currency, -amount)
    }


    /**
     * Withdraw the cash hold in an [other] Cash instance into this one.
     */
    fun withdraw(other: Cash) {
        other.data.forEach { deposit(it.key, -it.value) }
    }


    /**
     * Does the wallet contain multiple currencies with a non-zero balance.
     */
    fun isMultiCurrency(): Boolean {
        return currencies.size > 1
    }


    /**
     * Create a copy of this cash instance
     */
    fun copy(): Cash {
        val wallet = Cash()
        wallet.data.putAll(data)
        return wallet
    }

    /**
     * Clear this Cash instance, removing all entries.
     */
    fun clear() {
        data.clear()
    }

    /**
     * Provide a map representation of the cash hold where the key is the [Currency] and the value is the amount.
     * By default, empty values will not be included but this can be changed by setting [includeEmpty] to true.
     */
    fun toMap(includeEmpty: Boolean = false) : Map<Currency, Double> = if (includeEmpty) data.toMap() else data.filter { it.value != 0.0 }


    /**
     * Create a string representation with respecting currency preferred settings when formatting the amounts.
     */
    override fun toString(): String {
        val sb = StringBuffer()
        for ((c, v) in data) {
            if (v != 0.0)
                sb.append("${c.displayName} => ${c.format(v)} \n")
        }
        return sb.toString()
    }

    /**
     * Provide a short summary including all currencies, also the one that have a zero balance.
     */
    fun summary(): Summary {
        val s = Summary("Cash")
        data.forEach {
            s.add(it.key.displayName, it.key.format(it.value))
        }
        return s
    }

}
