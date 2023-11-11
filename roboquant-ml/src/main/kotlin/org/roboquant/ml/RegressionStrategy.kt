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
class RegressionStrategy(
    private val featureSet: DataFrameFeatureSet,
    private val asset: Asset,
    private val percentage: Double = 1.percent,
    val block: (DataFrame) -> DataFrameRegression
) : RecordingStrategy() {

    val logger = Logging.getLogger(this::class)
    private var trained = false

    private lateinit var model: DataFrameRegression

    private fun train() {
        val df = featureSet.getDataFrame()
        model = block(df)
    }

    private fun predict(): Double {
        val df = featureSet.getRow()
        val result = model.predict(df).last()
        record("prediction.${asset.symbol.lowercase()}", result)
        return result
    }

    override fun generate(event: Event): List<Signal> {
        featureSet.update(event)
        if (trained) {
            val g = predict()
            val rating = when {
                g > percentage -> Rating.BUY
                g < - percentage -> Rating.SELL
                else -> null
            }
            if (rating != null) return listOf(Signal(asset, rating))

        }
        return emptyList()
    }

    override fun end(run: String) {
        if (! trained) {
            train()
            trained = true
        }
    }

    override fun reset() {
        featureSet.reset()
        trained = false
    }

}
