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
import org.roboquant.common.bips
import org.roboquant.common.getBySymbol
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.ml.*
import smile.data.formula.Formula
import smile.regression.gbm


fun main() {
    val feed = AvroFeed.sp500()

    val features = FeatureSet()
    val asset = feed.assets.getBySymbol("TSLA")
    val y = PriceFeature("Y", asset, "CLOSE").returns()
    val x1 = TaLibSingleFeature("X1", asset) { ema(it, 20) }.returns()
    val x2 = TaLibSingleFeature("X2", asset) { sma(it, 10) }.growthRate()
    val x3 = TaLibSingleFeature("X3", asset) { rsi(it, 20) }
    val x4 = TaLibSingleFeature("X4", asset) { if (cdl3StarsInSouth(it)) 1.0 else -1.0 }
    val x5 = TaLibMultiFeature("X5", "X6", "X7", asset = asset) { bbands(it).toList() }.returns()
    val x6 = TaLibSingleFeature.obv(asset).returns()

    features.add(y, x1, x2, x3, x4, x5, x6)

    val percentage = 1.bips
    val myStrat = RegressionStrategy(features, asset, percentage, 500, 50) {
        val model = gbm(Formula.lhs("Y"), it)
        println(model.importance().toList())
        model
    }

    val rq = Roboquant(myStrat, ProgressMetric(), logger = MemoryLogger())
    rq.run(feed)
    println(rq.broker.account.summary())


}
