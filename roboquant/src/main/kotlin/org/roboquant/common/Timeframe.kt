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

import java.io.Serializable
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import kotlin.math.pow
import kotlin.random.Random

/**
 * A timeframe represents a period of time defined by a [start] time (inclusive) and [end] time. If [inclusive] is set
 * to true the [end] time is inclusive, exclusive otherwise. The default is that the [end] time is exclusive.
 *
 * A timeframe instance is immutable.  Like all time related logic in roboquant, it uses the [Instant] type to define
 * a moment in time, to avoid potential timezone inconsistencies.
 *
 * All internal trading logic uses nanoseconds as the smallest difference between two times. However, some
 * visualizations and charts might use milliseconds and the smallest time differences.
 *
 * It can be used to limit the duration of a run to that specific timeframe, for example, in a walk-forward. It can also
 * serve to limit a live-feed to a certain duration.
 *
 * @property start start time of timeframe, this is always inclusive
 * @property end end time of timeframe
 * @property inclusive should te [end] time be inclusive, default is false
 */
@Suppress("ReplaceCallWithBinaryOperator")
data class Timeframe(val start: Instant, val end: Instant, val inclusive: Boolean = false) : Serializable {

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
     * Return true is this is an infinite timeframe, false otherwise
     */
    fun isInfinite() = this == INFINITE

    /**
     * Return true is this being a finite timeframe, false otherwise
     */
    fun isFinite() = this != INFINITE

    /**
     * Return true is this is an empty timeframe, false otherwise
     */
    fun isEmpty() = start.equals(end) && !inclusive

    /**
     * @suppress
     */
    companion object {

        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 129021321L

        /**
         * The minimum start date of a timeframe, being 1900-01-01T00:00:00Z
         */
        val MIN: Instant = Instant.parse("1900-01-01T00:00:00Z")

        /**
         * The maximum end date of a timeframe, being 2200-01-01T00:00:00Z
         */
        val MAX: Instant = Instant.parse("2200-01-01T00:00:00Z")

        /**
         * 365 days expresses in milliseconds.
         */
        const val ONE_YEAR_MILLIS = 365.0 * 24.0 * 3600.0 * 1000.0

        /**
         * Infinite timeframe that matches any time and is typically used when no filtering is required or the
         * exact timeframe is yet unknown.
         */
        val INFINITE = Timeframe(MIN, MAX, true)

        /**
         * The empty timeframe doesn't contain any time.
         */
        val EMPTY = Timeframe(MIN, MIN, false)

        // Different formatters used when displaying a timeframe
        private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val secondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        private val milliFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        // **********************************************************************
        // predefined timeframes for significant events in the history of trading
        // **********************************************************************

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
         * After it became clear that the COVID-19 virus would also impact countries outside China, many exchanges
         * worldwide crashed due to the uncertainty of the impact it would have.
         */
        val coronaCrash2020
            get() = parse("2020-02-17T00:00:00Z", "2020-03-17T00:00:00Z")

        /**
         * Create a timeframe starting from 1 january of the [first] year until 1 january of
         * the [last] year (excluding).
         */
        fun fromYears(first: Int, last: Int, zoneId: ZoneId = ZoneOffset.UTC): Timeframe {
            val start = ZonedDateTime.of(first, 1, 1, 0, 0, 0, 0, zoneId)
            val stop = ZonedDateTime.of(last, 1, 1, 0, 0, 0, 0, zoneId)
            return Timeframe(start.toInstant(), stop.toInstant(), inclusive = false)
        }

        /**
         * Parse string into an [Instant], that caters for different precisions. The missing parts will be added before
         * the actual parsing.
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
         * Create a timeframe based on the [first] and [last] time provided. By default, the last time is
         * not [inclusive].
         *
         * The times are to be provided as a string with the following formats are supported:
         * 1. Year only
         * 2. Year and month
         * 3. Year, month and day
         * 4. Full date and time
         *
         * When not a complete datetime is provided, the missing part is the first possible time. For example, 2004
         * becomes 2004-01-01T00:00:00Z
         */
        fun parse(first: String, last: String, inclusive: Boolean = false): Timeframe {
            val start = first.toInstant()
            val stop = last.toInstant()
            return Timeframe(start, stop, inclusive)
        }

        /**
         * Create a timeframe till now minus the provided [period].
         */
        fun past(period: TimeSpan): Timeframe {
            val end = Instant.now()
            return Timeframe(end - period, end)
        }

        /**
         * Create a timeframe from now for the provided [period].
         *
         * This is useful to restrict a live feed, so it won't run forever.
         *```
         * val tf = TimeFrame.next(60.minutes)
         * roboquant.run(liveFeed, tf)
         * ```
         */
        fun next(period: TimeSpan): Timeframe {
            val start = Instant.now()
            return Timeframe(start, start + period)
        }

    }

