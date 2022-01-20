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

package org.roboquant.strategies


import org.junit.Test
import org.roboquant.TestData
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class CombinedStrategyTest {

    @Test
    fun test() {
        val s1 = EMACrossover.shortTerm()
        val s2 = EMACrossover.midTerm()

        val s = CombinedStrategy(s1, s2)
        assertEquals(2, s.strategies.size)

        val event = TestData.event()
        val signals = s.generate(event)
        assertTrue(signals.isEmpty())

    }

}