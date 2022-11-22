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

package org.roboquant.ta

import com.tictactec.ta.lib.Compatibility
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.roboquant.Roboquant
import org.roboquant.RunPhase
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.feeds.test.HistoricTestFeed
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import java.lang.IllegalArgumentException
import java.time.Instant
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class TaLibStrategyTest {

    @Test
    fun taCore() {
        val taLib = TaLib()
        assertEquals("Default", taLib.core.compatibility.name)
    }

    @Test
    fun test() {

        val strategy = TaLibStrategy(50)
        strategy.buy { price ->
            ema(price.close, 30) > ema(price.close, 50) && cdlMorningStar(price)
        }

        strategy.sell { price ->
            cdl3BlackCrows(price) || (cdl2Crows(price, 1) && ema(price.close, 30) < ema(price.close, 50))
        }

        val x = run(strategy, 60)
        assertEquals(60, x.size)

        assertEquals(strategy.taLib.core.compatibility, Compatibility.Default)
    }

    @Test
    fun testTASignalStrategy() {

        val strategy = TaLibSignalStrategy(20) { asset, series ->
            // record("test", 1)
            when {
                cdlMorningStar(series) -> Signal(asset, Rating.BUY, source = "Morning Star")
                cdl3BlackCrows(series) -> Signal(asset, Rating.SELL, source = "Black Crows")
                else -> null
            }
        }

        val x = run(strategy, 30)
        assertEquals(30, x.size)
        assertEquals(strategy.taLib.core.compatibility, Compatibility.Default)
    }

    @Test
    fun testTASignalStrategyInsufficientData() {

        val strategy = TaLibSignalStrategy(5) { asset, series ->
            if (cdlMorningStar(series)) Signal(asset, Rating.BUY)
            else if (cdl3BlackCrows(series)) Signal(asset, Rating.SELL)
            else null
        }

        assertFailsWith<InsufficientDataException> {
            run(strategy, 30)
        }
    }

    @Test
    fun taBreakout() {
        val strategy = TaLibStrategy.breakout(10, 30)
        val x = run(strategy, 60)
        assertEquals(60, x.size)
    }


    @Test
    fun taSignalBreakout() {
        val strategy = TaLibSignalStrategy.breakout(10, 30)
        val x = run(strategy, 60)
        assertEquals(60, x.size)
        assertContains(strategy.toString(), "30")

        val strategy2 = TaLibSignalStrategy.breakout()
        assertContains(strategy2.toString(), "100")
    }

    @Test
    fun testFail() {
        val strategy = TaLibStrategy(3)
        strategy.buy { price -> cdlMorningStar(price) }

        assertFailsWith<InsufficientDataException> {
            run(strategy)
        }
    }

    private fun getPriceBarBuffer(size: Int): PriceBarSeries {
        val asset = Asset("XYZ")
        val result = PriceBarSeries(size)
        repeat(size) {
            val pb = PriceBar(asset, 10.0, 12.0, 8.0, 11.0, 100 + it)
            result.add(pb)
        }
        return result
    }

    private fun run(s: Strategy, n: Int = 150): Map<Instant, List<Signal>> {
        s.reset()
        s.start(RunPhase.MAIN)
        val nHalf = n / 2
        val feed = HistoricTestFeed(100 until 100 + nHalf, 100 + nHalf - 1 downTo 100, priceBar = true)
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
        var strategy = TaLibStrategy.emaCrossover(50, 10)
        var s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TaLibStrategy.smaCrossover(50, 10)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TaLibStrategy.recordHighLow(100)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TaLibStrategy.recordHighLow(20, 50, 100)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        strategy = TaLibStrategy.rsi(20)
        s = run(strategy)
        assertTrue(s.isNotEmpty())

        assertThrows<IllegalArgumentException> {
            TaLibStrategy.rsi(20, -10.0, 110.0)
        }
    }

    @Test
    fun testExtraIndicators() {
        val data = getPriceBarBuffer(20)
        val taLib = TaLib()

        var a = taLib.recordHigh(data.close, 10)
        var b = taLib.recordHigh(data, 10)
        assertEquals(a, b)

        a = taLib.recordLow(data.close, 10)
        b = taLib.recordLow(data, 10)
        assertEquals(a, b)

    }

    @Test
    fun test2() {
        val strategy = TaLibStrategy(2)
        strategy.buy {
            true
        }

        val s = run(strategy, 10)
        assertEquals(10, s.entries.size)
        assertTrue(s.values.first().isEmpty())
        assertTrue(s.values.last().isNotEmpty())
    }

    @Test
    fun noRules() {
        // Default rule is false, meaning no signals
        val strategy = TaLibStrategy(30)
        val roboquant = Roboquant(strategy)
        val feed = RandomWalkFeed.lastYears(1, nAssets = 2)
        roboquant.run(feed)
        val account = roboquant.broker.account
        assertTrue(account.closedOrders.isEmpty())
    }

}

