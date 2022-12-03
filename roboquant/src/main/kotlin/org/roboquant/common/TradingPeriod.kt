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

import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAmount

/**
 * Trading Period is a class that unifies the JVM classes Duration and Period and allows to calculate with it more
 * easily.
 *
 * Under the hood it will use [Config.defaultZoneId] when working with periods that require a timezone.
 *
 * @property period the period that this applies to
 */
@JvmInline
value class TradingPeriod(val period : TemporalAmount)


/*********************************************************************************************
 * Extensions on Int type to make instantiation of TradingPeriods convenient
 *********************************************************************************************/

/**
 * Convert number to years
 */
val Int.years: TradingPeriod
    get() = TradingPeriod(Period.ofYears(this))

/**
 * Convert number to months
 */
val Int.months: TradingPeriod
    get() = TradingPeriod(Period.ofMonths(this))

/**
 * Convert number to weeks
 */
val Int.weeks: TradingPeriod
    get() = TradingPeriod(Period.ofWeeks(this))

/**
 * Convert number to days
 */
val Int.days: TradingPeriod
    get() = TradingPeriod(Period.ofDays(this))

/**
 * Convert number to hours
 */
val Int.hours: TradingPeriod
    get() = TradingPeriod(Duration.ofHours(this.toLong()))

/**
 * Convert number to minutes
 */
val Int.minutes: TradingPeriod
    get() = TradingPeriod(Duration.ofMinutes(this.toLong()))

/**
 * Convert number to seconds
 */
val Int.seconds: TradingPeriod
    get() = TradingPeriod(Duration.ofSeconds(this.toLong()))

/**
 * Convert number to millis
 */
val Int.millis: TradingPeriod
    get() = TradingPeriod(Duration.ofMillis(this.toLong()))


/**
 * Subtract a trading [period] to an instant
 */
operator fun Instant.minus(period: TradingPeriod) : Instant {
    val zoneId = Config.defaultZoneId
    val now = this.atZone(zoneId)
    return (now - period.period).toInstant()
}

/**
 * Add a trading [period] from an instant
 */
operator fun Instant.plus(period: TradingPeriod): Instant {
    val zoneId = Config.defaultZoneId
    val now = this.atZone(zoneId)
    return (now + period.period).toInstant()
}

/**
 * Add a trading [period] from an instant at a given [zoneId]
 */
fun Instant.plus(period: TradingPeriod, zoneId: ZoneId): Instant {
    val now = this.atZone(zoneId)
    return (now + period.period).toInstant()
}

/**
 * Subtract a trading [period] from a zoned date-time
 */
operator fun ZonedDateTime.minus(period: TradingPeriod) : ZonedDateTime {
    return this - period.period
}

/**
 * Add a trading [period] to a zoned date-time
 */
operator fun ZonedDateTime.plus(period: TradingPeriod): ZonedDateTime {
    return this + period.period
}
