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


/**
 * A timeframe represents a period of time defined by a [start] time (inclusive) and [end] time (exclusive). It can be
 * used to limit the duration of a run to that specific timeframe.
 *
 * Like all time related data in roboquant, it uses the [Instant] type to define a moment in time, in order to avoid
 * potential timezone inconsistencies.
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

        private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)
        private val secondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)

        // Time-frames for some significant events in history of trading
        fun blackMonday1987() = parse("1987-10-19T00:00:00Z", "1987-10-20T00:00:00Z")
        fun financialCrisis2008() = parse("2008-09-08T00:00:00Z", "2009-03-10T00:00:00Z")
        fun tenYearBullMarket2009() = parse("2009-03-10T00:00:00Z", "2019-03-10T00:00:00Z")
        fun flashCrash2010() = parse("2010-05-06T00:00:00Z", "2010-05-07T00:00:00Z")
        fun coronaCrash2020() = parse("2020-02-17T00:00:00Z", "2020-03-17T00:00:00Z")

        /**
         * Create a timeframe starting from 1 january of the [first] year until 31 december from the [last] year.
         */
        fun fromYears(first: Int, last: Int): TimeFrame {
            val start = Instant.parse("${first}-01-01T00:00:00Z")
            var stop = Instant.parse("${last + 1}-01-01T00:00:00Z")
            if (stop <= start) stop = start
            return TimeFrame(start, stop)
        }

        /**
         * Create a timeframe based on the [first] and [last] time provided. The times are to be provided as a string
         * and should be parsable by [Instant.parse]
         */
        fun parse(first: String, last: String): TimeFrame {
            val start = Instant.parse(first)
            val stop = Instant.parse(last)
            return TimeFrame(start, stop)
        }

        /*
        fun pastPeriod(duration: Long, unit: ChronoUnit = ChronoUnit.DAYS): TimeFrame {
            val end = Instant.now()
            val start = end.minus(duration, unit)
            return TimeFrame(start, end)
        }
         */

        fun pastPeriod(duration: Long, unit: ChronoUnit = ChronoUnit.DAYS): TimeFrame {
            val end = Instant.now()
            val ts = end.atZone(ZoneId.of("UTC"))
            val start = ts.minus(duration, unit).toInstant()
            return TimeFrame(start, end)
        }

        fun nextPeriod(duration: Long, unit: ChronoUnit = ChronoUnit.DAYS): TimeFrame {
            val start = Instant.now()
            val ts = start.atZone(ZoneId.of("UTC"))
            val end = ts.plus(duration, unit).toInstant()
            return TimeFrame(start, end)
        }

        /**
         * Create a timeframe starting with the current time and last for the specified number of [minutes]. This comes in
         * handy when you want to test a strategy against a live feed for a certain duration.
         *
         * See also [nextPeriod] for a more generic version of this method
         *
         * # Example
         *      roboquant.run(feed, TimeFrame.nextMinutes(30))
         *
         */
        fun nextMinutes(minutes: Long): TimeFrame = nextPeriod(minutes, ChronoUnit.MINUTES)

    }

    /**
     * Does the timeframe contain a certain [time].
     */
    fun contains(time: Instant): Boolean {
        return (time >= start) && (time < end)
    }


    fun minus(amount:Long, unit: ChronoUnit = ChronoUnit.MINUTES) : TimeFrame {
        return TimeFrame(start.minus(amount, unit), end.minus(amount, unit))
    }

    /**
     * Is this timeframe in a single day given the provided [zoneId].
     */
    fun isSingleDay(zoneId: String = "UTC"): Boolean {
        val z = ZoneId.of(zoneId)
        return start.atZone(z).toLocalDate() == end.minusMillis(1).atZone(z).toLocalDate()
    }


    /**
     * Extend a timeframe, both [before] and [after] the current timeframe. The current timeframe
     * remains unchanged and a new one is returned.
     *
     * ### Usage
     *      // Add 1 week before and after the black monday event
     *      val tf = TimeFrame.BlackMonday1987().extend(7)
     *
     */
    fun extend(before: Long, after: Long = before, unit: ChronoUnit = ChronoUnit.DAYS) {
        val s = start.atZone(ZoneId.of("UTC"))
        val e = end.atZone(ZoneId.of("UTC"))
        TimeFrame(s.minus(before, unit).toInstant(), e.plus(after, unit).toInstant())
    }

    /**
     * Calculate the intersection of this time-frame with an [other] timeframe and return
     * the new timeframe
     */
    fun intersect(other: TimeFrame): TimeFrame {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return TimeFrame(startTime, stopTime)
    }

    /**
     * Is there overlap between this timeframe and the [other] timeframe
     */
    fun overlap(other: TimeFrame): Boolean {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return stopTime > startTime
    }

    /**
     * Calculate the union of this timeframe with the [other] timeframe and return
     * the new timeframe
     */
    fun union(other: TimeFrame): TimeFrame {
        val startTime = if (start > other.start) other.start else start
        val stopTime = if (end < other.end) other.end else end
        return TimeFrame(startTime, stopTime)
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
                // val ldt = LocalDateTime.ofInstant(offset, zoneOffset)
                if (zdt.dayOfWeek !in weekend)
                    timeline.add(offset)
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
     * to create timeframes that can be used for walk forward back-test.
     */
    fun split(period: Period): List<TimeFrame> {
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
        val diff = end.epochSecond - start.epochSecond
        val formatter = if (diff > 24 * 60 * 60) dayFormatter else secondFormatter
        val s1 = if (start == Instant.MIN) "MIN" else if (start == Instant.MAX) "MAX" else formatter.format(start)
        val s2 = if (end == Instant.MIN) "MIN" else if (end == Instant.MAX) "MAX" else formatter.format(end)
        return "$s1 - $s2"
    }

}

