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

import smile.data.vector.DoubleVector

class GrowthRateFeature(private val f: Feature, private val n:Int = 1) : Feature by f {

    private fun DoubleArray.returns2(): DoubleArray {
        val result = DoubleArray(size)
        for (i in 0..<n) result[i] = Double.NaN
        for (i in n..lastIndex) result[i] = get(i) / get(i - n)
        return result
    }


    override fun getVectors(): List<DoubleVector> {
        return f.getVectors().map { DoubleVector.of(it.name(), it.array().returns2()) }
    }
}


fun Feature.growthRate(n: Int = 1) = GrowthRateFeature(this, n)
