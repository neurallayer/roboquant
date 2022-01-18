/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.strategies.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PairBufferTest {

    @Test
    fun test() {
        val pb = PairBuffer(20)
        for (i in 1..25) {
            val d = i.toDouble()
            pb.add(d * 2.0, d)
        }
        assertTrue(pb.isAvailable())
        assertEquals(1.0, pb.beta())
    }

    @Test
    fun test2() {
        val pb = PairBuffer(50)
        for (i in 1..51) {
            val d = i.toDouble()
            pb.add(d, d)
        }
        assertTrue(pb.covariance() > 0.0)
    }

}