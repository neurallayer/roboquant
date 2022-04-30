package org.roboquant.common

import java.time.*


/**
 * Trading calendar is used to define when an [Exchange] is open for trading. Timezone conversions
 * are handled by the Exchange class, the trading calendar only deals with [LocalDate] and [LocalTime] instances.
 */
interface TradingCalendar {

    /**
     * Returns the opening time for the provided [date] or null if it is not a trading day
     */
    fun getOpeningTime(date: LocalDate): LocalTime?

    /**
     * Returns the closing time for the provided [date] or null if it is not a trading day
     */
    fun getClosingTime(date: LocalDate): LocalTime?

    /**
     * Returns true if the provided [date] is a trading day, false otherwise.
     */
    fun isTradingDay(date: LocalDate): Boolean

}

/**
 * Simple trading calendar that supports a fixed open and close time and optional exclude certain days of the week.
 *
 * @property opening opening time, default is "09:30"
 * @property closing closing time, default is "16:00"
 * @property excludeDays which days of the week to exclude, default are SATURDAY and SUNDAY
 * @constructor Create new Simple trading calendar
 */
class SimpleTradingCalendar(
    private val opening: LocalTime = LocalTime.parse("09:30"),
    private val closing: LocalTime = LocalTime.parse("16:00"),
    private val excludeDays : Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
) : TradingCalendar {

    constructor(opening: String, closing: String) : this(
        LocalTime.parse(opening),
        LocalTime.parse(closing)
    )

    override fun getOpeningTime(date: LocalDate): LocalTime? = if (! isTradingDay(date)) null else opening

    override fun getClosingTime(date: LocalDate): LocalTime? = if (! isTradingDay(date)) null else closing

    override fun isTradingDay(date: LocalDate): Boolean = date.dayOfWeek !in excludeDays

}