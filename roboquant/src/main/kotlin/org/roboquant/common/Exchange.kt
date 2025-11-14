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

@file:Suppress("unused")

package org.roboquant.common

import java.time.*
import java.util.concurrent.ConcurrentHashMap


/**
 * Exchange contains the metadata of a marketplace or exchange.
 *
 * When creating a new Exchange instance, use the [Exchange.getInstance] method. This ensures only a single instance
 * of an exchange exists for a given exchange code and that allows for fast equality comparison.
 *
 * @property exchangeCode The exchange code
 * @property zoneId The ZoneId of the exchange
 * @property tradingCalendar The trading calendar
 */
class Exchange private constructor(
    val exchangeCode: String,
    val zoneId: ZoneId,
    private val tradingCalendar: TradingCalendar
) {

    /**
     * Returns true if the two provided times ([first] and [second]) belong to the same calendar day with respect to
     * the [zoneId] of the exchange.
     */
    fun sameDay(first: Instant, second: Instant): Boolean {
        val dt1 = LocalDate.ofInstant(first, zoneId)
        val dt2 = LocalDate.ofInstant(second, zoneId)
        return dt1.equals(dt2)
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
        if (opening === null) throw NoTradingException(date)
        val zdt = ZonedDateTime.of(date, opening, zoneId)
        return zdt.toInstant()
    }

    /**
     * Returns the closing time for a certain [date]
     */
    fun getClosingTime(date: LocalDate): Instant {
        val closing = tradingCalendar.getClosingTime(date)
        if (closing === null) throw NoTradingException(date)
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
     * Convert a local [dateTime] to an Instant type that reflects the timezone of the exchange.
     */
    fun getInstant(dateTime: LocalDateTime): Instant {
        val zdt = ZonedDateTime.of(dateTime, zoneId)
        return zdt.toInstant()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return exchangeCode
    }

    /**
     * @suppress
     */
    companion object {

        /**
         * List of all the registered exchanges
         */
        val exchanges: MutableCollection<Exchange>
            get() = instances.values

        private val instances = ConcurrentHashMap<String, Exchange>()
        private const val NY_TIMEZONE = "America/New_York"

        /**
         * Returns the Exchange instance for the given [exchangeCode]. If no exchange is found, a new exchange instance
         * is created with the provided [exchangeCode] and the [Exchange.DEFAULT] properties.
         *
         * If this is not the desired behaviour, call [addInstance] first to have full control on teh added exchange.
         */
        fun getInstance(exchangeCode: String): Exchange {
            val result = instances.getOrPut(exchangeCode) {
                Exchange(exchangeCode, DEFAULT.zoneId, DEFAULT.tradingCalendar)
            }
            return result
        }

        /**
         * Add an exchange instance to the list. For a given [exchangeCode] there can only be one instance so this will
         * override existing exchange with same exchangeCode. The [opening] and [closing] times are local times and the
         * [zone] is the time zone like for example `America/New_York`.
         */
        fun addInstance(
            exchangeCode: String,
            zone: String,
            opening: String = "09:30",
            closing: String = "16:00"
        ): Exchange {
            val zoneId = ZoneId.of(zone)
            val tradingCalendar = SimpleTradingCalendar(opening, closing)
            val instance = Exchange(exchangeCode, zoneId, tradingCalendar)
            instances[exchangeCode] = instance
            return instance
        }

        /**
         * The default exchange is the exchange with as exchangeCode an empty string and used as a fallback if an
         * exchange cannot be found or an exchange is not specified. It uses New York timezone.
         */
        val DEFAULT: Exchange = addInstance("", NY_TIMEZONE)

        /**
         * Generic US Exchange
         */
        val US: Exchange = addInstance("US", NY_TIMEZONE)

        /**
         * New York Stock Exchange (US)
         */
        val NYSE: Exchange = addInstance("NYSE", NY_TIMEZONE)

        /**
         * NASDAQ (US) Stock Exchange
         */
        val NASDAQ: Exchange = addInstance("NASDAQ", NY_TIMEZONE)

        /**
         * BATS (US) Stock Exchange
         */
        val BATS: Exchange = addInstance("BATS", NY_TIMEZONE)

        /**
         * CBOE (US) Stock Exchange
         */
        val CBOE: Exchange = addInstance("CBOE", NY_TIMEZONE)

        /**
         * ARCA (US) Stock Exchange
         */
        val ARCA: Exchange = addInstance("ARCA", NY_TIMEZONE)

        /**
         * AMEX (US) Stock Exchange
         */
        val AMEX: Exchange = addInstance("AMEX", NY_TIMEZONE)

        /**
         * Toronto Stock Exchange
         */
        val TSX: Exchange = addInstance("TSX", "America/Toronto")

        /**
         * Amsterdam Stock Exchange
         */
        val AEB: Exchange = addInstance("AEB", "Europe/Amsterdam", "09:00", "17:30")

        /**
         * London Stock Exchange
         */
        val LSE: Exchange = addInstance("LSE", "Europe/London", "08:00", "16:30")

        /**
         * German Stock Exchange
         */
        val DEX: Exchange = addInstance("DEX", "Europe/Berlin", "09:00", "17:30")

        /**
         * Swiss Stock Exchange
         */
        val SIX: Exchange = addInstance("SIX", "Europe/Zurich", "09:00", "17:20")

        /**
         * Paris Stock Exchange
         */
        val PAR: Exchange = addInstance("PAR", "Europe/Paris", "09:00", "17:30")

        /**
         * Tokyo Stock Exchange
         */
        val JPX: Exchange = addInstance("JPX", "Asia/Tokyo", "09:00", "15:00")

        /**
         * Shanghai Stock Exchange
         */
        val SSE: Exchange = addInstance("SSE", "Asia/Shanghai", closing = "15:00")

        /**
         * Hong Kong Stock Exchange
         */
        val SEKH: Exchange = addInstance("SEHK", "Asia/Hong_Kong")

        /**
         * Sydney Stock Exchange
         */
        val SSX: Exchange = addInstance("SSX", "Australia/Sydney", "10:00")

        /**
         * Generic 24x7 Crypto Exchange
         */
        val CRYPTO: Exchange = addInstance("CRYPTO", "UTC", "00:00", "23:59:59.999")

        /**
         * Generic 24x7 Forex Exchange
         */
        val FOREX: Exchange = addInstance("FOREX", "UTC", "00:00", "23:59:59.999")

    }
}
