/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.strategies

import org.junit.jupiter.api.Assertions.assertEquals
import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ParallelSignalStrategyTest {

    @Test
    fun test() {
        val s1 = EMACrossover.PERIODS_12_26
        val s2 = EMACrossover.PERIODS_5_15
        val s3 = ParallelStrategy(s1, s2)
        var signals = 0
        for (event in TestData.events(1_000)) {
            val r3 = s3.createSignals(event)
            signals += r3.size
        }
        // assertTrue(signals > 0)
    }

}
