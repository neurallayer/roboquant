/*
 * Copyright 2020-2022 Neural Layer
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

import java.time.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Exchange contains the metadata of a marketplace or exchange.  When creating a new Exchange instance, use
 * the [Exchange.getInstance] method. This ensures only a single instance of an exchange exists for a given currency
 * code and that allows for fast equality comparison.
 *
 * It is used at several areas in roboquant, for example when loading CSV files.
 *
 * @property exchangeCode The exchange code
 * @property zoneId The ZoneId of the exchange
 * @property currency The primary currency of the exchange
 * @property tradingCalendar The trading calendar
 */
class Exchange private constructor(
    val exchangeCode: String,
    val zoneId: ZoneId,
    val currency: Currency,
    private val tradingCalendar: TradingCalendar
) {

    /**
     * Returns true if the two provided times ([first] and [second]) belong to the same trading day. They can be
     * outside trading hours as long as they are in the same calendar day.
     */
    fun sameDay(first: Instant, second: Instant): Boolean {
        val dt1 = LocalDate.ofInstant(first, zoneId)
        val dt2 = LocalDate.ofInstant(second, zoneId)
        return dt1 == dt2
    }

    /**
     * Returns the local date based on the provided [time]
     */
    fun getLocalDate(time: Instant): LocalDate {
        return LocalDate.ofInstant(time, zoneId)
    }

    /**
     * Returns the opening time for a certain [date]
     */
    fun getOpeningTime(date: LocalDate): Instant {
        val opening = tradingCalendar.getOpeningTime(date)
        if (opening === null) throw NoTrading(date)
        val zdt = ZonedDateTime.of(date, opening, zoneId)
        return zdt.toInstant()
    }

    /**
     * Returns the closing time for a certain [date]
     */
    fun getClosingTime(date: LocalDate): Instant {
        val closing = tradingCalendar.getClosingTime(date)
        if (closing === null) throw NoTrading(date)
        val zdt = ZonedDateTime.of(date, closing, zoneId)
        return zdt.toInstant()
    }

    private fun getTradingHours(date: LocalDate): Timeframe {
        return Timeframe(getOpeningTime(date), getClosingTime(date))
    }

    /**
     * Is the provided time a trading time
     */
    fun isTrading(time: Instant): Boolean {
        val date = LocalDate.from(time.atZone(zoneId))
        return tradingCalendar.isTradingDay(date) && getTradingHours(date).contains(time)

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

    /**
     * @suppress
     */
    companion object {

        private val instances = ConcurrentHashMap<String, Exchange>()

        private const val NY_TIMEZONE = "America/New_York"

        /**
         * Returns the Exchange instance for the given [exchangeCode]. If no exchange is found, the default exchange
         * is returned instead.
         *
         * @param exchangeCode the currency code for the currency, not limited to ISO-4217 codes like regular Java
         * Currencies
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
        ): Exchange {
            val zoneId = ZoneId.of(zone)
            val currency = Currency.getInstance(currencyCode)
            val tradingCalendar = SimpleTradingCalendar(opening, closing)
            val instance = Exchange(exchangeCode, zoneId, currency, tradingCalendar)
            instances[exchangeCode] = instance
            return instance
        }

        init {
            addInstance("", NY_TIMEZONE, "USD")

            // North American exchanges
            addInstance("US", NY_TIMEZONE, "USD")
            addInstance("NYSE", NY_TIMEZONE, "USD")
            addInstance("NASDAQ", NY_TIMEZONE, "USD")
            addInstance("BATS", NY_TIMEZONE, "USD")
            addInstance("ARCA", NY_TIMEZONE, "USD")
            addInstance("AMEX", NY_TIMEZONE, "USD")
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

            // Generic 24x7 Crypto Exchange
            addInstance("CRYPTO", NY_TIMEZONE, "USD", "00:00", "23:59:59.999")
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
            get() = instances.values

    }
}