    /**
     * Is the provided [time] before the end of this timeframe. For an [inclusive] timeframe, the [time] has to be
     * before or equal the end time.
     */
    private fun beforeEnd(time: Instant): Boolean {
        return time < end || (inclusive && time.equals(end))
    }

    /**
     * Return a new timeframe inclusive of the [end] value.
     */
    fun toInclusive() = Timeframe(start, end, true)

    /**
     * Does the timeframe contain the provided [time].
     */
    operator fun contains(time: Instant): Boolean {
        if (isInfinite()) return true
        return (time >= start) && (beforeEnd(time))
    }

    /**
     * Is this timeframe within a single day given the provided [zoneId].
     */
    fun isSingleDay(zoneId: ZoneId): Boolean {
        if (start.equals(Instant.MIN) || end.equals(Instant.MAX)) return false
        val realEnd = if (inclusive) end else end.minusNanos(1)
        return start.atZone(zoneId).toLocalDate().equals(realEnd.atZone(zoneId).toLocalDate())
    }

    /**
     * Convert this timeframe to a [Timeline] where each time seperated by a [step] amount.
     * If the temporalAmount is defined as a [Period], UTC will be used as the ZoneId.
     * Usage:
     * ```
     * timeframe.toTimeline(1.days)
     * ```
     */
    fun toTimeline(step: TimeSpan) = buildList {
        var time = start
        while (this@Timeframe.contains(time)) {
            add(time)
            time += step
        }
    }

    /**
     * Split a timeframe into two parts, one for training and one for test using the provided [testSize]
     * for determining the size of test. [testSize] should be a number between 0.0 and 1.0, for example,
     * 0.25 means use last 25% as a test timeframe.
     *
     * It returns a [Pair] of timeframes, the first one being the training timeframe and the second being the
     * test timeframe.
     */
    fun splitTwoWay(testSize: Double): Pair<Timeframe, Timeframe> {
        require(testSize in 0.0..1.0) { "Test size has to between 0.0 and 1.0" }
        val diff = duration.toMillis()
        val train = (diff * (1.0 - testSize)).toLong()
        val border = start.plus(train, ChronoUnit.MILLIS)
        return Pair(Timeframe(start, border), Timeframe(border, end, inclusive))
    }

    /**
     * Split a timeframe into two parts, using the [offset] as split. The [offset] period should be smaller than
     * the total duration of the timeframe. Optionally, an [overlap] can be provided, default is [TimeSpan.ZERO].
     *
     * It returns a [Pair] of timeframes.
     */
    fun splitTwoWay(offset: TimeSpan, overlap: TimeSpan = TimeSpan.ZERO): Pair<Timeframe, Timeframe> {
        val border = start + offset
        require(contains(border)) { "offset should be smaller than timeframe" }
        return Pair(Timeframe(start, border), Timeframe(border - overlap, end, inclusive))
    }

    /**
     * Split a timeframe in multiple individual timeframes each of the fixed [period] length.
     * Optionally, an [overlap] can be specified, with the default being zero.
     *
     * When [includeRemaining] is set to true, the remaining part that is smaller than the specified [period] will still
     * be included, default is true.
     *
     * One common use case is to create timeframes that can be used in a walk forward back-test.
     */
    fun split(period: TimeSpan, overlap: TimeSpan = TimeSpan.ZERO, includeRemaining: Boolean = true) = buildList {
        var begin = start
        var done = false
        while (!done) {
            val last = begin + period
            val tf = when {
                last < end -> Timeframe(begin, last)
                last.equals(end) && inclusive -> Timeframe(begin, end, true)
                includeRemaining -> Timeframe(begin, end, inclusive)
                else -> null
            }
            addNotNull(tf)
            done = tf == null || tf.end.equals(end)
            begin = last - overlap
        }
    }

