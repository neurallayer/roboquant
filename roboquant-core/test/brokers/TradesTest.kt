package org.roboquant.brokers

import org.roboquant.TestData
import org.roboquant.common.Currency
import java.time.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class TradesTest {

    @Test
    fun test() {
        val trades = Trades()
        assertTrue(trades.isEmpty())

        val asset = TestData.usStock()
        var now = Instant.now()
        for (i in 1..10) {
            now = now.plusSeconds(60)
            val trade1 = Trade(now, asset, 10.0, 100.0, 1002.0, 10.0, 0.0, "id_$i")
            trades.add(trade1)

            now = now.plusSeconds(60)
            val trade2 = Trade(now, asset, -10.0, 100.0, 1002.0, 10.0, 10.0, "id_$i")
            trades.add(trade2)
        }

        assertEquals(200.0, trades.totalFee(asset))
        assertEquals(100.0, trades.realizedPnL(asset))
        assertEquals(100.0, trades.realizedPnL(asset.symbol))
        assertEquals(20, trades.timeline.size)

        assertTrue(trades.summary().content == "Trades")

        val s = trades.summary()
        assertTrue(s.toString().isNotEmpty())

        val trades2 = trades[0..5]
        assertEquals(50.0, trades2.totalFee().getAmount(Currency.USD))

        assertEquals(5, trades2.timeline.size)

    }

}