/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.traders

import org.roboquant.TestData
import org.roboquant.common.Stock
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SignalResolutionTest {

    @Test
    fun testSignalShuffle() {
        val policy = FlexTrader().shuffleSignals(Random(42))
        val account = TestData.usAccount()
        val assets = listOf(Stock("A"), Stock("B"), Stock("C"), Stock("D"))
        val signals = assets.map { Signal(it, 1.0) }
        val items = assets.map { TradePrice(it, 100.0, 10.0) }
        val orders = policy.createOrders(signals, account, Event(Instant.now(), items))
        assertEquals(signals.size, orders.size)
    }

}
