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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.sim.SpreadPricingEngine
import org.roboquant.common.*
import org.roboquant.ml.*
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.Strategy
import smile.data.formula.Formula
import smile.regression.gbm
import kotlin.test.Ignore
import kotlin.test.Test

internal class MLSamples {

    private fun getStrategy(asset: Asset): Strategy {
        val features = DataFrameFeatureSet(warmup = 60) // 60 steps warmup
        val y = PriceFeature(asset, "CLOSE", "Y").returns(15)
        features.add(y, offset = 15) // predict 15 steps in the future
        assert(features.names.contains("Y-RETURNS"))

        // Example of custom indicator
        val myFeature = TaLibFeature("custom", asset) {
            ema((it.close - it.open) * it.volume, 15)
        }.returns()

        features.add(
            VolumeFeature(asset).returns(),
            PriceFeature(asset, "CLOSE").returns(),
            PriceFeature(asset, "OPEN") / PriceFeature(asset, "CLOSE"),
            TaLibFeature.rsi(asset, 5),
            TaLibFeature.rsi(asset, 10),
            TaLibFeature.rsi(asset, 20),
            TaLibFeature.ema(asset, 11, 26),
            TaLibFeature.obv(asset).returns(),
            myFeature,
            HistoricPriceFeature(asset, 1, "CLOSE").returns(),
            HistoricPriceFeature(asset, 5, "CLOSE").returns(),
            HistoricPriceFeature(asset, 10, "CLOSE").returns(),
            HistoricPriceFeature(asset, 15, "CLOSE").returns(),
            HistoricPriceFeature(asset, 30, "CLOSE").returns(),
            // TestFeature(doubleArrayOf(1.0, 2.0, 3.0))
        )

        val s = RegressionStrategy(features, asset, 2.bips) {
            // Gradient Tree Boosting model
            val model = gbm(Formula.lhs("Y-RETURNS"), it, ntrees = 500) //, shrinkage = 1.0)
            println(model.importance().toList())
            model
        }
        return s
    }

    @Test
    @Ignore
    internal fun features() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Warn")
        // Currency.increaseDigits(4)
        val feed = AvroFeed.forex() // 1 minute bars for 1 year
        val asset = feed.assets.first() // EUR_USD

        val strategy = getStrategy(asset)
        val broker = SimBroker(pricingEngine = SpreadPricingEngine(1.5.bips))
        val policy = FlexPolicy {
            shorting = true
            orderPercentage = 50.percent
        }
        val rq = Roboquant(strategy, policy = policy, broker = broker)
        val (train, valid) = feed.timeframe.splitTwoWay(9.months)
        rq.run(feed, timeframe = train)
        rq.broker.reset()
        val account = rq.run(feed, timeframe = valid)
        println(account.summary())
        val trades = account.trades
        println(trades.filter { !it.pnlValue.iszero }.joinToString("\n") {
            val p = 100.0 * it.pnlPercentage
            "${it.asset.symbol} $p"
        })

    }
}
