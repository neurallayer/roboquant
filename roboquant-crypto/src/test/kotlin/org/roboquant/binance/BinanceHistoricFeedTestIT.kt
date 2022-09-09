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

package org.roboquant.binance

import org.junit.jupiter.api.Test
import org.roboquant.common.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BinanceHistoricFeedTestIT {

    @Test
    fun test() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = BinanceHistoricFeed()
        assertTrue(feed.availableAssets.isNotEmpty())

        val asset = feed.availableAssets.getBySymbol("BTC/BUSD")
        assertEquals(asset.type, AssetType.CRYPTO)
        assertEquals(asset.currencyPair, Pair(Currency.BTC, Currency.getInstance("BUSD")))

        val tf = Timeframe.past(100.days)
        feed.retrieve("BTCBUSD", timeframe = tf)
        assertEquals(1, feed.assets.size)
    }

}