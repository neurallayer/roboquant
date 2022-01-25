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
import org.roboquant.common.seconds
import org.roboquant.feeds.Event
import org.roboquant.strategies.ta.TALib
import org.roboquant.strategies.utils.PriceBarBuffer
import java.time.Instant
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class TAStrategyTest {

    @Test
    fun test() {

        val strategy = TAStrategy(50)
        strategy.buy { price ->
            ta.ema(price.close, 30) > ta.ema(price.close, 50) && ta.cdlMorningStar(price)
        }

        strategy.sell { price ->
            ta.cdl3BlackCrows(price) || (ta.cdl2Crows(price, 1) && ta.ema(price.close, 30) < ta.ema(price.close, 50))
        }

        val x = run(strategy, 60)
        assertEquals(60, x.size)
    }

    @Test
    fun testFail() {
        val strategy = TAStrategy(3)
        strategy.buy { price -> ta.cdlMorningStar(price) }

        assertFailsWith<InsufficientData> {
            run(strategy)
        }
    }

    private fun getPriceBarBuffer(size: Int) : PriceBarBuffer {
        val result = PriceBarBuffer(size)
        repeat(size) {
            result.update(TestData.priceBar(), Instant.now())
        }
        return result
    }

    private fun run(s: TAStrategy, n: Int = 100): Map<Instant, List<Signal>> {
        val actions = listOf(TestData.priceBar())
        val result = mutableMapOf<Instant, List<Signal>>()
        var now = Instant.now()
        repeat(n) {
            val event = Event(actions, now)
            val signals = s.generate(event)
            result[now] = signals
            now += 1.seconds
        }
        return result
    }

    @Test
    fun testPredefined() {
        var strategy = TAStrategy.emaCrossover(50, 10)
        var s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TAStrategy.smaCrossover(50, 10)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TAStrategy.recordHighLow(100)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TAStrategy.recordHighLow(20, 50, 100)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TAStrategy.rsi(20)
        s = run(strategy)
        assertTrue(s.isNotEmpty())
    }

    @Test
    fun testExtraIndicators() {
        val data = getPriceBarBuffer(20)
        var a = TALib.recordHigh(data.close, 10)
        var b = TALib.recordHigh(data, 10)
        assertEquals(a, b)

        a = TALib.recordLow(data.close, 10)
        b = TALib.recordLow(data, 10)
        assertEquals(a, b)

        val c = TALib.vwap(data, 10)
        assertTrue(c.isFinite())
    }


    @Test
    fun test2() {
        val strategy = TAStrategy(2)
        strategy.buy {
            true
        }

        val s = run(strategy, 10)
        assertEquals(10, s.entries.size)
        assertTrue(s.values.first().isEmpty())
        assertTrue(s.values.last().isNotEmpty())
    }


    @Test
    fun testMetrics() {
        val strategy = TAStrategy(10)
        strategy.buy {
            val a = ta.sma(it.close, 5)
            val b = ta.sma(it.close, 10)
            record("sma.fast", a)
            record("sma.slow", b)
            a > b
        }

        run(strategy)
        var metrics = strategy.getMetrics()
        assertContains(metrics, "sma.fast")
        assertContains(metrics, "sma.slow")

        metrics = strategy.getMetrics()
        assertTrue(metrics.isEmpty())
    }
}