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


class RegressionStrategy(
    private val features: DataFrameFeatureSet,
    private val asset: Asset,
    private val percentage: Double = 1.percent,
    private val train: Int,
    val block: (DataFrame) -> DataFrameRegression
) : RecordingStrategy() {

    val logger = Logging.getLogger(this::class)

    private lateinit var model: DataFrameRegression

    private fun train() {
        val df = features.getDataFrame()
        model = block(df)
    }

    private fun predict(): Double {
        val df = features.getRow()
        val result = model.predict(df).last()
        record("prediction", result)
        return result
    }

    override fun generate(event: Event): List<Signal> {
        features.update(event)
        if (features.samples == train) train()
        if (features.samples > train) {
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

    override fun reset() {
        features.reset()
    }

}
