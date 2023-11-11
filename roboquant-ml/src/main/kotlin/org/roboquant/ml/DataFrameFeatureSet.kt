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
class DataFrameFeatureSet(private val offset: Int = 0, private val warmup: Int = 0) {

    private class Entry(val feature: Feature, val data: DoubleArray, val offset: Int = 0)

    private val input = mutableListOf<Entry>()
    private val labels = mutableListOf<Entry>()
    var samples = 0

    val names
        get() = input.map { it.feature.name }


    fun addInput(vararg feature: Feature) {
        for (f in feature) input.add(Entry(f, DoubleArray(1_000_000)))
    }

    fun addLabel(vararg feature: Feature) {
        for (f in feature) labels.add(Entry(f, DoubleArray(1_000_000)))
    }

    private fun DoubleArray.inputData(): DoubleArray {
        val result = DoubleArray(samples - offset)
        System.arraycopy(this, 0, result, 0, result.size)
        return result
    }

    private fun DoubleArray.labelData(): DoubleArray {
        val result = DoubleArray(samples - offset)
        System.arraycopy(this, offset, result, 0, result.size)
        return result
    }

    fun getDataFrame(): DataFrame {
        val i = input.map { DoubleVector.of(it.feature.name, it.data.inputData()) }
        val l = labels.map { DoubleVector.of(it.feature.name, it.data.labelData()) }
        val total = (l + i).toTypedArray()
        @Suppress("SpreadOperator")
        return DataFrame.of(*total)
    }


    private fun Entry.getRow(row: Int): DoubleVector {
        val doubleArr = if (row > data.lastIndex) doubleArrayOf(Double.NaN) else  doubleArrayOf(data[row])
        return DoubleVector.of(feature.name, doubleArr)
    }

    fun getRow(row: Int = samples - 1): DataFrame {
        val i = input.map { it.getRow(row) }
        val l = labels.map { it.getRow(row) }
        val total = (l + i).toTypedArray()
        @Suppress("SpreadOperator")
        return DataFrame.of(*total)
    }

    fun update(event: Event) {
        for (entry in input) {
            val value = entry.feature.calculate(event)
            if (samples >= warmup) entry.data[samples] = value
        }
        for (entry in labels) {
            val value = entry.feature.calculate(event)
            if (samples >= warmup) entry.data[samples] = value
        }
        samples++
    }

    fun reset() {
        
    }


}
