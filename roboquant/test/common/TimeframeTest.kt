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


import org.junit.jupiter.api.Test
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TimeframeTest {



    @Test
    fun split() {
        val tf = Timeframe.fromYears(1980, 1999)
        val subFrames = tf.split(2.years)
        assertEquals(10, subFrames.size)
        assertEquals(tf.start, subFrames.first().start)
        assertEquals(tf.end, subFrames.last().end)
    }


    @Test
    fun constants() {
        val tf2 = Timeframe.INFINITE
        assertEquals(Timeframe.INFINITE, tf2)
        assertTrue(tf2.isInfinite())

        assertTrue(Timeframe.blackMonday1987.isSingleDay(ZoneId.of("America/New_York")))
        assertFalse(Timeframe.coronaCrash2020.isSingleDay())
        assertTrue(Timeframe.flashCrash2010.isSingleDay(ZoneId.of("America/New_York")))
        assertFalse(Timeframe.financialCrisis2008.isSingleDay())
        assertFalse(Timeframe.tenYearBullMarket2009.isSingleDay())
    }

    @Test
    fun print() {
        val tf2 = Timeframe.INFINITE

        val s2 = tf2.toString()
        assertTrue(s2.isNotBlank())

        val s3 = tf2.toRawString()
        assertTrue(s3.isNotBlank())
    }


    @Test
    fun creation() {
        val tf = Timeframe.next(1.minutes)
        assertEquals(60, tf.end.epochSecond - tf.start.epochSecond)

        val tf2 = tf.extend(1.days)
        assertTrue { tf2.contains(tf.start) }
        assertTrue { tf2.contains(tf.end) }
    }

    @Test
    fun union() {
        val tf1 = Timeframe.parse("2020-01-01", "2021-02-01")
        val tf2 = Timeframe.parse("2021-01-01", "2022-01-01")

        assertEquals(Timeframe(tf1.start, tf2.end), tf1.union(tf2))
    }


    @Test
    fun annualize() {
        val tf = Timeframe.fromYears(2019, 2019)
        val x = tf.annualize(0.1)
        assertTrue(x - 0.1 < 0.01)

        val tf2 = Timeframe.fromYears(2019, 2020)
        val y = tf2.annualize(0.1)
        assertTrue(0.05 - y < 0.1)
    }

    @Test
    fun plusMinus() {
        val tf = Timeframe.fromYears(2019, 2019)
        val tf2 = tf + 2.years - 2.years
        assertEquals(tf, tf2)
    }

    @Test
    fun toTimeline() {
        val tf = Timeframe.parse("2020-01-01T18:00:00Z","2021-12-31T18:00:00Z")
        val timeline = tf.toTimeline(1.days)
        assertTrue(timeline.size > 200)
    }
}