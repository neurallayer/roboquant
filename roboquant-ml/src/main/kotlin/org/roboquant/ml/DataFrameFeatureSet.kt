/*
 * Copyright 2020-2024 Neural Layer
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
class DataFrameFeatureSet(private val historySize: Int = 1_000_000, private val warmup: Int = 0) {

    private class Entry(
        val feature: Feature,
        val matrix: Matrix,
        val offset: Int = 0,
        // var mean: Double = Double.NaN
    ) {
        fun inputData(lastRow: Int): DoubleArray {
            val len = lastRow*feature.size
            val result = DoubleArray(len)
            System.arraycopy(matrix.data, 0, result, 0, len)
            // mean = result.fillNaNMean()
            return result
        }

        fun toDoubleVector(lastRow: Int): DoubleVector {
            return DoubleVector.of(feature.name, inputData(lastRow))
        }

    }

    private val entries = mutableListOf<Entry>()

    private var samples = 0
    private var warmupCountdown = warmup

    /**
     * The names of all the features in this feature set
     */
    val names
        get() = entries.map { it.feature.name }

    private val maxOffset
        get() = entries.maxOf { it.offset }

    /**
     * Add one or more [features] and optionally provide an [offset]
     */
    fun add(vararg features: Feature, offset: Int = 0) {
        for (f in features) {
            require(f.name !in names) { "duplicate feature name ${f.name}" }
            entries.add(Entry(f, Matrix(historySize, f.size), offset))
        }
    }


    /**
     * Returns the training data as a [DataFrame]
     */
    fun getTrainingData(): DataFrame {
        val size = samples - maxOffset
        val i = entries.map {it.toDoubleVector(size) }
        @Suppress("SpreadOperator")
        return DataFrame.of(*i.toTypedArray())
    }


    private fun Entry.getRow(row: Int): DoubleVector {
        val last = samples - offset - 1
        val doubleArr = if (row > last) doubleArrayOf(Double.NaN) else matrix[row]
        return DoubleVector.of(feature.name, doubleArr)
    }

    /**
     * Returns the prediction data as a [DataFrame]
     */
    fun getPredictData(row: Int = samples - 1): DataFrame {
        assert(row >= 0)
        val i = entries.map { it.getRow(row) }
        @Suppress("SpreadOperator")
        return DataFrame.of(*i.toTypedArray())
    }

    /**
     * Update all the features in this set the provided [event] and store the results internally
     */
    fun update(event: Event) {
        if (warmupCountdown > 0) {
            for (entry in entries) entry.feature.calculate(event)
            warmupCountdown--
            return
        }

        for (entry in entries) {
            val value = entry.feature.calculate(event)
            val idx = samples - entry.offset
            if (idx >= 0) {
                entry.matrix[idx] = value
            }
        }
        samples++
    }

    /**
     * Reset internal state and all features
     */
    fun reset() {
        for (f in entries) f.feature.reset()
        samples = 0
        warmupCountdown = warmup
    }


}
