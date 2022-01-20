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

package org.roboquant.common

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import kotlin.math.pow


/**
 * A timeframe represents a period of time defined by a [start] time (inclusive) and [end] time (exclusive). The
 * timeframe instance is immutable.  * Like all time related data in roboquant, it uses the [Instant] type to define
 * a moment in time, in order to avoid potential timezone inconsistencies.
 *
 * It can be used to limit the duration of a run to that specific timeframe, for example in a walk-forward. It can also
 * serve to limit a live-feed to a certain duration.
 */
data class Timeframe(val start: Instant, val end: Instant) {

    /**
     * Duration of TimeFrame
     */
    val duration: Duration
        get() = Duration.between(start, end)


    init {
        require(end > start) { "end time has to be larger than start time, found $start - $end" }
    }

    companion object {

        /**
         * Infinity timeframe matches any time and is typically used when no filtering is required
         */
        val INFINITY = Timeframe(Instant.MIN, Instant.MAX)

        private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val secondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // predefined Time-frames for significant events in history of trading

        /**
         * Black Monday is the name given to the global, sudden, severe, and largely unexpected stock market
         * crash on October 19, 1987. All of the twenty-three major world markets experienced a sharp decline.
         */
        val blackMonday1987
            get() = parse("1987-10-19T14:30:00Z", "1987-10-19T21:00:00Z")

        /**
         * The Financial Crisis of 2008 was a historic systemic risk event. Prominent financial institutions collapsed,
         * credit markets seized up, stock markets plunged, and the world entered a severe recession.
         */
        val financialCrisis2008
            get() = parse("2008-09-08T00:00:00Z", "2009-03-10T00:00:00Z")

        /**
         * After the finincial crisis of 2008-2009, a ten year period of mostly a bullish market started
         */
        val tenYearBullMarket2009
            get() = parse("2009-03-10T00:00:00Z", "2019-03-10T00:00:00Z")

        /**
         * The 2010 flash crash is the market crash that occurred on May 6, 2010. During the crash, leading
         * US stock indices tumbled but also partially rebounded in less than an hour. This was a day with high
         * volatilty accross asset classes.
         */
        val flashCrash2010
            get() = parse("2010-05-06T19:30:00Z", "2010-05-06T20:15:00Z")

        /**
         * After is became clear that COVID-19 virus would also impact countries outside China, many exchanges worldwide
         * crashed due to the uncertainty the virus would have on economies and companies.
         */
        val coronaCrash2020
            get() = parse("2020-02-17T00:00:00Z", "2020-03-17T00:00:00Z")

        /**
         * Create a timeframe starting from 1 january of the [first] year until 31 december from the [last] year.
         */
        fun fromYears(first: Int, last: Int, zoneId: ZoneId = ZoneId.of("UTC")): Timeframe {
            require(last >= first)
            val start = ZonedDateTime.of(first, 1, 1, 0, 0, 0, 0, zoneId)
            val stop = ZonedDateTime.of(last + 1, 1, 1, 0, 0, 0, 0, zoneId)
            return Timeframe(start.toInstant(), stop.toInstant())
        }

        /**
         * Create a timeframe based on the [first] and [last] time provided. The times are to be provided as a string
         * and should be parsable by [Instant.parse]
         *
         * If the time component is omitted, the provided strings will be appended first with "T00:00:00" before
         * being parsed.
         */
        fun parse(first: String, last: String): Timeframe {
            val f1 = if (first.length == 10) first + "T00:00:00Z" else first
            val f2 = if (last.length == 10) last + "T00:00:00Z" else last
            val start = Instant.parse(f1)
            val stop = Instant.parse(f2)
            return Timeframe(start, stop)
        }

        /**
         * Create a timeframe from now for the previous period.
         */
        fun past(period: TemporalAmount): Timeframe {
            val end = Instant.now()
            return Timeframe(end - period, end)
        }

        /**
         * Create a timeframe from now for the provided duration. This is useful to restrict a live feed so it
         * won't run forever.
         *
         *      val tf = TimeFrame.nexy(60.minutes)
         *      roboquant.run(feed, tf)
         *
         */
        fun next(period: TemporalAmount): Timeframe {
            val start = Instant.now()
            return Timeframe(start, start + period)
        }


    }

    /**
     * A timeframe inclusive of the [end] value
     */
    val inclusive
        get() = Timeframe(start, end + 1)

    /**
     * Does the timeframe contain a certain [time].
     */
    fun contains(time: Instant): Boolean {
        return (time >= start) && (time < end)
    }

    /**
     * Is this timeframe in a single day given the provided [zoneId].
     */
    fun isSingleDay(zoneId: ZoneId = Config.defaultZoneId): Boolean {
        if (start == Instant.MIN || end == Instant.MAX) return false
        return start.atZone(zoneId).toLocalDate() == end.minusMillis(1).atZone(zoneId).toLocalDate()
    }


    /**
     * Extend a timeframe, both [before] and [after] the current timeframe. The current timeframe
     * remains unchanged and a new one is returned.
     *
     * ### Usage
     *      // Add 1 week before and after the black monday event
     *      val tf = TimeFrame.BlackMonday1987().extend(1.weeks)
     *
     */
    fun extend(before: TemporalAmount, after: TemporalAmount = before) = Timeframe(start - before, end + after)


