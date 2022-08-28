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

package org.roboquant.strategies

import kotlin.test.*
import org.roboquant.TestData
import org.roboquant.common.AssetFilter
import org.roboquant.feeds.Event
import java.time.Instant

internal class AssetFilterStrategyTest {

    private class AlwaysStrategy(private val rating: Rating = Rating.BUY) : Strategy {
        override fun generate(event: Event): List<Signal> {
            return event.prices.keys.map { Signal(it, rating) }
        }

    }

    @Test
    fun test() {
        val action = TestData.priceAction()
        val strategy = AlwaysStrategy().filter(AssetFilter.all())
        val event = Event(listOf(action), Instant.now())
        val signals = strategy.generate(event)
        assertEquals(1, signals.size)
        assertEquals(Rating.BUY, signals.first().rating)

        val asset = action.asset
        val strategy2 = AlwaysStrategy().filter(AssetFilter.excludeSymbols(asset.symbol))
        val signals2 = strategy2.generate(event)
        assertTrue(signals2.isEmpty())
    }

}