package org.roboquant.common

import org.roboquant.brokers.Account


/**
 * Cash can contain amounts of multiple currencies at the same time. So a single instance of Cash can
 * contain both USD and EURO amounts.
 *
 * You can add other currencies to a Cash instance. If the currency is already contained in the cash instance, it will
 * be added to the existing amount, otherwise the currency and amount will be added.
 *
 * It is used throughout roboquant in order to support trading in multiple assets with different currency denominations.
 *
 * For the monetary amounts internally it uses [Double] since it is accurate enough for trading while providing large
 * performance benefits over BigDecimal.
 *
 * Cash itself will never convert the currencies it contains. However, an account can do this if required, provided the
 * appropriate conversion rates are available. See also [Account.convertToCurrency] on how to convert a cash value
 * to a single currency amount.
 *
 * @constructor create a new Cash instance with the provided amounts added to it
 *
 * @param amounts Any amounts that should be initially added to the instance
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
     * Get the amount for a certain currency. If the currency is not
     * present, 0.0 will be returned instead.
     *
     * @param currency
     * @return
     */
    fun getAmount(currency: Currency): Double {
        return data.getOrDefault(currency, 0.0)
    }

    /**
     * Is this cash instance empty, meaning it has zero entries with a non-zero balance
     *
     * @return
     */
    fun isEmpty(): Boolean {
        return !isNotEmpty()
    }

    /**
     * Is this cash instance not empty, meaning it has at least one entry that has a non-zero balance.
     *
     * @return
     */
    fun isNotEmpty(): Boolean {
        return data.any { it.value != 0.0 }
    }

    /**
     * Deposit a monetary amount into the cash. If the currency already exist, it
     * will be added to the existing amount, otherwise a new entry will be created.
     *
     * @param currency
     * @param amount
     */
    fun deposit(currency: Currency, amount: Double) {
        data[currency] = data.getOrDefault(currency, 0.0) + amount
    }


    /**
     * Deposit the cash hold in another cash instance into this one.
     *
     * @param other
     */
    fun deposit(other: Cash) {
        other.data.forEach { deposit(it.key, it.value) }
    }

    /**
     * Withdraw a monetary amount from the Cash. If the currency already exist, it
     * will be deducted from the existing amount, otherwise a new entry will be created.
     *
     * @param currency
     * @param amount
     */
    fun withdraw(currency: Currency, amount: Double) {
        deposit(currency, -amount)
    }


    /**
     * Withdraw the cash hold in another cash object into this one.
     *
     * @param other
     */
    fun withdraw(other: Cash) {
        other.data.forEach { deposit(it.key, -it.value) }
    }


    /**
     * Does the wallet contain multiple currencies with a non-zero balance.
     *
     * @return
     */
    fun isMultiCurrency(): Boolean {
        return currencies.size > 1
    }


    /**
     * Create a copy of the cash
     *
     * @return
     */
    fun copy(): Cash {
        val wallet = Cash()
        wallet.data.putAll(data)
        return wallet
    }

    /**
     * Clear the cash, removing all entries.
     */
    fun clear() {
        data.clear()
    }

    /**
     * Provide a map representation of the cash where the key is the Currency and the value is the amount.
     * By default, empty values will not be included.
     */
    fun toMap(includeEmpty: Boolean = false) = if (includeEmpty) data.toMap() else data.filter { it.value != 0.0 }


    /**
     * Convert this cash to a format suitable for metric results. This does include zero cash
     *
     * @param prefix The prefix to use
     */
    fun toMetrics(prefix: String) = data.map { "${prefix}${it.key.currencyCode}" to it.value }.toMap()

    /**
     * Create a string representation with respecting currency preferred settings when formatting the amounts.
     *
     * @return the formatted string
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
     *
     */
    fun summary(): Summary {
        val s = Summary("Cash")
        data.forEach {
            s.add(it.key.displayName, it.key.format(it.value))
        }
        return s
    }

}