    /**
     * Calculate the intersection of this timeframe with an [other] timeframe and return
     * the resulting timeframe
     */
    fun intersect(other: Timeframe): Timeframe {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return Timeframe(startTime, stopTime)
    }

    /**
     * Is there any overlap between this timeframe and the [other] timeframe
     */
    fun overlap(other: Timeframe): Boolean {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return stopTime > startTime
    }

    /**
     * Calculate the union of this timeframe with the [other] timeframe and return
     * the resulting timeframe
     */
    fun union(other: Timeframe): Timeframe {
        val startTime = if (start > other.start) other.start else start
        val stopTime = if (end < other.end) other.end else end
        return Timeframe(startTime, stopTime)
    }

    /**
     * Convert a timeframe to a timeline of individual days, optionally [excludeWeekends]
     */
    fun toDays(excludeWeekends: Boolean = false, zoneId: ZoneId = ZoneOffset.UTC): List<Instant> {
        val timeline = mutableListOf<Instant>()
        var offset = start
        val oneDay = Period.ofDays(1)
        val weekend = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        while (offset < end) {
            if (excludeWeekends) {
                val zdt = ZonedDateTime.ofInstant(offset, zoneId)
                if (zdt.dayOfWeek !in weekend) timeline.add(offset)
            } else {
                timeline.add(offset)
            }
            offset += oneDay
        }
        return timeline
    }


    /**
     * Convert a timeframe to a timeline (is a list of Instant values) with one minute intervals. Optionally specify a
     * different [startTime] and [endTime] in a day given the provided [zoneId]. You can also optionally exclude
     * weekends by setting [excludeWeekends] to true.
     */
    fun toMinutes(
        excludeWeekends: Boolean = false,
        zoneId: ZoneId = ZoneOffset.UTC,
        startTime: LocalTime = LocalTime.parse("09:30:00"),
        endTime: LocalTime = LocalTime.parse("16:00:00")
    ): List<Instant> {
        val timeline = mutableListOf<Instant>()
        var offset = start
        val weekend = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        while (offset <= end) {
            val zdt = ZonedDateTime.ofInstant(offset, zoneId)
            val time = LocalTime.of(zdt.hour, zdt.minute, zdt.second)
            if ((time >= startTime) and (time < endTime)) {
                if (excludeWeekends) {
                    if (zdt.dayOfWeek !in weekend)
                        timeline.add(offset)
                } else {
                    timeline.add(offset)
                }
            }
            offset = offset.plus(1, ChronoUnit.MINUTES)
        }
        return timeline
    }


    /**
     * Split a timeframe into two parts, one for training and one for test using the provided [testSize]
     * for determining the size of test. [testSize] should be a number between 0.0 and 1.0, for example
     * 0.25 means use last 25% as test timeframe.
     */
    fun splitTrainTest(testSize: Double): Pair<Timeframe, Timeframe> {
        assert(testSize < 1.0)
        val diff = end.toEpochMilli() - start.toEpochMilli()
        val train = (diff * (1.0 - testSize)).toLong()
        val border = start.plus(train, ChronoUnit.MILLIS)
        return Pair(Timeframe(start, border), Timeframe(border, end))
    }


    /**
     * Split a timeframe in multiple individual timeframes each of the fixed [period] length. One common use case is
     * to create timeframes that can be used in a walk forward back-test.
     */
    fun split(period: TemporalAmount): List<Timeframe> {
        val utc = ZoneOffset.UTC
        val start = LocalDateTime.ofInstant(start, utc)
        val stop = LocalDateTime.ofInstant(end, utc)

        val result = mutableListOf<Timeframe>()
        var offset = start
        while (offset < stop) {
            var end = offset + period
            if (end > stop) end = stop
            val timeframe = Timeframe(offset.toInstant(utc), end.toInstant(utc))
            result.add(timeframe)
            offset += period
        }
        return result
    }

    /**
     * Provide a nicer string representation of the timeframe
     */
    override fun toString(): String {
        return "[$start - $end]"
    }

    /**
     * Depending on the duration of the timeframe, format the timeframe either with seconds or days
     * resolution.
     */
    fun toPrettyString(): String {
        val formatter = if (duration < Duration.ofHours(24)) secondFormatter else dayFormatter
        val fmt = formatter.withZone(Config.defaultZoneId)
        val s1 = if (start == Instant.MIN) "MIN" else if (start == Instant.MAX) "MAX" else fmt.format(start)
        val s2 = if (end == Instant.MIN) "MIN" else if (end == Instant.MAX) "MAX" else fmt.format(end)
        return "$s1 - $s2"
    }

    /**
     * Subtract a [period] from this timeframe and return the result
     *
     *      val newTimeFrame = timeframe - 2.days
     */
    operator fun minus(period: TemporalAmount) = Timeframe(start - period, end - period)

    /**
     * Add a [period] to this timeframe and return the result.
     *
     *      val newTimeFrame = timeframe + 2.days
     */
    operator fun plus(period: TemporalAmount) = Timeframe(start + period, end + period)


    /**
     * Annualize a [percentage] based on the duration of this timeframe. So if I make x percent profit
     * during a timeframe, what would be my profit per year.
     *
     * [percentage] is expected to be provided as a fraction, so 1% is 0.01
     */
    fun annualize(percentage: Double): Double {
        val period = duration.toMillis()
        val years = (365.0 * 24.0 * 3600.0 * 1000.0) / period
        return (1.0 + percentage).pow(years) - 1.0
    }

}


