package org.roboquant.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.ZoneId
import kotlin.test.assertEquals

class ZonedPeriodTest {

    @Test
    fun conversions() {
        assertEquals(1, 1.minutes.toMinutes())
        assertEquals(2, 2.hours.toHours())
        assertEquals(3, 3.days.toDays())
        assertEquals(4 * 7, 4.weeks.toDays())
    }

    @Test
    fun atZone() {
        val a = ZonedPeriod(10.hours, ZoneId.of("UTC"))
        val b = a.atZone(ZoneId.of("UTC"))
        assertDoesNotThrow {  b.toString()}
    }

}