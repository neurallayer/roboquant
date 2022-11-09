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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import kotlin.math.pow

/**
 * A timeframe represents a period of time defined by a [start] time (inclusive) and [end] time. If [inclusive] is set
 * to true the [end] time is inclusive, exclusive otherwise. The default is that the [end] time is exclusive.
 *
 * A timeframe instance is immutable.  Like all time related logic in roboquant, it uses the [Instant] type to define
 * a moment in time, in order to avoid potential timezone inconsistencies.
 *
 * All internal trading logic uses nanoseconds as the smallest difference between two times. However, some
 * visualizations and charts might use milliseconds and the smallest time differences.
 *
 * It can be used to limit the duration of a run to that specific timeframe, for example in a walk-forward. It can also
 * serve to limit a live-feed to a certain duration.
 *
 * @property start start time of timeframe, this is always inclusive
 * @property end end time of timeframe
 * @property inclusive should te [end] time be inclusive, default is false
 */
data class Timeframe(val start: Instant, val end: Instant, val inclusive: Boolean = false) {

    /**
     * Duration of timeframe
     */
    val duration: Duration
        get() = Duration.between(start, end)

    init {
        require(end >= start) { "end time has to be larger or equal than start time, found $start - $end" }
        require(start >= MIN) { "start time has to be larger or equal than $MIN" }
        require(end <= MAX) { "end time has to be smaller or equal than $MAX" }
    }

    /**
     * Is this an infinite timeframe
     */
    fun isInfinite() = this == INFINITE

    /**
     * @suppress
     */
    companion object {
        /**
         * Minimum start date of a timeframe
         */
        val MIN: Instant = Instant.parse("1900-01-01T00:00:00Z")

        /**
         * Maximum end date of a timeframe
         */
        val MAX: Instant = Instant.parse("2200-01-01T00:00:00Z")

        private const val ONE_YEAR_MILLIS = 365.0 * 24.0 * 3600.0 * 1000.0

        /**
         * Infinite timeframe that matches any time and is typically used when no filtering is required or the
         * exact timeframe is yet unknown.
         */
        val INFINITE = Timeframe(MIN, MAX)

        // Different formatters used when displaying a timeframe
        private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val minutesFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val hoursFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
        private val secondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val millisFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        // *******************************************************************
        // predefined timeframes for significant events in history of trading
        // *******************************************************************

        /**
         * Black Monday is the name given to the global, and largely unexpected stock market crash on October 19, 1987.
         * In some timezones it is also known as Black Tuesday.
         */
        val blackMonday1987
            get() = parse("1987-10-19T14:30:00Z", "1987-10-19T21:00:00Z")

        /**
         * The Financial Crisis of 2008 was a historic systemic risk event. Several financial institutions collapsed,
         * stock markets plunged, and the world entered a severe recession.
         */
        val financialCrisis2008
            get() = parse("2008-09-08T00:00:00Z", "2009-03-10T00:00:00Z")

        /**
         * After the financial crisis of 2008-2009, a ten-year period of mostly a bullish market started.
         */
        val tenYearBullMarket2009
            get() = parse("2009-03-10T00:00:00Z", "2019-03-10T00:00:00Z")

        /**
         * The 2010 flash crash is the market crash that occurred on May 6, 2010. During this crash, leading
         * US stock indices tumbled but also partially rebounded in less than an hour.
         */
        val flashCrash2010
            get() = parse("2010-05-06T19:30:00Z", "2010-05-06T20:15:00Z")

        /**
         * After it became clear that COVID-19 virus would also impact countries outside China, many exchanges worldwide
         * crashed due to the uncertainty of the impact that the virus would have on economies and companies.
         */
        val coronaCrash2020
            get() = parse("2020-02-17T00:00:00Z", "2020-03-17T00:00:00Z")

        /**
         * Create a timeframe starting from 1 january of the [first] year until 31 december from the [last] year.
         */
        fun fromYears(first: Int, last: Int, zoneId: ZoneId = Config.defaultZoneId): Timeframe {
            require(last >= first)
            val start = ZonedDateTime.of(first, 1, 1, 0, 0, 0, 0, zoneId)
            val stop = ZonedDateTime.of(last + 1, 1, 1, 0, 0, 0, 0, zoneId)
            return Timeframe(start.toInstant(), stop.toInstant())
        }

        /**
         * String representation that caters for different durations.
         */
        private fun String.toInstant(): Instant {
            val fStr = when (length) {
                4 -> "$this-01-01T00:00:00Z"
                7 -> "$this-01T00:00:00Z"
                10 -> "${this}T00:00:00Z"
                19 -> "${this}Z"
                else -> this
            }
            return Instant.parse(fStr)
        }

        /**
         * Create a timeframe based on the [first] and [last] time provided. The times are to be provided as a string
         * and the following formats are supported:
         * 1. Year only
         * 2. Year and month
         * 3. Year, month and day
         * 4. Full date and time
         *
         * When not a complete datetime is provided, the missing part is the first possible time. So for example 2004
         * becomes 2004-01-01T00:00:00Z
         */
        fun parse(first: String, last: String): Timeframe {
            val start = first.toInstant()
            val stop = last.toInstant()
            return Timeframe(start, stop)
        }

        /**
         * Create a timeframe from now - [period] to now
         */
        fun past(period: TemporalAmount): Timeframe {
            val end = Instant.now()
            return Timeframe(end - period, end)
        }

        /**
         * Create a timeframe from now for the provided duration. This is useful to restrict a live feed, so it
         * won't run forever.
         *
         *      val tf = TimeFrame.next(60.minutes)
         *      roboquant.run(feed, tf)
         */
        fun next(period: TemporalAmount): Timeframe {
            val start = Instant.now()
            return Timeframe(start, start + period)
        }

    }

