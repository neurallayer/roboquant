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



import org.junit.Test
import java.time.Instant
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TimeFrameTest {

    @Test
    fun toDays() {
        val tf = TimeFrame.fromYears(1900, 2000)
        val timeline1 = tf.toDays()
        val timeline2 = tf.toDays(excludeWeekends = true)
        assertTrue( timeline1.size > timeline2.size)
    }

    @Test
    fun beforeAfter() {
        val tf = TimeFrame.fromYears(1900, 2000)
        val timeline = tf.toDays()
        val first = timeline.first()
        val last = timeline.last()
        assertEquals(timeline.lastIndex, timeline.latestNotAfter(last))
        assertEquals(0, timeline.earliestNotBefore(first))
    }

    @Test
    fun split() {
        val i1 = Instant.parse("1980-01-01T09:00:00Z")
        val i2 =  Instant.parse("2000-01-01T09:00:00Z")
        val tf = TimeFrame(i1, i2)
        val subFrames = tf.split(Period.ofYears(2))
        assertEquals(10, subFrames.size)
    }


    @Test
    fun constants() {
        val tf2 = TimeFrame.FULL
        assertEquals(TimeFrame.FULL, tf2)

        assertTrue(TimeFrame.blackMonday1987().isSingleDay())
        assertFalse(TimeFrame.coronaCrash2020().isSingleDay())
        assertTrue(TimeFrame.flashCrash2010().isSingleDay())
        assertFalse(TimeFrame.financialCrisis2008().isSingleDay())
        assertFalse(TimeFrame.tenYearBullMarket2009().isSingleDay())

    }

    @Test
    fun print() {
        val tf2 = TimeFrame.FULL

        val s2 = tf2.toPrettyString()
        assertTrue(s2.isNotBlank())
    }


    @Test
    fun creation() {
        val tf = TimeFrame.nextPeriod(1, ChronoUnit.MINUTES)
        assertEquals(60,tf.end.epochSecond - tf.start.epochSecond)
    }
}