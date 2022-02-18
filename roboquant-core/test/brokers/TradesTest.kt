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

package org.roboquant.brokers


internal class TradesTest {

/*
    @Test
    fun test() {
        val trades = Trades()
        assertTrue(trades.isEmpty())

        val asset = TestData.usStock()
        var now = Instant.now()
        for (i in 1..10) {
            now = now.plusSeconds(60)
            val trade1 = Trade(now, asset, 10.0, 100.0, 10.0, 0.0, "id_$i")
            trades.add(trade1)

            now = now.plusSeconds(60)
            val trade2 = Trade(now, asset, -10.0, 100.0, 10.0, 10.0, "id_$i")
            trades.add(trade2)
        }

        assertEquals(200.0, trades.totalFee(asset).value)
        assertEquals(100.0, trades.realizedPnL(asset).value)
        assertEquals(100.0, trades.realizedPnL(asset.symbol).value)
        assertEquals(20, trades.timeline.size)

        assertTrue(trades.summary().content == "Trades")

        val s = trades.summary()
        assertTrue(s.toString().isNotEmpty())

        val trades2 = trades[0..5]
        assertEquals(50.USD, trades2.totalFee().getAmount(Currency.USD))

        assertEquals(5, trades2.timeline.size)

    }
*/

}