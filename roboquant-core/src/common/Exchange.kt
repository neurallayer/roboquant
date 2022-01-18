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

import java.time.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Exchange contains the metadata about an exchange. The most important meta-data are the [zoneId], [opening] and
 * [closing] times and the default currency.
 *
 * It is used at several areas in roboquant, for example when loading CSV files.
 */
class Exchange private constructor(
    val exchangeCode: String,
    val zoneId: ZoneId,
    val currency: Currency,
    val opening: LocalTime = LocalTime.parse("09:30"),
    val closing: LocalTime = LocalTime.parse("16:00")
) {

    /**
     * Belong the two provided times ([first] and [second]) to the same trading day? They can be outside
     * trading hours.
     */
    fun sameDay(first: Instant, second: Instant): Boolean {
        val dt1 = LocalDate.ofInstant(first, zoneId)
        val dt2 = LocalDate.ofInstant(second, zoneId)
        return dt1 == dt2
    }

    /**
     * Get the opening time for a local [date]
     */
    fun getOpeningTime(date: LocalDate): Instant {
        val zdt = ZonedDateTime.of(date, opening, zoneId)
        return zdt.toInstant()
    }

    /**
     * Get the closing time for a local [date]
     */
    fun getClosingTime(date: LocalDate): Instant {
        val zdt = ZonedDateTime.of(date, closing, zoneId)
        return zdt.toInstant()
    }

    /**
     * Get the trading hours for a certain date
     */
    fun getTradingHours(date: LocalDate): TimeFrame {
       return TimeFrame(getOpeningTime(date), getClosingTime(date))
    }


    /**
     * Convert a local [dateTime] to an Instant type
     */
    fun getInstant(dateTime: LocalDateTime): Instant {
        val zdt = ZonedDateTime.of(dateTime, zoneId)
        return zdt.toInstant()
    }

    override fun toString(): String {
        return exchangeCode
    }

    companion object {

        private val instances = ConcurrentHashMap<String, Exchange>()

        /**
         * Returns the Exchange instance for the given [exchangeCode]. If no exchange is found, the default exchange
         * is returned instead.
         *
         * @param exchangeCode the currency code for the currency, not limited to ISO-4217 codes like regular Java Currencies
         * @return Returns the Currency instance for the given currency code
         */
        fun getInstance(exchangeCode: String): Exchange = instances[exchangeCode] ?: DEFAULT

        /**
         * add the Exchange instance to the list. For a given [exchangeCode] there can only be one instance.
         *
         * @param exchangeCode the exchange code
         */
        fun addInstance(
            exchangeCode: String,
            zone: String,
            currencyCode: String = "USD",
            opening: String = "09:30",
            closing: String = "16:00"
        ) {
            val zoneId = ZoneId.of(zone)
            val currency = Currency.getInstance(currencyCode)
            val instance = Exchange(exchangeCode, zoneId, currency, LocalTime.parse(opening), LocalTime.parse(closing))
            instances[exchangeCode] = instance
        }

        init {
            addInstance("", "America/New_York", "USD")

            // North American exchanges
            addInstance("US", "America/New_York", "USD")
            addInstance("NYSE", "America/New_York", "USD")
            addInstance("NASDAQ", "America/New_York", "USD")
            addInstance("BATS", "America/New_York", "USD")
            addInstance("ARCA", "America/New_York", "USD")
            addInstance("AMEX", "America/New_York", "USD")
            addInstance("TSX", "America/Toronto", "CAD")

            // European exchanges
            addInstance("AEB", "Europe/Amsterdam", "EUR", "09:00", "17:30")
            addInstance("LSE", "Europe/London", "GBP", "08:00", "16:30")
            addInstance("FSX", "Europe/Berlin", "EUR", "09:00", "17:30")
            addInstance("SIX", "Europe/Zurich", "CHF", "09:00", "17:20")

            // Asian exchanges
            addInstance("JPX", "Asia/Tokyo", "JPY", "09:00", "15:00")
            addInstance("SSE", "Asia/Shanghai", "CNY", "09:30", "15:00")
            addInstance("SEHK", "Asia/Hong_Kong", "CNY", "09:30", "16:00")

            // Australian exchanges
            addInstance("SSX", "Australia/Sydney", "AUD", "10:00", "16:00")
        }


        /**
         * The default exchange is the exchange with as exchangeCode an empty string and used as a fallback if an
         * exchange cannot be found.
         */
        val DEFAULT
            get() = getInstance("")


        /**
         * List of all the currently registered exchanges
         */
        val exchanges
            get() = instances.values.toList()


    }
}