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

import org.junit.jupiter.api.assertThrows
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.common.RoboquantException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HistoricPriceSignalStrategyTest {

    private class MySubClass1 : HistoricPriceStrategy(10) {
        var called = false

        override fun generateSignal(asset: Asset, data: DoubleArray): Signal? {
            called = true
            return null
        }
    }


    private class MySubClass2 : HistoricPriceStrategy(10, priceType = "`CLOSE`") {
        var called = false

        override fun generateRating(data: DoubleArray): Double? {
            called = true
            return null
        }
    }

    private class MySubclass3 : HistoricPriceStrategy(1)

    @Test
    fun test() {
        val c = MySubClass1()
        val event = TestData.event()
        val signals = c.createSignals(event)
        assertTrue(signals.isEmpty())
        assertFalse(c.called)

        repeat(10) { c.createSignals(event) }
        assertTrue(c.called)

        c.reset()
        c.called = false
        c.createSignals(event)
        assertFalse(c.called)
    }

    @Test
    fun test2() {
        val c = MySubClass2()
        val signals = c.createSignals(TestData.event2())
        assertTrue(signals.isEmpty())
        assertFalse(c.called)

        repeat(10) { c.createSignals(TestData.event2()) }
        assertTrue(c.called)

        c.reset()
        c.called = false
        c.createSignals(TestData.event2())
        assertFalse(c.called)
    }

    @Test
    fun test3() {
        val c = MySubclass3()
        assertThrows<RoboquantException> {
            c.createSignals(TestData.event())
            c.createSignals(TestData.event())
            c.createSignals(TestData.event())
        }

    }

}
