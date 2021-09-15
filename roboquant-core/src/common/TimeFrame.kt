package org.roboquant.common

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Time-frame represents a period of time defined by a start-time (inclusive) and stop-time (exclusive). It can be used
 * to limit the evaluation of a run to that specific time-frame.
 *
 * Like all time related functionality in roboquant, it uses the [Instant] type to define a moment in time
 * in order to avoid potential timezone inconsistencies.
 *
 * @property start The start time for the time-frame (inclusive)
 * @property end The end time for the time-frame (exclusive)
 * @constructor Create new time-frame
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
         * Full time-frame, matches any time and is typically used when no filtering is required
         */
        val FULL = TimeFrame(Instant.MIN, Instant.MAX)

        // Time-frames for some significant events in history of trading
        fun blackMonday1987() = parse("1987-10-19T00:00:00Z", "1987-10-20T00:00:00Z")
        fun financialCrisis2008() = parse("2008-09-08T00:00:00Z", "2009-03-10T00:00:00Z")
        fun tenYearBullMarket2009() = parse("2009-03-10T00:00:00Z", "2019-03-10T00:00:00Z")
        fun flashCrash2010() = parse("2010-05-06T00:00:00Z", "2010-05-07T00:00:00Z")
        fun coronaCrash2020() = parse("2020-02-17T00:00:00Z", "2020-03-17T00:00:00Z")

        /**
         * Create a timeframe for the provided years
         *
         * @param first First year to include, from january 1 onwards
         * @param last Last year to include, till 31 December of that year
         * @return
         */
        fun fromYears(first: Int, last: Int): TimeFrame {
            val start = Instant.parse("${first}-01-01T00:00:00Z")
            var stop = Instant.parse("${last + 1}-01-01T00:00:00Z")
            if (stop <= start) stop = start
            return TimeFrame(start, stop)
        }


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
         * Create a timeframe starting with the current time and last for the specified number of minutes. This comes in
         * handy when you want to test a strategy against a live feed for a certain duration.
         *
         * See also [nextPeriod] for a more generic version of this method
         *
         * # Example
         *      roboquant.run(feed, TimeFrame.nextMinutes(30))
         *
         * @param minutes
         * @return
         */
        fun nextMinutes(minutes: Long): TimeFrame = nextPeriod(minutes, ChronoUnit.MINUTES)

    }

    /**
     * Does the timeframe contain a certain time.
     *
     * @param now
     * @return
     */
    fun contains(now: Instant): Boolean {
        return (now >= start) && (now < end)
    }

    /**
     * Is this timeframe in a single day given the provided time zoneId.
     *
     * @param zoneId
     * @return
     */
    fun isSingleDay(zoneId: String = "UTC"): Boolean {
        val z = ZoneId.of(zoneId)
        return start.atZone(z).toLocalDate() == end.minusMillis(1).atZone(z).toLocalDate()
    }


    /**
     * Extend a timeframe, both before and after the current timeframe. The current timeframe
     * remains unchanged and a new one is returned.
     *
     * ### Usage
     *      // Add 1 week before and after the black monday event
     *      val tf = TimeFrame.BlackMonday1987().extend(7)
     *
     *
     * @param pre The amount to subtract to the beginning
     * @param post The amount to add to the end, default is same as pre amount.
     * @param unit The [ChronoUnit], default is DAYS
     */
    fun extend(pre: Long, post: Long = pre, unit: ChronoUnit = ChronoUnit.DAYS) {
        val s = start.atZone(ZoneId.of("UTC"))
        val e = end.atZone(ZoneId.of("UTC"))
        TimeFrame(s.minus(pre, unit).toInstant(), e.plus(post, unit).toInstant())
    }

    /**
     * Calculate the intersection of this time-frame with another one and return
     * the new time-frame
     *
     * @param other
     * @return
     */
    fun intersect(other: TimeFrame): TimeFrame {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return TimeFrame(startTime, stopTime)
    }

    /**
     * Is there any overlap between timeframes
     *
     * @param other
     * @return
     */
    fun overlap(other: TimeFrame): Boolean {
        val startTime = if (start > other.start) start else other.start
        val stopTime = if (end < other.end) end else other.end
        return stopTime > startTime
    }

    /**
     * Calculate the union of this time-frame with another one and return
     * the new time-frame
     *
     * @param other
     * @return
     */
    fun union(other: TimeFrame): TimeFrame {
        val startTime = if (start > other.start) other.start else start
        val stopTime = if (end < other.end) other.end else end
        return TimeFrame(startTime, stopTime)
    }

    /**
     * Convert a timeframe to a timeline of individual days
     *
     * @param excludeWeekends
     * @param zoneId
     * @return
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
     * Convert a timeframe to a timeline with one minute intervals. Optionally a between a certain start and stop time
     * in the day cna be configured.
     *
     * @param excludeWeekends
     * @param zoneId
     * @return
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
     * Split a time-frame into two parts, one for training and one for test/validation
     *
     * @param testSize Value between 0 and 1 that determines the percentage to allocate for testing
     * @return
     */
    fun splitTrainTest(testSize: Double): Pair<TimeFrame, TimeFrame> {
        assert(testSize < 1.0)
        val diff = end.toEpochMilli() - start.toEpochMilli()
        val train = (diff * (1.0 - testSize)).toLong()
        val border = start.plus(train, ChronoUnit.MILLIS)
        return Pair(TimeFrame(start, border), TimeFrame(border, end))
    }


    /**
     * Split a time-frame in multiple parts. One common use case is to create a walk forward
     * back-test.
     *
     * @param period
     * @return
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

    override fun toString(): String {
        return "[$start - $end]"
    }

    /**
     * Depending on the duration of the time frame, format the time-frame either with seconds or days
     * resolution.
     *
     * TODO: print singleDay timeframes a bit nicer
     *
     * @return
     */
    fun toPrettyString(): String {
        val s1 = if (start == Instant.MIN) "MIN" else if (start == Instant.MAX) "MAX" else formatter.format(start)
        val s2 = if (end == Instant.MIN) "MIN" else if (end == Instant.MAX) "MAX" else formatter.format(end)
        return "$s1 - $s2"
    }


    private val formatter by lazy {
        val diff = end.epochSecond - start.epochSecond
        if (diff > 24 * 60 * 60) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)
        } else {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)
        }
    }
}

