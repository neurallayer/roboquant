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

import org.roboquant.TestData
import org.roboquant.run
import kotlin.test.Test
import kotlin.test.assertNotEquals

internal class EMACrossoverTest {

    @Test
    fun simpleTest() {
        val strategy = EMACrossover()
        run(TestData.feed, strategy)
    }

    @Test
    fun simple2() {
        val strategy1 = EMACrossover.PERIODS_5_15
        val strategy2 = EMACrossover.PERIODS_12_26
        val strategy3 = EMACrossover.PERIODS_50_200
        assertNotEquals(strategy1, strategy2)
        assertNotEquals(strategy2, strategy3)
    }

}