    /**
     * Sample one or more timeframes each of a [period] length. Common use case is a Monte Carlo simulation. It uses
     * [resolution] for the start of timeframes, default being [ChronoUnit.DAYS].
     * The minimum resolution is milliseconds.
     *
     * The returned list will not contain duplicate timeframes (sampling without replacement).
     */
    fun sample(
        period: TimeSpan,
        samples: Int = 1,
        resolution: TemporalUnit? = period.resolution(),
        random: Random = Config.random,
    ): List<Timeframe> {
        require(samples >= 1) { "samples need to be >= 1" }
        require(end - period > start) { "period=$period too large for timeframe=$this" }
        val duration = Timeframe(start, end - period).duration.toMillis()
        require(duration > samples) { "not enough data to sample $samples" }
        val result = mutableSetOf<Timeframe>()
        while (result.size < samples) {
            val offset = random.nextLong(duration)
            var newStart = start.plusMillis(offset)
            if (resolution != null) newStart = newStart.truncatedTo(resolution)
            result.add(Timeframe(newStart, newStart + period))
        }
        return result.toList()
    }

    /**
     * Return a string representation of the timeframe
     */
    override fun toString(): String {
        val s1 = if (start.equals(MIN)) "MIN" else if (start.equals(MAX)) "MAX" else start.toString()
        val s2 = if (end.equals(MIN)) "MIN" else if (end.equals(MAX)) "MAX" else end.toString()
        val end = if (inclusive) ']' else '>'
        return "[$s1 - $s2$end"
    }

    /**
     * Depending on the duration of the timeframe, format the timeframe either with milli, seconds, minutes or days
     * resolution.
     */
    fun toPrettyString(): String {
        val d = duration.toSeconds()
        val formatter = when {
            d < 10 -> milliFormatter // less than 10 seconds
            d < 3600 * 24 -> secondFormatter // less than 1 day
            else -> dayFormatter
        }

        val fmt = formatter.withZone(ZoneOffset.UTC)
        val s1 = if (start.equals(MIN)) "MIN" else if (start.equals(MAX)) "MAX" else fmt.format(start)
        val s2 = if (end.equals(MIN)) "MIN" else if (end.equals(MAX)) "MAX" else fmt.format(end)
        return "$s1 - $s2"
    }

    /**
     * Ensure an instant is always within the MIN-MAX range
     */
    private fun makeValid(time: Instant) = time.coerceAtLeast(MIN).coerceAtMost(MAX)

    /**
     * Subtract a [period] from start- and end-time of this timeframe and return the result.
     * If the period contains timezone-dependent values, UTC will be used as the ZoneId.
     * ```
     * val newTimeFrame = timeframe - 2.days
     * ```
     */
    operator fun minus(period: TimeSpan): Timeframe {
        val e = makeValid(end - period)
        val incl = end.equals(MAX) || inclusive
        return Timeframe(makeValid(start - period), e, incl)
    }

    /**
     * Add a [period] to start- and end-time of this timeframe and return the result.
     * If the period contains timezone-dependent values, UTC will be used as the ZoneId.
     * ```
     * val newTimeFrame = timeframe + 2.days
     * ```
     */
    operator fun plus(period: TimeSpan): Timeframe {
        val e = makeValid(end + period)
        val incl = (end.equals(MAX)) || inclusive
        return Timeframe(makeValid(start + period), e, incl)
    }

    /**
     * Extend this period with a [before] and [after] period and return the result.
     * If no [after] period is provided, the same value as for [before] will be used.
     *
     * If [before] or [after] contain timezone-dependent values, UTC will be used as the ZoneId.
     */
    fun extend(before: TimeSpan, after: TimeSpan = before): Timeframe {
        val e = makeValid(end + after)
        val incl = (end.equals(MAX)) || inclusive
        return Timeframe(makeValid(start - before), e, incl)
    }

    /**
     * Annualize a [rate] based on the duration of this timeframe. So given x percent return
     * during a timeframe, what would be the return percentage for a full year (365 days). If this timeframe has higher
     * than millisecond precision, the remaining precision will not be used.
     *
     * [rate] is expected to be provided as a fraction — for example, 1% is 0.01
     */
    fun annualize(rate: Double): Double {
        return (1.0 + rate).pow(toYears()) - 1.0
    }

    /**
     * Convert this timeframe to years (using 365 days in a year).
     */
    private fun toYears(): Double {
        val period = duration.toMillis()
        return ONE_YEAR_MILLIS / period
    }

    /**
     * Compare timeframes based on their [duration]
     */
    operator fun compareTo(other: TimeSpan): Int {
        return duration.compareTo(other.duration)
    }


}

