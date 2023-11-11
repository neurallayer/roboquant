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
import org.roboquant.common.bips
import org.roboquant.common.findBySymbols
import org.roboquant.common.getBySymbol
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.ml.*
import smile.data.formula.Formula
import smile.regression.gbm

fun main() {


    val feed = AvroFeed.sp500()

    val features = DataFrameFeatureSet(offset = 3)
    val asset = feed.assets.getBySymbol("TSLA")
    val y = PriceFeature(asset, "CLOSE", "Y").returns()
    features.addLabel(y)

    val xAssets = feed.assets.findBySymbols("TSLA", "MSFT", "F", "GOOGL")
    xAssets.forEach {
        val x1 = PriceFeature(it, "CLOSE").returns()
        val x2 = TaLibFeature.rsi(it, 10)
        val x3 = TaLibFeature.obv(it).returns()
        val x4 = TaLibFeature.ema(it)
        features.addInput(x1, x2, x3, x4)
    }

    println(features.names)

    val percentage = 1.bips
    val myStrat = RegressionStrategy(features, asset, percentage, 500) {
        val model = gbm(Formula.lhs("Y-RETURNS"), it, ntrees = 100)
        println(model.importance().toList())
        model
    }

    val broker = SimBroker()
    val rq = Roboquant(myStrat, ProgressMetric(), broker= broker,  logger = MemoryLogger(false))
    rq.run(feed)
    println(rq.broker.account.summary())


}
