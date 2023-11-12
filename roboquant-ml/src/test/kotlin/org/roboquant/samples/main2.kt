/*
 * Copyright 2020-2023 Neural Layer
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
import org.roboquant.common.*
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.ml.*
import org.roboquant.strategies.CombinedStrategy
import org.roboquant.strategies.Strategy
import smile.data.formula.Formula
import smile.regression.gbm


fun getStrategy(x: Asset, assets: Collection<Asset>): Strategy {
    val features = DataFrameFeatureSet(warmup = 20)
    val y = PriceFeature(x, "CLOSE", "Y").returns()
    features.add(y, offset = 3)

    assets.forEach {
        val x1 = PriceFeature(it, "CLOSE").returns()
        val x2 = TaLibFeature.rsi(it, 10)
        val x3 = TaLibFeature.obv(it).returns()
        val x4 = TaLibFeature.ema(it)
        val x5 = VolumeFeature(it).returns()
        val x6 = HistoricPriceFeature(it, 5).returns()
        features.add(x1, x2, x3, x4, x5, x6)
    }

    assert(features.names.contains("Y-RETURNS"))

    /*
    val s = RegressionStrategy(features, x, 1.percent) {
        val model = ridge(Formula.lhs("Y-RETURNS"), it, 0.005)
        println(model)
        model
    }
     */

    val s = RegressionStrategy(features, x, 2.percent) {
        val model = gbm(Formula.lhs("Y-RETURNS"), it, ntrees = 1_000, shrinkage = 1.0)
        // println(model.importance().toList())
        model
    }
    s.recording = true
    return s
}

fun main() {
    System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Warn")
    val feed = AvroFeed.sp500()

    val xAssets = feed.assets.findBySymbols("MSFT", "F", "GOOGL")
    val s1 = getStrategy(feed.assets.getBySymbol("TSLA"), xAssets)
    val s2 = getStrategy(feed.assets.getBySymbol("JPM"), xAssets)
    val myStrat = CombinedStrategy(s1, s2)

    val broker = SimBroker(retention = 100.years)
    val rq = Roboquant(myStrat, ProgressMetric(), broker = broker, logger = MemoryLogger())
    val (train, valid) = feed.timeframe.splitTwoWay(3.years)
    rq.run(feed, train)
    rq.broker.reset()
    rq.run(feed, valid, reset = false)
    println(rq.broker.account.summary())
    val trades = rq.broker.account.trades
    println(trades.filter { !it.pnlValue.iszero }.joinToString("\n") {
        val p = 100.0 * it.pnlPercentage
        "${it.asset.symbol} $p"
    })

}
