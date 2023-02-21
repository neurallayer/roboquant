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

package org.roboquant.policies

import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.orders.CreateOrder
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SignalResolutionTest {

    @Test
    fun rules() {
        val asset = TestData.usStock()
        val signals = listOf(Signal(asset, Rating.BUY), Signal(asset, Rating.SELL), Signal(asset, Rating.SELL))
        assertEquals(signals.first(), signals.resolve(SignalResolution.FIRST).first())
        assertEquals(signals.last(), signals.resolve(SignalResolution.LAST).last())
        assertTrue(signals.resolve(SignalResolution.NO_DUPLICATES).isEmpty())
        assertTrue(signals.resolve(SignalResolution.NO_CONFLICTS).isEmpty())
        assertEquals(signals, signals.resolve(SignalResolution.NONE))
    }


    @Test
    fun testSignalShuffle() {
        val policy = TestPolicy().shuffleSignals(Random(42))
        val account = TestData.usAccount()
        val assets = listOf(Asset("A"), Asset("B"), Asset("C"), Asset("D"))
        val signals = assets.map { Signal(it, Rating.BUY) }
        val orders = policy.act(signals, account, Event.empty())
        assertEquals(signals.size, orders.size)
    }

    @Test
    fun testSkipSymbols() {
        val policy = TestPolicy().skipSymbols("A", "C")
        val account = TestData.usAccount()
        val assets = listOf(Asset("A"), Asset("B"), Asset("C"), Asset("D"))
        val signals = assets.map { Signal(it, Rating.BUY) }
        val orders = policy.act(signals, account, Event.empty())
        val symbols = orders.filterIsInstance<CreateOrder>().map { it.asset.symbol }
        assertTrue(symbols.contains("B"))
        assertFalse(symbols.contains("C"))
    }

}