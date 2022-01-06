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

@file:Suppress("MemberVisibilityCanBePrivate")

package org.roboquant.common

import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap

/**
 * Currency implementation that supports regular currencies as well as cryptocurrencies. So the [currencyCode] for the
 * currency is not limited to ISO-4217 codes like regular Java Currency class.
 *
 * This is a lightweight implementation since most of roboquant functionality only relies on the currency code.
 *
 * When creating a new currency instance, use the [Currency.getInstance] method. This ensures only a single
 * instance of a currency exist for a given currency code and that allows for faster equality comparison and hashmap
 * lookup.
 *
 *  @property currencyCode The currency code for the currency.
 **/
class Currency private constructor(val currencyCode: String) {

    /**
     * The name to use when displaying this currency, default is the currency code
     */
    val displayName
        get() = currencyCode

    /**
     * The number of digits to use when formatting amounts denominated in this currency
     */
    var defaultFractionDigits: Int =
        try {
            java.util.Currency.getInstance(currencyCode).defaultFractionDigits
        } catch (e: Exception) {
            // If we cannot find a default fraction for this currency, use 2
            2
        }


    companion object {
        private val currencies = ConcurrentHashMap<String, Currency>()

        /**
         * Rounding mode to use when displaying limited number of digits, default is [RoundingMode.HALF_DOWN]
         * @see RoundingMode
         */
        var roundingMode = RoundingMode.HALF_DOWN

        /**
         * Returns the Currency instance for the given the provided [currencyCode].
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

        /**
         * For all already registered currencies increase the number of display digits with [extraDigits]. This doesn't
         * change calculations, only the way currency amounts are displayed.
         *
         * TIP: For Forex trading this is often required since otherwise small differences can not be seen.
         */
        fun increaseDigits(extraDigits: Int = 3) {
            for (currency in currencies.values) currency.defaultFractionDigits += extraDigits
        }
    }

    /** @suppress */
    override fun toString(): String {
        return displayName
    }

}
