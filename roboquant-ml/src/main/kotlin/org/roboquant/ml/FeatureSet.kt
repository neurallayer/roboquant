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

import org.roboquant.feeds.Event
import smile.data.vector.DoubleVector


class FeatureSet {

    private val features = mutableListOf<Feature>()
    var size = 0


    fun add(vararg feature: Feature) {
        features.addAll(feature)
    }

    fun update(event: Event) {

        for (feature in features) {
            feature.update(event)
        }
        size++

    }

    private fun DoubleVector.drop(n: Int): DoubleVector = DoubleVector.of(name(), array().drop(n))

    fun getVectors(drop: Int = 0)  = features.map { it.getVectors() }.flatten().map { it.drop(drop)}.toTypedArray()

    fun getLastVectors() = features.map { it.getLast() }.flatten().toTypedArray()

    val columns
        get() = getVectors().map { it.name() }.toSortedSet()

}
