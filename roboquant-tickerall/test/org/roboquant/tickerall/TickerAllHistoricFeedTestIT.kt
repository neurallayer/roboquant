/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.tickerall

import org.roboquant.common.Config
import org.roboquant.common.PriceBar
import org.roboquant.common.PriceItem
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration test for the historic candle feed. Runs when `TICKERALL_API_KEY` is set with EITHER a
 * MetaTrader login (`TICKERALL_BROKER`/`TICKERALL_SERVER`/`TICKERALL_ACCOUNT`/`TICKERALL_PASSWORD`) or a
 * pre-connected `TICKERALL_ACCOUNT_ID` (the id is derived from the login when only that is given);
 * otherwise it returns early so CI stays green. The optional `TICKERALL_SYMBOL` selects the instrument
 * (default `EURUSD`).
 */
internal class TickerAllHistoricFeedTestIT {

    @Test
    fun retrieveCandles() {
        val key = Config.getProperty("TICKERALL_API_KEY") ?: return
        val id = resolveTickerAllAccountId(key) ?: return
        val symbol = Config.getProperty("TICKERALL_SYMBOL") ?: "EURUSD"

        val feed = TickerAllHistoricFeed {
            apiKey = key
            accountId = id
        }
        feed.retrieve(symbol, timeframe = "H1", hours = 168)

        val actions = feed.filter<PriceItem>()
        if (actions.isNotEmpty()) {
            assertTrue(actions.first().second is PriceBar)
            assertTrue(actions.all { it.second.asset.symbol == symbol })
            // bars must be time-ordered
            val times = actions.map { it.first }
            assertTrue(times == times.sorted())
        } else {
            println("No candles returned (market closed or symbol not available?)")
        }

        feed.close()
        assertTrue(feed.assets.isEmpty())
    }
}
