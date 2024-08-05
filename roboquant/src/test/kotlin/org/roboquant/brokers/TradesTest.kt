/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.brokers

import org.roboquant.TestData
import org.roboquant.brokers.sim.*
import org.roboquant.common.*
import org.roboquant.common.Currency.Companion.USD
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TradesTest {

    private fun getTrades(): List<Trade> {
        val trades = mutableListOf<Trade>()
        val asset = TestData.usStock()
        var now = Instant.now()
        val size = Size(10)
        for (i in 1..10) {
            now = now.plusSeconds(60)
            val trade1 = Trade(now, asset, size, 100.0, 10.0, 0.0, i.toString())
            trades.add(trade1)

            now = now.plusSeconds(60)
            val trade2 = Trade(now, asset, -size, 100.0, 0.0, 100.0, i.toString())
            trades.add(trade2)
        }
        return trades
    }

    @Test
    fun basic() {
        val trade = Trade(Instant.now(), USStock("TEST"), Size(10), 100.0, 5.0, 100.0, 1.toString())
        assertEquals(1005.0, trade.totalCost.value)
        assertEquals(100.0 / 1005.0, trade.pnlPercentage)

        val trade2 = Trade(Instant.now(), USStock("TEST"), -Size(10), 100.0, 5.0, 100.0, 1.toString())
        assertEquals(-995.0, trade2.totalCost.value)
        assertEquals(100.0 / 995.0, trade2.pnlPercentage)
    }

    @Test
    fun testAggr() {
        val trades = getTrades()

        assertEquals(100.0, trades.fee.getAmount(USD).value)
        assertEquals(20, trades.timeline.size)


        val trades2 = trades[0..4]
        assertEquals(30.USD, trades2.fee.getAmount(USD))
        assertEquals(5, trades2.timeline.size)

        val metrics = trades.toPNLPercentageMetrics()
        assertTrue(metrics.isNotEmpty())
        assertTrue(metrics.first().value.iszero)
        assertEquals(0.1, metrics.last().value)

        assertTrue(trades.first().pnlPercentage.iszero)
        assertEquals(0.1, trades.last().pnlPercentage)
    }


}
