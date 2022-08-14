package org.roboquant.ta

import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import org.ta4j.core.rules.StopGainRule
import org.ta4j.core.rules.StopLossRule
import kotlin.test.Test
import kotlin.test.assertTrue

internal class Ta4JStrategyTest {

    @Test
    fun test() {
        val strategy = Ta4jStrategy(maxBarCount = 30)

        strategy.buy  { series ->
            val closePrice = ClosePriceIndicator(series)
            val shortSma = SMAIndicator(closePrice, 20)
            val longSma = SMAIndicator(closePrice, 30)
            CrossedUpIndicatorRule(shortSma, longSma)
        }

        strategy.sell { series ->
            val closePrice = ClosePriceIndicator(series)
            val shortSma = SMAIndicator(closePrice, 20)
            val longSma = SMAIndicator(closePrice, 30)
            CrossedDownIndicatorRule(shortSma, longSma)
                .or(StopLossRule(closePrice, 3.0))
                .or(StopGainRule(closePrice, 2.0))
        }

        val roboquant = Roboquant(strategy)
        val feed = RandomWalk.lastYears(1, nAssets = 2)
        roboquant.run(feed)
        val account = roboquant.broker.account
        assertTrue(account.closedOrders.isNotEmpty())
    }

    @Test
    fun noRules() {
        // Default rule is false, meaning no signals
        val strategy = Ta4jStrategy(maxBarCount = 30)
        val roboquant = Roboquant(strategy)
        val feed = RandomWalk.lastYears(1, nAssets = 2)
        roboquant.run(feed)
        val account = roboquant.broker.account
        assertTrue(account.closedOrders.isEmpty())
    }

}