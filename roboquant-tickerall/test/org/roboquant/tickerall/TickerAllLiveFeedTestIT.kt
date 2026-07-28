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
import org.roboquant.common.PriceQuote
import org.roboquant.common.PriceItem
import org.roboquant.common.Timeframe
import org.roboquant.common.seconds
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Integration test for the live tick feed. Runs only when `TICKERALL_API_KEY` / `TICKERALL_ACCOUNT_ID` are
 * set; otherwise it returns early so CI stays green. The optional `TICKERALL_SYMBOL` selects the instrument
 * (default `EURUSD`). The run is bounded to a short window so it always terminates.
 */
internal class TickerAllLiveFeedTestIT {

    private val liveTestTime = 30.seconds

    @Test
    fun subscribeToLiveTicks() {
        val key = Config.getProperty("TICKERALL_API_KEY") ?: return
        val id = Config.getProperty("TICKERALL_ACCOUNT_ID") ?: return
        val symbol = Config.getProperty("TICKERALL_SYMBOL") ?: "EURUSD"

        val feed = TickerAllLiveFeed {
            apiKey = key
            accountId = id
        }
        feed.subscribe(symbol)

        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()

        if (actions.isNotEmpty()) {
            val action = actions.first().second
            assertTrue(action is PriceQuote)
            assertTrue(action.asset.symbol == symbol)
        } else {
            println("No ticks received (market closed?)")
        }
    }
}
