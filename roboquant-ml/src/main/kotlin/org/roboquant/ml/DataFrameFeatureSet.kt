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
import smile.data.DataFrame
import smile.data.vector.DoubleVector

/**
 * FeatureSet contains one or more [features][Feature] and can return it as a dataframe
 */
class DataFrameFeatureSet(private val historySize: Int = 1_000_000) {

    private class Entry(val feature: Feature, val data: DoubleArray, val offset: Int = 0)

    private val features = mutableListOf<Entry>()

    private var samples = 0

    val names
        get() = features.map { it.feature.name }

    private val maxOffset
        get() = features.maxOf { it.offset }

    fun add(vararg feature: Feature, offset: Int = 0) {
        for (f in feature) {
            require(f.name !in names) { "duplicate name ${f.name}"}
            features.add(Entry(f, DoubleArray(historySize), offset))
        }
    }

    private fun Entry.inputData(size: Int): DoubleArray {
        val result = DoubleArray(size)
        System.arraycopy(data, 0, result, 0, size)
        return result
    }


    fun getDataFrame(): DataFrame {
        val size = samples - maxOffset
        val i = features.map { DoubleVector.of(it.feature.name, it.inputData(size)) }
        @Suppress("SpreadOperator")
        return DataFrame.of(*i.toTypedArray())
    }


    private fun Entry.getRow(row: Int): DoubleVector {
        val last = samples - offset - 1
        val doubleArr = if (row > last) doubleArrayOf(Double.NaN) else doubleArrayOf(data[row])
        return DoubleVector.of(feature.name, doubleArr)
    }

    fun getRow(row: Int = samples - 1): DataFrame {
        assert(row >= 0)
        val i = features.map { it.getRow(row) }
        @Suppress("SpreadOperator")
        return DataFrame.of(*i.toTypedArray())
    }

    fun update(event: Event) {
        for (entry in features) {
            val value = entry.feature.calculate(event)
            val idx = samples - entry.offset
            if (idx >= 0) entry.data[idx] = value
        }
        samples++
    }

    fun reset() {
        for (f in features) f.feature.reset()
    }


}
