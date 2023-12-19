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

class Matrix(private val rows: Int, private val columns: Int) {

    internal val data = DoubleArray(rows * columns) {Double.NaN}

    operator fun set(row: Int, value: DoubleArray) {
        System.arraycopy(value, 0, data, row * columns, columns)
    }

    operator fun get(row: Int): DoubleArray {
        assert(row < rows)
        val result = DoubleArray(columns) {Double.NaN}
        System.arraycopy(data, row*columns, result, 0, columns)
        return result
    }

    operator fun get(start: Int, end: Int): DoubleArray {
        assert(end < rows)
        val len = end - start
        val result = DoubleArray(columns * len) {Double.NaN}
        System.arraycopy(data, start*columns, result, 0, len*columns)
        return result
    }

}
