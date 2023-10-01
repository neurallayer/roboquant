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

import org.roboquant.common.Config
import org.roboquant.feeds.Event
import smile.data.vector.DoubleVector


class FeatureSet {

    private val features = mutableListOf<Feature>()
    var size = 0

    val names
        get() = features.map { it.names }.flatten()


    fun add(vararg feature: Feature) {
        features.addAll(feature)
    }

    fun update(event: Event) {

        for (feature in features) {
            feature.update(event)
        }
        size++

    }


    fun sample(n: Int, warmup: Int, future: Int): List<DoubleArray> {
        val result = mutableListOf<DoubleArray>()
        var first = true
        repeat(n) {
            var idx = 0
            val s = Config.random.nextInt(size - warmup - future) + warmup
            for (feature in features) {
                val offset = if (feature.names.contains("Y")) s + future else s
                val arrs = feature[offset]
                for (d in arrs) {
                    if (first) result.add(DoubleArray(n))
                    result[idx][n] = d
                    idx++
                }

            }
            first = false
        }
        return result
    }

    private fun DoubleVector.drop(n: Int): DoubleVector = DoubleVector.of(name(), array().drop(n))

    fun getVectors(drop: Int = 0)  = features.map { it.getVectors() }.flatten().map { it.drop(drop)}.toTypedArray()

    fun getLastVectors() = features.map { it.getLast() }.flatten().toTypedArray()

    val columns
        get() = getVectors().map { it.name() }.toSortedSet()

}
