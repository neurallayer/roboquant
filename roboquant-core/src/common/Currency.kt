@file:Suppress("MemberVisibilityCanBePrivate")

package org.roboquant.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap

/**
 * Currency implementation that supports regular currencies as well as cryptocurrencies. This is a lite-weight
 * implementation since most of roboquant relies only on the currency code.
 *
 * When creating a new currency instance, use the [Currency.getInstance] method. This ensures only a single
 * instance of a currency exist for a given currency code and that allows for faster equality comparison and hashmap
 * lookup.
 *
 **/
class Currency private constructor(val currencyCode: String) {

    val displayName
        get() = currencyCode

    val defaultFractionDigits: Int =
        try {
            java.util.Currency.getInstance(currencyCode).defaultFractionDigits
        } catch (e: Exception) {
            // If we cannot find a default fraction for this currency, use 2
            2
        }

    /**
     * Format an amount based on the currency. For example USD would have two fraction digits
     * while JPY would have none.
     *
     * @param amount The amount as a Double
     * @param includeCurrency Include the currency code in the returned string
     * @return The formatted amount as a string
     */
    fun format(amount: Double, includeCurrency: Boolean = false): String {
        val amountStr = toBigDecimal(amount).toString()
        return if (includeCurrency)
            "$amountStr $displayName"
        else
            amountStr
    }

    /**
     * Convert double amount to BigDecimal using the currency number of fraction digits.
     *
     * @param amount
     * @return
     */
    fun toBigDecimal(amount: Double) : BigDecimal = BigDecimal(amount).setScale(defaultFractionDigits, roundingMode)


    companion object {
        private val currencies = ConcurrentHashMap<String, Currency>()

        /**
         * Rounding mode to use when displaying limited number of digits
         */
        var roundingMode = RoundingMode.HALF_DOWN

        /**
         * Returns the Currency instance for the given currency code.
         *
         * @param currencyCode the currency code for the currency, not limited to ISO-4217 codes like regular Java Currencies
         * @return Returns the Currency instance for the given currency code
         */
        fun getInstance(currencyCode: String): Currency = currencies.getOrPut(currencyCode) { Currency(currencyCode) }

        // Top 10 commonly used currencies in trading

        /**
         * United States Dollar
         */
        val USD = getInstance("USD")

        /**
         * European Euro
         */
        val EUR = getInstance("EUR")

        /**
         * Japanese Yen
         */
        val JPY = getInstance("JPY")

        /**
         * British Pound Sterling
         */
        val GBP = getInstance("GBP")

        /**
         * Australian Dollar
         */
        val AUD = getInstance("AUD")

        /**
         * Canadian Dollar
         */
        val CAD = getInstance("CAD")

        /**
         * Swiss Franc
         */
        val CHF = getInstance("CHF")

        /**
         * Chinese Yuan Renminbi
         */
        val CNY = getInstance("CNY")

        /**
         * Hong Kong Dollar
         */
        val HKD = getInstance("HKD")

        /**
         * New Zealand Dollar
         */
        val NZD = getInstance("NZD")
    }

    override fun toString(): String {
        return displayName
    }

}