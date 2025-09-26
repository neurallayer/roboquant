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
import org.roboquant.common.Event
import org.roboquant.common.Signal
import org.roboquant.common.Stock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CombinedSignalStrategyTest {

    class MyStrategy : Strategy {

        override fun createSignals(event: Event): List<Signal> {
            return emptyList()
        }


    }

    @Test
    fun test() {
        val s1 = MyStrategy()
        val s2 = MyStrategy()

        val signal = Signal(asset = Stock("A"), rating = 999.0)
        val resolver: SignalResolver = { listOf(signal) }
        val s = CombinedStrategy(s1, s2, signalResolver = resolver)
        assertEquals(2, s.strategies.size)

        val signals = mutableListOf<Signal>()
        for (event in TestData.events(10)) signals += s.createSignals(event)

        val expected = (1..10).map { signal }
        assertEquals(expected, signals)
    }

    @Test
    fun test2() {
        val s1 = MyStrategy()
        val s2 = MyStrategy()

        val signal = Signal(asset = Stock("A"), rating = 999.0)
        val resolver: SignalResolver = { listOf(signal) }
        val s = ParallelStrategy(s1, s2, signalResolver = resolver)
        assertEquals(2, s.strategies.size)

        val signals = mutableListOf<Signal>()
        for (event in TestData.events(10)) signals += s.createSignals(event)

        val expected = (1..10).map { signal }
        assertEquals(expected, signals)
    }


}
