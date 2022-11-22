package org.roboquant.common

import org.junit.jupiter.api.assertDoesNotThrow
import java.time.Instant
import java.time.Period
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TradingPeriodTest {

    @Test
    fun test() {
        val y = Period.ofDays(2)
        val x = TradingPeriod(y)
        assertEquals(y, x.period)
    }


    @Test
    fun numbers() {
        val x = ZonedDateTime.now()
        val y = x + 1.days + 2.months + 1.years + 2.weeks + 100.millis + 10.seconds + 30.minutes + 1.hours
        assertTrue(y > x)
    }

    @Test
    fun instant() {
        val t = Instant.now()
        assertDoesNotThrow {
            t + 2.years - 100.millis + 1.hours
        }
    }

    @Test
    fun zonedDateTime() {
        val t = ZonedDateTime.now()
        assertDoesNotThrow {
            t + 2.years - 100.millis + 1.hours
        }
    }

    @Test
    fun same() {
        val z = ZonedDateTime.now(Config.defaultZoneId)
        val i = z.toInstant()

        val z2 = z + 1.years - 100.millis
        val i2 = i + 1.years - 100.millis

        assertEquals(i2, z2.toInstant())
    }



}