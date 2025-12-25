/*
 * Copyright 2020-2026 Neural Layer
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

import org.junit.jupiter.api.assertDoesNotThrow
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TimeSpanTest {

    @Test
    fun numbers() {
        val x = Instant.now()
        val y = x + 1.days + 2.months + 1.years + 100.millis + 10.seconds + 30.minutes + 1.hours
        assertTrue(y > x)

        val diff = 1.years + 10.seconds
        assertEquals(x, x + diff - diff)
    }

    @Test
    fun instant() {
        val t = Instant.now()
        assertDoesNotThrow {
            t + 2.years - 100.millis + 1.hours
        }
    }

    @Test
    fun zero() {
        val ts = TimeSpan.ZERO
        assertTrue(ts.isZero)

        val ts2 = ts + TimeSpan.ZERO
        assertEquals(ts, ts2)
    }

    @Test
    fun calc() {
        val t = 2.years - 1.hours + 1.hours
        assertEquals(2.years, t)
        assertEquals("P2YT0S", t.toString())
    }

    @Test
    fun toStringTest() {
        val t = 2.years + 1.months + 1.days - 1.hours + 12.minutes - 30.seconds + 100.millis
        assertEquals("P2Y1M1DT-48M-29.9S", t.toString())

        val t2 = 30.seconds
        assertEquals("P0DT30S", t2.toString())

        val t3 = 1.days
        assertEquals("P1DT0S", t3.toString())
    }

    @Test
    fun parsing() {
        val t = 2.years + 1.months + 1.days - 1.hours + 12.minutes - 30.seconds + 100.millis
        val t2 = TimeSpan.parse(t.toString())
        assertEquals(t, t2)
    }

    @Test
    fun zonedDateTime() {
        val t = Instant.now()
        assertDoesNotThrow {
            t + 2.years - 100.millis + 1.hours
        }
    }

    @Test
    fun equal() {
        val utc = ZoneId.of("UTC")
        val z = ZonedDateTime.now(utc)
        val i = z.toInstant()
        assertEquals(z, i.atZone(utc))

        val z2 = z + 1.years - 100.millis
        val i2 = i + 1.years - 100.millis

        assertEquals(i2, z2.toInstant())
    }


}
