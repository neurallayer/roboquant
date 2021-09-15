package org.roboquant.orders

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.*



internal class TimeInForceTest {


    @Test
    fun gtc() {
        val tif = GTC()
        assertEquals("GTC", tif.toString())
        val t1 = Instant.now()
        val t2 = t1.plusSeconds(1000)
        assertFalse(tif.isExpired(t1, t2, 10.0, 20.0))

        val t3 = t1.plusSeconds(3600L*24*365)
        assertTrue(tif.isExpired(t1, t3, 10.0, 20.0))
    }

    @Test
    fun gtd() {
        // 5 days in future
        val date =Instant.now().plusSeconds(3600L*24*5)
        val tif = GTD(date)
        assertEquals("GTD", tif.toString().slice(0..2))

        val t1 = Instant.now()
        val t2 = t1.plusSeconds(3600)

        assertFalse(tif.isExpired(t1, t2, 1.0, 10.0))

        val t3 = t1.plusSeconds((3600L*24*6))
        assertTrue(tif.isExpired(t1, t3,10.0, 10.0))
    }

    @Test
    fun day() {
        val tif = DAY(ZoneId.of("UTC"))
        assertEquals("DAY", tif.toString())
        val date = Instant.now()
        assertFalse(tif.isExpired(date, date,10.0, 20.0))
        assertTrue(tif.isExpired(date, date.plus(1, ChronoUnit.DAYS),10.0, 20.0))
    }

    @Test
    fun ioc() {
        val tif = IOC()
        assertEquals("IOC", tif.toString())
        val date = Instant.now()
        assertFalse(tif.isExpired(date, date, 10.0, 10.0))
    }

    @Test
    fun fok() {
        val tif = FOK()
        assertEquals("FOK", tif.toString())
        val date = Instant.now()
        assertFalse(tif.isExpired(date, date, 10.0, 10.0))
        assertTrue(tif.isExpired(date, date, 10.0, 20.0))
    }

}