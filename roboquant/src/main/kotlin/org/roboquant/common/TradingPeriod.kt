package org.roboquant.common

import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.TemporalAmount

/**
 * Trading Period is a class that unifies the JVM classes Duration and Period and allows to calculate with it more
 * easily.
 *
 * Under the hood it will use [Config.defaultZoneId] when working with periods that require a timezone.
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
 * Add a trading [period] to an instant
 */
operator fun Instant.minus(period: TradingPeriod) : Instant {
    val zoneId = Config.defaultZoneId
    val now = this.atZone(zoneId)
    return (now - period.period).toInstant()
}

/**
 * Subtract a trading [period] from an instant
 */
operator fun Instant.plus(period: TradingPeriod): Instant {
    val zoneId = Config.defaultZoneId
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
