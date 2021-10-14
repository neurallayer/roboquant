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
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.*

internal class ExchangeTest {


    @Test
    fun test() {
        assertEquals(Exchange.DEFAULT, Exchange.getInstance("DUMMY"))

        val exchange1 = Exchange.addInstance("DUMMY", "CET/CEST")
        val exchange2 = Exchange.getInstance("DUMMY")
        assertEquals(exchange1, exchange2)
        assertEquals("DUMMY", exchange2.exchangeCode)
        assertIs<ZoneId>(exchange2.zoneId)

        assertTrue(exchange1 in Exchange.exchanges)

        val now = Instant.now()
        assertTrue( exchange1.sameDay(now, now))

        val exchange3 = Exchange.addInstance("DUMMY2", "CET/CEST")
        assertNotEquals(exchange2, exchange3)

        assertIs<Exchange>(Exchange.FSX)
        assertIs<Exchange>(Exchange.JPX)
        assertIs<Exchange>(Exchange.LSE)
        assertIs<Exchange>(Exchange.NASDAQ)
        assertIs<Exchange>(Exchange.NYSE)
        assertIs<Exchange>(Exchange.SIX)
        assertIs<Exchange>(Exchange.SSE)
        assertIs<Exchange>(Exchange.TSX)
        assertIs<Exchange>(Exchange.SSX)

        val d = LocalDate.now()
        val ct = exchange1.getClosingTime(d)
        val ot = exchange1.getOpeningTime(d)
        assertTrue(ct > ot)

        assertTrue(exchange1.opening < exchange1.closing)

    }

}