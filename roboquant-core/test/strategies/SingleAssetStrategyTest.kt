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
import org.roboquant.common.Asset
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceQuote
import java.time.Instant
import kotlin.test.assertEquals

internal class SingleAssetStrategyTest {

    class MyStrategy(asset: Asset) : SingleAssetStrategy(asset) {

        override fun generate(priceAction: PriceAction, now: Instant): Signal {
            return Signal(asset, Rating.BUY)
        }

    }


    @Test
    fun test()  {
        val asset = Asset("DUMMY")
        val strategy1 = MyStrategy(asset)
        val s = strategy1.generate(PriceQuote(asset, 100.0, 100.0, 100.0, 100.0), Instant.now())
        assertEquals(asset, s.asset)
    }

}