    /**
     * Is the provided [time] still before the end of this timeframe
     */
    private fun beforeEnd(time: Instant): Boolean {
        return time < end || (inclusive && time <= end)
    }


    /**
     * Return a new timeframe inclusive of the [end] value.
     */
    fun toInclusive() = Timeframe(start, end, true)

    /**
     * Does the timeframe contain the provided [time].
     */
    operator fun contains(time: Instant): Boolean {
        return (time >= start) && (beforeEnd(time))
    }

    /**
     * Is this timeframe within a single day given the provided [zoneId].
     */
    fun isSingleDay(zoneId: ZoneId = Config.defaultZoneId): Boolean {
        if (start == Instant.MIN || end == Instant.MAX) return false
        val realEnd = if (inclusive) end else end.minusNanos(1)
        return start.atZone(zoneId).toLocalDate() == realEnd.atZone(zoneId).toLocalDate()
    }


    private fun Instant.add(temporalAmount: TemporalAmount) : Instant {
        val zoneId = Config.defaultZoneId
        val now = this.atZone(zoneId)
        return (now + temporalAmount).toInstant()
    }


    private fun Instant.subtract(temporalAmount: TemporalAmount) : Instant {
        val zoneId = Config.defaultZoneId
        val now = this.atZone(zoneId)
        return (now - temporalAmount).toInstant()
    }


    /**
     * Extend a timeframe, both [before] and [after] the current timeframe. The current timeframe
     * remains unchanged and a new one is returned.
     *
     * ### Usage
     *      // Add 1 week before and after the black monday event
     *      val tf = TimeFrame.BlackMonday1987.extend(1.weeks)
     */
    fun extend(before: TemporalAmount, after: TemporalAmount = before) =
        Timeframe(start.subtract(before), end.add(after), inclusive)

    /**
     * Convert a timeframe to a timeline where each time is [step] size separated
     *
     * Usage:
     *      timeframe.toTimeline(1.days)
     */
    fun toTimeline(step: TemporalAmount): Timeline {
        val timeline = mutableListOf<Instant>()
        var time = start
        while (time <= this) {
            timeline.add(time)
            time = time.add(step)
        }
        return timeline
    }

    /**
     * Split a timeframe into two parts, one for training and one for test using the provided [testSize]
     * for determining the size of test. [testSize] should be a number between 0.0 and 1.0, for example
     * 0.25 means use last 25% as test timeframe.
     *
     * It returns a [Pair] of timeframes, the first one being the training timeframe and the second being the
     * test timeframe.
     */
    fun splitTrainTest(testSize: Double): Pair<Timeframe, Timeframe> {
        require(testSize in 0.0..1.0) { "Test size has to between 0.0 and 1.0" }
        val diff = duration.toMillis()
        val train = (diff * (1.0 - testSize)).toLong()
        val border = start.plus(train, ChronoUnit.MILLIS)
        return Pair(Timeframe(start, border), Timeframe(border, end, inclusive))
    }

    /**
     * Split a timeframe in multiple individual timeframes each of the fixed [period] length. One common use case is
     * to create timeframes that can be used in a walk forward back-test.
     */
    fun split(period: TemporalAmount): List<Timeframe> {
        val result = mutableListOf<Timeframe>()
        var last = start
        while (true) {
            val next = last.add(period)
            if (! beforeEnd(next)) {
                result.add(Timeframe(last, end, inclusive))
                return result
            }
            val timeframe = Timeframe(last, next)
            result.add(timeframe)
            last = next
        }
    }

    /**
     * Provide a string representation of the timeframe
     */
    fun toRawString(): String {
        val lastChar = if (inclusive) ']' else '>'
        return "[$start - $end$lastChar"
    }

    /**
     * Depending on the duration of the timeframe, format the timeframe either with milli, seconds, minutes or days
     * resolution.
     */
    override fun toString(): String {
        val d = duration.toSeconds()
        val formatter = when {
            d < 1 -> millisFormatter // less than 1 second
            d < 60 -> secondFormatter // less than 1 minute
            d < 3600 -> minutesFormatter // less than 1 hour
            d < 3600 * 24 -> hoursFormatter // less than 1 day
            else -> dayFormatter
        }

        val fmt = formatter.withZone(Config.defaultZoneId)
        val s1 = if (start == MIN) "MIN" else if (start == MAX) "MAX" else fmt.format(start)
        val s2 = if (end == MIN) "MIN" else if (end == MAX) "MAX" else fmt.format(end)
        return "$s1 - $s2"
    }

    /**
     * Subtract a [period] from this timeframe and return the result
     *
     *      val newTimeFrame = timeframe - 2.days
     */
    operator fun minus(period: TemporalAmount) = Timeframe(start.subtract(period), end.subtract(period), inclusive)

    /**
     * Add a [period] to this timeframe and return the result.
     *
     *      val newTimeFrame = timeframe + 2.days
     */
    operator fun plus(period: TemporalAmount) = Timeframe(start.add(period), end.add(period), inclusive)

    /**
     * Annualize a [percentage] based on the duration of this timeframe. So given x percent return
     * during a timeframe, what would be the return for a full year. If this timeframe has higher than milliseconds
     * precision, the remaining will not be used.
     *
     * [percentage] is expected to be provided as a fraction, for example 1% is 0.01
     */
    fun annualize(percentage: Double): Double {
        val period = duration.toMillis()
        val years = ONE_YEAR_MILLIS / period
        return (1.0 + percentage).pow(years) - 1.0
    }

}

