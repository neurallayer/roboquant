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

package org.roboquant.orders

import org.roboquant.TestData
import org.roboquant.common.Asset
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class TimeInForceTest {

    class TestOrder(asset: Asset = TestData.usStock()) : Order(asset) {

        fun updatePlaced(now: Instant) {
            placed = now
        }

    }


    @Test
    fun gtc() {
        val tif = GTC()
        assertEquals("GTC", tif.toString())
        val order = TestData.usMarketOrder()
        val t2 = order.placed.plusSeconds(1000)
        assertFalse(tif.isExpired(order, t2, 10.0))

        val t3 = order.placed.plusSeconds(3600L * 24 * 365)
        assertTrue(tif.isExpired(order, t3, 10.0))
    }

    @Test
    fun gtd() {
        // 5 days in future
        val date = Instant.now().plusSeconds(3600L * 24 * 5)
        val tif = GTD(date)
        assertEquals("GTD", tif.toString().slice(0..2))

        val t1 = Instant.now()
        val t2 = t1.plusSeconds(3600)

        val order = TestData.usMarketOrder()

        assertFalse(tif.isExpired(order, t2, 1.0))

        val t3 = t1.plusSeconds((3600L * 24 * 6))
        assertTrue(tif.isExpired(order, t3, 10.0))
    }

    @Test
    fun day() {
        val tif = DAY()
        val order = TestOrder()
        assertEquals("DAY", tif.toString())
        val date = Instant.now()
        order.updatePlaced(date)

        assertFalse(tif.isExpired(order, date, 10.0))
        assertTrue(tif.isExpired(order, date.plus(1, ChronoUnit.DAYS), 10.0))
    }

    @Test
    fun ioc() {
        val tif = IOC()
        val order = TestOrder()
        assertEquals("IOC", tif.toString())
        val date = Instant.now()
        order.updatePlaced(date)
        assertFalse(tif.isExpired(order, date, 10.0))
    }

    @Test
    fun fok() {
        val tif = FOK()
        val order = TestOrder()
        assertEquals("FOK", tif.toString())
        val date = Instant.now()
        order.updatePlaced(date)
        assertFalse(tif.isExpired(order, date, 0.0))
        assertTrue(tif.isExpired(order, date, 10.0))
    }

}