package org.roboquant.ta

import org.junit.jupiter.api.Test
import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import org.roboquant.common.Wallet
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.test.HistoricTestFeed
import java.time.Instant
import kotlin.test.assertTrue

class TAMetricTest {

    @Test
    fun test() {
        val metric = TAMetric("ema50",50) { series ->
            ema(series.close, 50)
        }
        assertTrue(metric.getMetrics().isEmpty())

        val account = Account(
            Currency.USD, Instant.now(), Wallet(), emptyList(), emptyList(), emptyList(), emptyMap(), Amount(Currency.USD, 0.0)
        )

        val results = metric.calc(account, Event(emptyList(), Instant.now()))
        assertTrue(results.isEmpty())

        val feed = HistoricTestFeed(100 until 110, priceBar = true)
        val events = feed.filter<PriceBar>()
        for (event in events) {
            val mResults = metric.calc(account, Event(listOf(event.second), event.first))
            assertTrue(mResults.isEmpty())
        }

    }
}