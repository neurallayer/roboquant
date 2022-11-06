package org.roboquant.common

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals


internal class TimeParserTest {


    private fun parse(time: String) : Instant {
        val tp = AutoDetectTimeParser()
        val exchange = Exchange.getInstance("US")
        return tp.parse(time, exchange)
    }

    @Test
    fun basic() {
        val result = Instant.parse("2022-01-04T21:00:00Z")
        assertEquals(result, parse("20220104"))
        assertEquals(result, parse("2022-01-04"))
        assertEquals(result, parse("2022-01-04T21:00:00Z"))
        assertEquals(result, parse("2022-01-04 16:00:00"))
        assertEquals(result, parse("20220104 16:00:00"))
        assertEquals(result, parse("20220104  16:00:00"))
        assertEquals(result, parse(result.toEpochMilli().toString()))
    }



}