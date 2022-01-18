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
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class ExchangeTest {


    @Test
    fun test() {
        assertEquals(Exchange.DEFAULT, Exchange.getInstance("DUMMY"))

        Exchange.addInstance("DUMMY", "Europe/Paris")
        val exchange2 = Exchange.getInstance("DUMMY")
        assertEquals("DUMMY", exchange2.exchangeCode)
        assertIs<ZoneId>(exchange2.zoneId)
        assertTrue(exchange2 in Exchange.exchanges)

        val now = Instant.now()
        assertTrue(exchange2.sameDay(now, now))

        Exchange.addInstance("DUMMY2", "Europe/Paris")
        val exchange3 = Exchange.getInstance("DUMMY2")
        assertNotEquals(exchange2, exchange3)

        val d = LocalDate.now()
        val ct = exchange2.getClosingTime(d)
        val ot = exchange2.getOpeningTime(d)
        assertTrue(ct > ot)
        assertTrue(exchange2.opening < exchange2.closing)
    }

    @Test
    fun hours() {
        val exchange = Exchange.getInstance("US")
        val date = LocalDate.of(2020,1,5)
        val tradingHours = exchange.getTradingHours(date)
        assertEquals(Duration.ofSeconds(13*30*60), tradingHours.duration)
    }

}