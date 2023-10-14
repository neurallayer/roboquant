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
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.operations.div

class GrowthRateFeature(private val f: Feature<D1>, private val n: Int = 1) : Feature<D1> by f {

    override fun get(i: Int): NDArray<Double, D1> {
        if (i < n) return mk.ndarray(DoubleArray(f.size) { Double.NaN })
        return f[i] / f[i - n]
    }


}





fun Feature<D1>.growthRate(n: Int = 1) = GrowthRateFeature(this, n)
