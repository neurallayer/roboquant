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
import org.roboquant.common.percent
import org.roboquant.feeds.Event
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import smile.data.DataFrame
import smile.regression.DataFrameRegression


class RegressionStrategy(
    private val features: FeatureSet,
    private val asset: Asset,
    private val percentage: Double = 1.percent,
    private val train: Int,
    private val warmup: Int,
    val block: (DataFrame) -> DataFrameRegression
) : Strategy {

    private lateinit var model: DataFrameRegression

    private fun train() {
        val vectors = features.getVectors(warmup)
        assert(vectors.isNotEmpty())
        val first = vectors.first()
        assert(first.size() > 0)
        assert(vectors.all { it.size() == first.size() })
        val df = DataFrame.of(*vectors)
        model = block(df)
    }

    private fun predict(): Double {
        val vectors = features.getLastVectors()
        assert(vectors.isNotEmpty())
        assert(vectors.all { it.size() == 1 })
        val df = DataFrame.of(*vectors)
        val result = model.predict(df)
        return result.last()
    }

    override fun generate(event: Event): List<Signal> {
        features.update(event)
        if (features.size == train) train()
        if (features.size > train) {
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


}
