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

@file:Suppress("unused")

package org.roboquant.ml

import org.roboquant.common.Config


/**
 * Drop first [n] elements from the array and return the result
 */
fun DoubleArray.drop(n: Int = 1): DoubleArray {
    if (n == 0) return this
    val newSize = size - n
    val data = DoubleArray(newSize)
    System.arraycopy(this, n, data, 0, newSize)
    return data
}

/**
 * Concatenate a number of arrays
 */
fun concatenate(vararg arrays: DoubleArray): DoubleArray = concatenate(arrays.toList())

/**
 * Concatenate a number of arrays and return the result. The arrays can be of different size
 */
fun concatenate(arrays: Collection<DoubleArray>): DoubleArray {
    if (arrays.isEmpty()) return DoubleArray(0)

    val size = arrays.sumOf { it.size }
    val data = DoubleArray(size)
    var offset = 0
    arrays.forEach { arr ->
        val s = arr.size
        System.arraycopy(arr, 0, data, offset, s)
        offset += s
    }

    return data
}

/**
 * Sample [n] elements of [size] from the array and return the result. The result is return as a list of rows
 */
fun DoubleArray.sample(size: Int, n: Int = 1) = buildList {
    val r = Config.random
    val max = this.size - size
    repeat(n) {
        val offset = r.nextInt(max)
        val arr = DoubleArray(size)
        System.arraycopy(this@sample, offset, arr, 0, size)
        add(arr)
    }
}


class Sampler(private val data: DoubleArray, private val size: Int) {

    private val r = Config.random
    private val max = this.size - size

    fun sample(): DoubleArray {
        val arr = DoubleArray(size)
        val offset = r.nextInt(max)
        System.arraycopy(this, offset, arr, 0, size)
        return arr
    }

}

/**
 * Replace all NaN values with the mean.
 * There should be at least one finite number in the array
 */
internal fun DoubleArray.fillNaNMean(size: Int = this.size): Double {
    var n = 0
    var sum = 0.0
    for (idx in 0..<size) {
        val v = get(idx)
        if (!v.isNaN()) {
            n++
            sum += v
        }
    }
    val avg = sum / n
    if (n < size) for (idx in 0..<size) if (get(idx).isNaN()) set(idx, avg)
    return avg

}

/**
 * Sample [n] elements of [size] from the array and return the result as a list of columns
 */
fun DoubleArray.sampleColumns(size: Int, n: Int = 1): List<DoubleArray> {
    val result = mutableListOf<DoubleArray>()
    val max = this.size - size
    repeat(size) {
        result.add(DoubleArray(n))
    }
    val r = Config.random
    repeat(n) {
        val offset = r.nextInt(max)
        for (i in 0..<size) {
            result[i][it] = this[offset + i]
        }

    }
    return result
}

/**
 * Drop last [n] elements from the array and return the result
 */
fun DoubleArray.dropLast(n: Int = 1): DoubleArray {
    val newSize = size - n
    val data = DoubleArray(newSize)
    System.arraycopy(this, 0, data, 0, newSize)
    return data
}

/**
 * Take last [n] elements from the array and return the result
 */
fun DoubleArray.takeLast(n: Int = 1): DoubleArray {
    val data = DoubleArray(n)
    System.arraycopy(this, size - n, data, 0, n)
    return data
}

