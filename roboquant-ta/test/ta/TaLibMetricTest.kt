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
import org.roboquant.metrics.MetricResults
import java.time.Instant
import kotlin.test.assertTrue

class TaLibMetricTest {

    @Test
    fun test() {
        val metric = TaLibMetric("ema10", 10) { series ->
            ema(series.close, 10)
        }
        assertTrue(metric.getMetrics().isEmpty())

        val account = Account(
            Currency.USD,
            Instant.now(),
            Wallet(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyMap(),
            Amount(Currency.USD, 0.0)
        )

        val results = metric.calc(account, Event(emptyList(), Instant.now()))
        assertTrue(results.isEmpty())

        val feed = HistoricTestFeed(100 until 111, priceBar = true)
        val events = feed.filter<PriceBar>()
        var mResult: MetricResults = emptyMap()
        for (event in events) {
            mResult = metric.calc(account, Event(listOf(event.second), event.first))
        }
        assertTrue(mResult.isNotEmpty())

    }
}