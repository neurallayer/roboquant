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

package org.roboquant.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Trading calendar defines when an [Exchange] is open for trading. Timezone conversions are handled by the [Exchange]
 * class, so the trading calendar only deals with [LocalDate] and [LocalTime] instances.
 */
interface TradingCalendar {

    /**
     * Returns the opening time for the provided local [date] or null if it is not a trading day
     */
    fun getOpeningTime(date: LocalDate): LocalTime?

    /**
     * Returns the closing time for the provided local [date] or null if it is not a trading day
     */
    fun getClosingTime(date: LocalDate): LocalTime?

    /**
     * Returns true if the provided [date] is a trading day, false otherwise.
     */
    fun isTradingDay(date: LocalDate): Boolean

}

/**
 * Simple trading calendar that supports a fixed open and close time and optionally exclude certain days of the week.
 * There is no support for public holidays or bank holidays.
 *
 * @property opening opening time, default is "09:30"
 * @property closing closing time, default is "16:00"
 * @property excludeDays which days of the week to exclude, default is SATURDAY and SUNDAY
 * @constructor Create a new Simple Trading Calendar
 */
class SimpleTradingCalendar(
    private val opening: LocalTime = LocalTime.parse("09:30"),
    private val closing: LocalTime = LocalTime.parse("16:00"),
    private val excludeDays: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
) : TradingCalendar {

    constructor(opening: String, closing: String) : this(
        LocalTime.parse(opening),
        LocalTime.parse(closing)
    )

    /**
     * @see TradingCalendar.getOpeningTime
     */
    override fun getOpeningTime(date: LocalDate): LocalTime? = if (!isTradingDay(date)) null else opening

    /**
     * @see TradingCalendar.getClosingTime
     */
    override fun getClosingTime(date: LocalDate): LocalTime? = if (!isTradingDay(date)) null else closing

    /**
     * @see TradingCalendar.isTradingDay
     */
    override fun isTradingDay(date: LocalDate): Boolean = date.dayOfWeek !in excludeDays

}
