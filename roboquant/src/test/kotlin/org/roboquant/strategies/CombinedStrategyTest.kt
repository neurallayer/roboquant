/*
 * Copyright 2020-2023 Neural Layer
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

import kotlin.test.Test
import org.roboquant.TestData
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CombinedStrategyTest {

    class MyStrategy : Strategy {

        override fun generate(event: Event): List<Signal> {
            return emptyList()
        }

        override fun getMetrics(): Map<String, Double> {
            return emptyMap()
        }

    }

    @Test
    fun test() {
        val s1 = MyStrategy()
        val s2 = MyStrategy()
        val s = CombinedStrategy(s1, s2)
        assertEquals(2, s.strategies.size)
        s.reset()
        val run = "test"
        s.start(run, Timeframe.INFINITE)
        val signals = mutableListOf<Signal>()
        for (event in TestData.events(10)) signals += s.generate(event)
        assertTrue(signals.isEmpty())
        assertTrue(s.getMetrics().isEmpty())
        s.end(run)
    }

}
