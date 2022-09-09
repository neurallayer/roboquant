/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.strategies

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import kotlin.test.assertEquals

internal class ParallelStrategyTest {

    @Test
    fun test() {
        val s1 = EMACrossover(1, 3)
        val s2 = EMACrossover(3, 5)
        val s3 = ParallelStrategy(EMACrossover(1, 3), EMACrossover(3, 5))

        for (event in TestData.events(20)) {
            val r1 = s1.generate(event)
            val r2 = s2.generate(event)
            val r3 = s3.generate(event)
            assertEquals(r1.size + r2.size, r3.size)
        }

    }

}