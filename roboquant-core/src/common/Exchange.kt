@file:Suppress("unused")
package org.roboquant.common

import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Exchange contains the metadata about an exchange. The most important meta-data are the zoneId, opening and
 * closing times and the default currency.
 *
 * It is used at several areas in roboquant, for example when loading CSV files
 *
 * @property exchangeCode
 * @property zoneId
 * @constructor Create new Exchange
 */
class Exchange private constructor(
    val exchangeCode: String,
    val zoneId: ZoneId,
    val currency: Currency,
    val opening: LocalTime = LocalTime.parse("09:00"),
    val closing: LocalTime = LocalTime.parse("16:30")
) {


    fun day(now: Instant): Int {
        val dt = now.atZone(zoneId)
        return dt.dayOfYear
    }

    /**
     * Do the two provided times belong to the same trading day.
     *
     * @param first
     * @param second
     * @return
     */
    fun sameDay(first: Instant, second: Instant): Boolean {
        val dt1 = LocalDate.ofInstant(first, zoneId)
        val dt2 = LocalDate.ofInstant(second, zoneId)
        return dt1 == dt2
    }

    /**
     * Get opening time at an Instant based on a local date
     *
     * @param date
     * @return
     */
    fun getOpeningTime(date: LocalDate): Instant {
        val zdt = ZonedDateTime.of(date, opening, zoneId)
        return zdt.toInstant()
    }

    /**
     * Get closing time
     *
     * @param date
     * @return
     */
    fun getClosingTime(date: LocalDate): Instant {
        val zdt = ZonedDateTime.of(date, closing, zoneId)
        return zdt.toInstant()
    }

    /**
     * Get closing time
     *
     * @param dateTime
     * @return
     */
    fun getInstant(dateTime: LocalDateTime): Instant {
        val zdt = ZonedDateTime.of(dateTime, zoneId)
        return zdt.toInstant()
    }

    companion object {

        private val instances = ConcurrentHashMap<String, Exchange>()

        /**
         * Returns the Currency instance for the given currency code.
         *
         * @param exchangeCode the currency code for the currency, not limited to ISO-4217 codes like regular Java Currencies
         * @return Returns the Currency instance for the given currency code
         */
        fun getInstance(exchangeCode: String): Exchange = instances[exchangeCode] ?: DEFAULT

        /**
         * Returns the Currency instance for the given currency code.
         *
         * @param exchangeCode the currency code for the currency, not limited to ISO-4217 codes like regular Java Currencies
         * @return Returns the Currency instance for the given currency code
         */
        fun addInstance(exchangeCode: String, zone: String, currencyCode: String = "USD"): Exchange {
            val zoneId = TimeZone.getTimeZone(zone).toZoneId()
            val currency = Currency.getInstance(currencyCode)
            val instance = Exchange(exchangeCode, zoneId, currency)
            instances[exchangeCode] = instance
            return instance
        }

        /**
         * The default exchange
         */
        val DEFAULT = addInstance("", "America/New_York", "USD")

        // Exchange for all US exchanges
        val US = addInstance("US", "America/New_York", "USD")
        val NYSE = addInstance("NYSE", "America/New_York", "USD")
        val NASDAQ = addInstance("NASDAQ", "America/New_York", "USD")
        val BATS = addInstance("BATS", "America/New_York", "USD")
        val ARCA = addInstance("ARCA", "America/New_York", "USD")
        val AMEX = addInstance("AMEX", "America/New_York", "USD")

        val TSX = addInstance("TSX", "America/Toronto", "CAD")

        // European exchanges
        val LSE = addInstance("LSE", "Europe/London", "GBP")
        val FSX = addInstance("FSX", "Europe/Berlin", "EUR")
        val SIX = addInstance("SIX", "Europe/Zurich", "CHF")

        // Asian
        val JPX = addInstance("JPX", "Asia/Tokyo", "JPY")
        val SSE = addInstance("SSE", "Asia/Shanghai", "CNY")

        // Australia
        val SSX = addInstance("SSX", "Australia/Sydney", "AUD")

        /**
         * List of currently registered exchanges
         */
        val exchanges
            get() = instances.values.toList()


    }
}