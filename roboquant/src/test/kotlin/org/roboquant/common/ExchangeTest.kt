/*
 * Copyright 2020-2022 Neural Layer
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

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.*

internal class ExchangeTest {

    @Test
    fun test() {
        assertEquals(Exchange.DEFAULT.zoneId, Exchange.getInstance("DUMMY").zoneId)

        Exchange.addInstance("DUMMY", "Europe/Paris")
        val exchange2 = Exchange.getInstance("DUMMY")
        assertEquals("DUMMY", exchange2.exchangeCode)
        assertEquals(ZoneId.of("Europe/Paris"), exchange2.zoneId)
        assertTrue(exchange2 in Exchange.exchanges)

        val now = Instant.now()
        assertTrue(exchange2.sameDay(now, now))

        Exchange.addInstance("DUMMY2", "Europe/Paris")
        val exchange3 = Exchange.getInstance("DUMMY2")
        assertNotEquals(exchange2, exchange3)
        assertEquals("DUMMY2", exchange3.toString())

        val d = LocalDate.parse("2021-02-02")
        val ct = exchange2.getClosingTime(d)
        val ot = exchange2.getOpeningTime(d)
        assertTrue(ct > ot)

        assertDoesNotThrow {
            exchange3.getLocalDate(now)
        }
    }

    @Test
    fun serializer() {
        assertEquals("\"\"", Json.encodeToString(Exchange.DEFAULT))
        assertEquals(Exchange.DEFAULT, Json.decodeFromString("\"\""))

        val us = Exchange.getInstance("US")
        assertEquals("\"US\"", Json.encodeToString(us))
        assertEquals(us, Json.decodeFromString("\"US\""))
    }


    @Test
    fun testNoTrading() {
        val exchange = Exchange.getInstance("US")

        assertDoesNotThrow {
            val localDate = LocalDate.parse("2020-01-03")
            assertEquals(DayOfWeek.FRIDAY, localDate.dayOfWeek)
            exchange.getClosingTime(localDate)
        }

        assertFailsWith<NoTradingException> {
            val localDate = LocalDate.parse("2020-01-04")
            assertEquals(DayOfWeek.SATURDAY, localDate.dayOfWeek)
            exchange.getClosingTime(localDate)
        }

    }

    @Test
    fun testIsTrading() {
        val exchange = Exchange.getInstance("US")
        assertTrue(exchange.isTrading(Instant.parse("2022-01-03T20:00:00Z")))
        assertFalse(exchange.isTrading(Instant.parse("2022-01-03T08:00:00Z")))
    }

}