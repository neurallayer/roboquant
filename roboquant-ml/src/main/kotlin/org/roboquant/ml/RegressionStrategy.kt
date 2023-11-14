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

package org.roboquant.ml

import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.addNotNull
import org.roboquant.common.percent
import org.roboquant.feeds.Event
import org.roboquant.strategies.Rating
import org.roboquant.strategies.RecordingStrategy
import org.roboquant.strategies.Signal
import smile.data.DataFrame
import smile.regression.DataFrameRegression

/**
 * Strategy based on a Smile Regression
 */
open class RegressionStrategy(
    private val featureSet: DataFrameFeatureSet,
    private val asset: Asset,
    private val percentage: Double = 1.percent,
    val block: (DataFrame) -> DataFrameRegression
) : RecordingStrategy() {

    private val logger = Logging.getLogger(this::class)
    private var trained = false
    private val metricName = "prediction.${asset.symbol.lowercase()}"

    private lateinit var model: DataFrameRegression

    private fun train() {
        val df = featureSet.getTrainingData()
        model = block(df)
    }

    private fun predict(): Double {
        val df = featureSet.getPredictData()
        val result = model.predict(df).last()
        record(metricName, result)
        return result
    }

    override fun generate(event: Event): List<Signal> {
        featureSet.update(event)
        val results = mutableListOf<Signal>()
        if (trained) {
            val pred = predict()
            val signal = getSignal(asset, pred, event)
            results.addNotNull(signal)
        }
        return results
    }

    /**
     * Allow custom logic for generating more advanced signals
     */
    open fun getSignal(asset: Asset, prediction: Double, event: Event) : Signal? {
        val price = event.getPrice(asset) ?: Double.NaN
        val takeProfit = price * (1.0 + prediction)
        return when {
            prediction > percentage -> Signal(asset, Rating.BUY, takeProfit = takeProfit)
            prediction < - percentage ->  Signal(asset, Rating.SELL, takeProfit = takeProfit)
            else -> null
        }

    }

    override fun end(run: String) {
        if (! trained) {
            logger.trace { "start training" }
            train()
            trained = true
        }
    }

    override fun reset() {
        featureSet.reset()
        trained = false
    }

}
