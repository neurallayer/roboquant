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
import org.roboquant.feeds.Event
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.toList
import smile.data.vector.DoubleVector

/**
 * A feature is logic that is able to extract one or more named values from a history of events
 */
interface Feature {
    fun update(event: Event)

    val names: List<String>

    fun reset() {}

    fun getVectors(): List<DoubleVector>

    fun getLast(): List<DoubleVector> {
        return getVectors().map { DoubleVector.of(it.name(), doubleArrayOf(it.array().last())) }
    }
}


abstract class SingelValueFeature : Feature {

    abstract val name: String

    override val names: List<String>
        get() = listOf(name)

    abstract fun getVector(): DoubleVector

    override fun getVectors(): List<DoubleVector> = listOf(getVector())

}

class PriceFeature(override val name: String, private val asset: Asset, private val type: String = "DEFAULT") :
    SingelValueFeature() {

    private val prices = mutableListOf<Double>()

    override fun update(event: Event) {
        val action = event.prices[asset]
        if (action != null) prices.add(action.getPrice(type)) else prices.add(Double.NaN)
    }

    override fun getVector(): DoubleVector {
        return DoubleVector.of(name, prices.toDoubleArray())
    }

}


fun main() {
    val feed = RandomWalkFeed.lastYears(10)
    val asset = feed.assets.first()

    val features = FeatureSet()

    val feature1 = TaLibSingleFeature("f1", asset) {
        ema(it, 20) / ema(it, 20, 1)
    }

    val feature2 = TaLibSingleFeature("f2", asset) {
        max(it, 20)
    }

    val feature3 = PriceFeature("close", asset, "CLOSE")

    features.add(feature1)
    features.add(feature2)
    features.add(feature3)

    for (event in feed.toList()) {
        features.update(event)
    }
    println(features.columns)


}
