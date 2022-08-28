/*
 * Copyright 2020-2022 Neural Layer
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

        strategy.buy { series ->
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