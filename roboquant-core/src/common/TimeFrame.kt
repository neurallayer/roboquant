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


/**
 * A time-frame represents a period of time defined by a [start] time (inclusive) and [end] time (exclusive). The
 * time-frame instance is immutable.  * Like all time related data in roboquant, it uses the [Instant] type to define
 * a moment in time, in order to avoid potential timezone inconsistencies.
 *
 * It can be used to limit the duration of a run to that specific time-frame, for example in a walk-forward. It can also
 * serve to limit a live-feed to a certain duration.
 */
data class TimeFrame(val start: Instant, val end: Instant) {

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
         * Full timeframe, matches any time and is typically used when no filtering is required
         */
        val FULL = TimeFrame(Instant.MIN, Instant.MAX)

        private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val secondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Time-frames for some significant events in history of trading
        fun blackMonday1987() = parse("1987-10-19T14:30:00Z", "1987-10-19T21:00:00Z")
        fun financialCrisis2008() = parse("2008-09-08T00:00:00Z", "2009-03-10T00:00:00Z")
        fun tenYearBullMarket2009() = parse("2009-03-10T00:00:00Z", "2019-03-10T00:00:00Z")
        fun flashCrash2010() = parse("2010-05-06T19:30:00Z", "2010-05-06T20:15:00Z")
        fun coronaCrash2020() = parse("2020-02-17T00:00:00Z", "2020-03-17T00:00:00Z")

        /**
         * Create a timeframe starting from 1 january of the [first] year until 31 december from the [last] year.
         */
        fun fromYears(first: Int, last: Int, zoneId: ZoneId = ZoneId.of("UTC")): TimeFrame {
            require(last >= first)
            val start = ZonedDateTime.of(first, 1, 1, 0, 0, 0, 0, zoneId)
            val stop = ZonedDateTime.of(last + 1, 1, 1, 0, 0, 0, 0, zoneId)
            return TimeFrame(start.toInstant(), stop.toInstant())
        }

        /**
         * Create a timeframe based on the [first] and [last] time provided. The times are to be provided as a string
         * and should be parsable by [Instant.parse]
         *
         * If the time component is omitted, the provided strings will be appended first with "T00:00:00" before
         * being parsed.
         */
        fun parse(first: String, last: String): TimeFrame {
            val f1 = if (first.length == 10) first + "T00:00:00Z" else first
            val f2 = if (last.length == 10) last + "T00:00:00Z" else last
            val start = Instant.parse(f1)
            val stop = Instant.parse(f2)
            return TimeFrame(start, stop)
        }

        /**
         * Create a time-frame from now for the previous period.
         */
        fun past(period: TemporalAmount): TimeFrame {
            val end = Instant.now()
            return TimeFrame(end - period, end)
        }

        /**
         * Create a time-frame from now for the provided duration. This is useful to restrict a live feed so it
         * won't run forever.
         *
         *      val tf = TimeFrame.nexy(60.minutes)
         *      roboquant.run(feed, tf)
         *
         */
        fun next(period: TemporalAmount): TimeFrame {
            val start = Instant.now()
            return TimeFrame(start, start + period)
        }


    }

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
    fun extend(before: TemporalAmount, after: TemporalAmount = before) = TimeFrame(start - before, end + after)


    /**
     * Calculate the intersection of this time-frame with an [other] time-frame and return
     * the resulting time-frame
     */
    fun intersect(other: TimeFrame): TimeFrame {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return TimeFrame(startTime, stopTime)
    }

    /**
     * Is there any overlap between this timeframe and the [other] timeframe
     */
    fun overlap(other: TimeFrame): Boolean {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return stopTime > startTime
    }

    /**
     * Calculate the union of this timeframe with the [other] timeframe and return
     * the resulting time-frame
     */
    fun union(other: TimeFrame): TimeFrame {
        val startTime = if (start > other.start) other.start else start
        val stopTime = if (end < other.end) other.end else end
        return TimeFrame(startTime, stopTime)
    }

    /**
     * Convert a time-frame to a timeline of individual days, optionally [excludeWeekends]
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
    fun splitTrainTest(testSize: Double): Pair<TimeFrame, TimeFrame> {
        assert(testSize < 1.0)
        val diff = end.toEpochMilli() - start.toEpochMilli()
        val train = (diff * (1.0 - testSize)).toLong()
        val border = start.plus(train, ChronoUnit.MILLIS)
        return Pair(TimeFrame(start, border), TimeFrame(border, end))
    }


    /**
     * Split a timeframe in multiple individual timeframes each of the fixed [period] length. One common use case is
     * to create timeframes that can be used in a walk forward back-test.
     */
    fun split(period: TemporalAmount): List<TimeFrame> {
        val utc = ZoneOffset.UTC
        val start = LocalDateTime.ofInstant(start, utc)
        val stop = LocalDateTime.ofInstant(end, utc)

        val result = mutableListOf<TimeFrame>()
        var offset = start
        while (offset < stop) {
            var end = offset + period
            if (end > stop) end = stop
            val timeFrame = TimeFrame(offset.toInstant(utc), end.toInstant(utc))
            result.add(timeFrame)
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
     * Subtract a [period] from this time-frame and return the result
     *
     *      val newTimeFrame = timeFrame - 2.days
     */
    operator fun minus(period: TemporalAmount) = TimeFrame(start - period, end - period)

    /**
     * Add a [period] to this time-frame and return the result.
     *
     *      val newTimeFrame = timeFrame + 2.days
     */
    operator fun plus(period: TemporalAmount) = TimeFrame(start + period, end + period)


}

