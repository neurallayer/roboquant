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

package org.roboquant.ta

import org.junit.jupiter.api.Test
import org.roboquant.RunPhase
import org.roboquant.common.Asset
import org.roboquant.common.seconds
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.test.HistoricTestFeed
import org.roboquant.strategies.*
import org.roboquant.strategies.utils.PriceBarBuffer
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class TAStrategyTest {

    @Test
    fun test() {

        val strategy = TALibStrategy(50)
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
    fun testTASignalStrategy() {

        val strategy = TASignalStrategy(20) { price, asset ->
            record("test", 1)
            when {
                ta.cdlMorningStar(price) -> Signal(asset, Rating.BUY, source = "Morning Star")
                ta.cdl3BlackCrows(price) -> Signal(asset, Rating.SELL, source = "Black Crows")
                else -> null
            }
        }

        val x = run(strategy, 30)
        assertEquals(30, x.size)
        assertContains(strategy.getMetrics(), "test")
    }

    @Test
    fun testTASignalStrategyInsufficientData() {

        val strategy = TASignalStrategy(5) { price, asset ->
            record("test", 1)
            if (ta.cdlMorningStar(price)) Signal(asset, Rating.BUY)
            else if (ta.cdl3BlackCrows(price)) Signal(asset, Rating.SELL)
            else null
        }

        assertFailsWith<InsufficientData> {
            run(strategy, 30)
        }
    }

    @Test
    fun taBreakout() {
        val strategy = TALibStrategy.breakout(10, 30)
        val x = run(strategy, 60)
        assertEquals(60, x.size)
    }

    @Test
    fun taSignalBreakout() {
        val strategy = TASignalStrategy.breakout(10, 30)
        val x = run(strategy, 60)
        assertEquals(60, x.size)
    }

    @Test
    fun testFail() {
        val strategy = TALibStrategy(3)
        strategy.buy { price -> ta.cdlMorningStar(price) }

        assertFailsWith<InsufficientData> {
            run(strategy)
        }
    }

    private fun getPriceBarBuffer(size: Int): PriceBarBuffer {
        val result = PriceBarBuffer(size)
        val asset = Asset("XYZ")
        repeat(size) {
            val pb = PriceBar(asset, 10.0, 12.0, 8.0, 11.0, 100 + it)
            result.update(pb, Instant.now())
        }
        return result
    }

    private fun run(s: Strategy, n: Int = 100): Map<Instant, List<Signal>> {
        s.reset()
        s.start(RunPhase.MAIN)
        val feed = HistoricTestFeed(100 until 100 + n, priceBar = true)
        val events = feed.filter<PriceBar>()
        val result = mutableMapOf<Instant, List<Signal>>()
        var now = Instant.now()
        for (event in events) {
            val signals = s.generate(Event(listOf(event.second), event.first))
            result[now] = signals
            now += 1.seconds
        }
        return result
    }

    @Test
    fun testPredefined() {
        var strategy = TALibStrategy.emaCrossover(50, 10)
        var s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TALibStrategy.smaCrossover(50, 10)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TALibStrategy.recordHighLow(100)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TALibStrategy.recordHighLow(20, 50, 100)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TALibStrategy.rsi(20)
        s = run(strategy)
        assertTrue(s.isNotEmpty())
    }

    @Test
    fun testExtraIndicators() {
        val data = getPriceBarBuffer(20)
        val ta = TALib

        var a = ta.recordHigh(data.close, 10)
        var b = ta.recordHigh(data, 10)
        assertEquals(a, b)

        a = ta.recordLow(data.close, 10)
        b = ta.recordLow(data, 10)
        assertEquals(a, b)

        val c = ta.vwap(data, 10)
        assertTrue(c.isFinite())
    }

    @Test
    fun testSma() {
        val feed = HistoricTestFeed(100..150, priceBar = true)
        val asset = feed.assets.first()

        val closingPrice = feed.filter<PriceBar> { it.asset == asset }.map { it.second.close }.toDoubleArray()
        assertTrue(closingPrice.size > 30)

        val emaBatch1 = TALibBatch.sma(closingPrice)
        val emaBatch2 = TALibBatch.sma(closingPrice)
        assertEquals(emaBatch1.last(), emaBatch2.last())

        val ta = TALib
        val ema = ta.sma(closingPrice, 30)
        assertTrue((emaBatch1.last() - ema).absoluteValue < 0.00001)
    }

    @Test
    fun test2() {
        val strategy = TALibStrategy(2)
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
        val strategy = TALibStrategy(10)
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

