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
 * A timeframe represents a period of time defined by a [start] time (inclusive) and [end] time (exclusive). A
 * timeframe instance is immutable.  Like all time related logic in roboquant, it uses the [Instant] type to define
 * a moment in time, in order to avoid potential timezone inconsistencies.
 *
 * The internal logic uses milliseconds as the the smallest difference between two times.
 *
 * It can be used to limit the duration of a run to that specific timeframe, for example in a walk-forward. It can also
 * serve to limit a live-feed to a certain duration.
 */
data class Timeframe(val start: Instant, val end: Instant) {

    /**
     * Duration of timeframe
     */
    val duration: Duration
        get() = Duration.between(start, end)


    init {
        require(end > start) { "end time has to be larger than start time, found $start - $end" }
    }

    companion object {

        /**
         * Infinite timeframe that matches any time and is typically used when no filtering is required or the
         * timeframe for a number of events is unknown.
         */
        val INFINITY = Timeframe(Instant.MIN, Instant.MAX)

        private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val minutesFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val secondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val millisFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        // predefined Time-frames for significant events in history of trading

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
         * After the finincial crisis of 2008-2009, a ten year period of mostly a bullish market started.
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
         * After is became clear that COVID-19 virus would also impact countries outside China, many exchanges worldwide
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
            return Timeframe(start.toInstant(), stop.toInstant() - 1)
        }

        /**
         * Create a timeframe based on the [first] and [last] time provided. The times are to be provided as a string
         * and should be parsable by [Instant.parse]
         *
         * If the time component is omitted, the provided strings will be appended first with "T00:00:00Z" before
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
         *      val tf = TimeFrame.next(60.minutes)
         *      roboquant.run(feed, tf)
         *
         */
        fun next(period: TemporalAmount): Timeframe {
            val start = Instant.now()
            return Timeframe(start, start + period)
        }

        /**
         * Create a [Timeframe] with both the [start] and the [end] time being inclusive.
         */
        fun inclusive(start: Instant, end: Instant): Timeframe {
            return Timeframe(start, end + 1)
        }

    }

    /**
     * Return a timeframe inclusive of the [end] value.
     */
    val inclusive
        get() = Timeframe(start, end + 1)

    /**
     * Does the timeframe contain a certain [time].
     */
    operator fun contains(time: Instant): Boolean {
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
     * Is there an overlap between this timeframe and an [other] timeframe
     */
    fun overlap(other: Timeframe): Boolean {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return stopTime > startTime
    }

    /**
     * Calculate the union of this timeframe with an [other] timeframe and return
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
    fun toDays(excludeWeekends: Boolean = false, zoneId: ZoneId = Config.defaultZoneId): Timeline {
        val timeline = mutableListOf<Instant>()
        var offset = start
        val oneDay = 1.days
        val weekend = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        while (offset <= end) {
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
     * Convert a timeframe to a timeline of Period sized [steps][step] and only include times according to the
     * provided [exchange] trading hours.
     *
     * Usage:
     *
     *      timeframe.toTimeline(1.days, Exchange.AEB)
     */
    fun toTimeline(step: Period, exchange: Exchange = Exchange.DEFAULT): Timeline {
        val timeline = mutableListOf<Instant>()
        var time = start
        while (time < end) {
            if (exchange.isTrading(time)) timeline.add(time)
            time += step
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
    ): Timeline {
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
     *
     * It return a [Pair] of timeframes, the first one being the training timeframe and the second being the
     * test timefame.
     */
    fun splitTrainTest(testSize: Double): Pair<Timeframe, Timeframe> {
        require(testSize in 0.0..1.0) {"Test size has to between 0 and 1" }
        val diff = end.toEpochMilli() - start.toEpochMilli()
        val train = (diff * (1.0 - testSize)).toLong()
        val border = start.plus(train, ChronoUnit.MILLIS)
        return Pair(Timeframe(start, border), Timeframe(border, end))
    }


    /**
     * Split a timeframe in multiple individual timeframes each of the fixed [period] length. One common use case is
     * to create timeframes that can be used in a walk forward back-test.
     */
    fun split(period: TemporalAmount, zoneId: ZoneId = Config.defaultZoneId): List<Timeframe> {
        val start =  ZonedDateTime.ofInstant(start, zoneId)
        val stop = ZonedDateTime.ofInstant(end, zoneId)

        val result = mutableListOf<Timeframe>()
        var offset = start
        while (offset < stop) {
            var end = offset + period
            if (end > stop) end = stop
            val timeframe = Timeframe(offset.toInstant(), end.toInstant())
            result.add(timeframe)
            offset = end
        }
        return result
    }

    /**
     * Provide a string representation of the timeframe
     */
    fun toRawString(): String {
        return "[$start - $end]"
    }

    /**
     * Depending on the duration of the timeframe, format the timeframe either with seconds or days
     * resolution.
     */
    override fun toString(): String {
        val formatter = when {
            duration < 1.hours -> millisFormatter
            duration < 4.hours -> secondFormatter
            duration < 100.hours -> minutesFormatter
            else -> dayFormatter
        }

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
     * Annualize a [percentage] based on the duration of this timeframe. So given x percent profit
     * during a timeframe, what would be the profit per year.
     *
     * [percentage] is expected to be provided as a fraction, for example 1% is 0.01
     */
    fun annualize(percentage: Double): Double {
        val period = end.toEpochMilli() - start.toEpochMilli()
        val years = (365.0 * 24.0 * 3600.0 * 1000.0) / period
        return (1.0 + percentage).pow(years) - 1.0
    }

}

