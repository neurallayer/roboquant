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

internal class TA4JStrategyTest {

    @Test
    fun test() {
        val strategy = TA4JStrategy(maxBarCount = 50)

        strategy.buyingRule = { series ->
            val closePrice = ClosePriceIndicator(series)
            val shortSma = SMAIndicator(closePrice, 5)
            val longSma = SMAIndicator(closePrice, 30)
            CrossedUpIndicatorRule(shortSma, longSma)
        }

        strategy.sellingRule = { series ->
            val closePrice = ClosePriceIndicator(series)
            val shortSma = SMAIndicator(closePrice, 5)
            val longSma = SMAIndicator(closePrice, 30)
            CrossedDownIndicatorRule(shortSma, longSma)
                .or(StopLossRule(closePrice, 3.0))
                .or(StopGainRule(closePrice, 2.0))
        }

        val roboquant = Roboquant(strategy)

        val feed = RandomWalk.lastYears()
        roboquant.run(feed)
        val account = roboquant.broker.account
        assertTrue(account.closedOrders.isNotEmpty())
    }

}