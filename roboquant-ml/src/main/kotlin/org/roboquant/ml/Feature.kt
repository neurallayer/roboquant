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

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.Dimension
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.roboquant.feeds.Event
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.toList
import smile.data.vector.DoubleVector


/**
 * A feature contains data that is derived from a series of events
 */
interface Feature<D: Dimension> {

    /**
     * Update the feature with a new event
     */
    fun update(event: Event)

    val name: String

    fun clean() {}

    fun reset() {}

    fun getVectors(): List<DoubleVector>

    val size: Int
        get() = 1

    val shape: IntArray
        get() = intArrayOf(size)

    fun getLast(): List<DoubleVector> {
        return getVectors().map { DoubleVector.of(it.name(), doubleArrayOf(it.array().last())) }
    }

    operator fun get(i: Int) : NDArray<Double, D>


}



/**
 * Small utlity class that makes it simple to write a feature that is only prducing a simple value
 */
abstract class SingelValueFeature : Feature<D1> {

    private val data = mutableListOf<Double>()

    protected fun add(elem: Double) = data.add(elem)

    override fun getVectors(): List<DoubleVector> = listOf(DoubleVector.of(name, data.toDoubleArray()))

    override operator fun get(i: Int) = mk.ndarray(doubleArrayOf(data[i]))

    override fun clean() { data.clear() }

    override fun reset() {
        data.clear()
    }

}


/**
 * Small utlity class that makes it simple to write a feature that is produce multiple values
 */
abstract class MultiValueFeature : Feature<D1> {

    private val data = mutableListOf<DoubleArray>()

    protected fun add(elem: DoubleArray) = data.add(elem)

    override fun getVectors(): List<DoubleVector> {
        return emptyList()
    }

    override fun reset() {
        data.clear()
    }

    override operator fun get(i: Int) = mk.ndarray(data[i])

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



}
