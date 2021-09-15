package org.roboquant.feeds.binance

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.TimeFrame
import org.roboquant.logging.InfoLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.policies.NeverShortPolicy
import org.roboquant.strategies.TAStrategy
import org.junit.Test

internal class BinanceFeedTestIT {

    @Test
    fun testBinanceFeed() {
        System.getProperty("binance") ?: return

        val broker = SimBroker.withDeposit(10_000.00, "BUSD")
        val strategy = TAStrategy(15)

        strategy.buy {
            (ta.cdlMorningStar(it) || ta.cdlMorningDojiStar(it)) && ta.ema(it.close, 5) > ta.ema(it.close, 9)
        }

        strategy.sell {
            ta.cdl2Crows(it) && ta.ema(it.close, 3) < ta.ema(it.close, 5)
        }

        val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), broker = broker)
        val feed = BinanceFeed()
        feed.subscribePriceBar(Pair("BTCBUSD", "BUSD"))
        assert(feed.assets.isNotEmpty())

        val timeFrame = TimeFrame.nextMinutes(2)
        roboquant.run(feed, timeFrame)
        broker.account.summary().log()
    }


    @Test
    fun testBinanceFeed2() {
        System.getProperty("binance") ?: return

        val broker = SimBroker.withDeposit(1_000_000.00, "BUSD")
        val strategy = TAStrategy(6)

        strategy.buy {
            println("\nINSIDE BUY")
            ta.sma(it.close, 3) > ta.sma(it.close, 5)
        }

        strategy.sell {
            println("INSIDE SELL")
            false
        }

        val p = NeverShortPolicy(10_000.0, 200_000.00)
        p.recording = true

        val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), policy = p, logger = InfoLogger(), broker = broker)
        val feed = BinanceFeed()
        feed.subscribePriceBar(Pair("BTCBUSD", "BUSD"))


        val timeFrame = TimeFrame.nextMinutes(30)
        roboquant.run(feed, timeFrame)
        broker.account.summary().log()
        feed.disconnect()
    }

}

