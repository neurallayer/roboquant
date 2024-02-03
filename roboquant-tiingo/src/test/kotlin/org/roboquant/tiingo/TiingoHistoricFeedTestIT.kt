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

package org.roboquant.tiingo

import org.roboquant.common.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TiingoHistoricFeedTestIT {


    @Test
    internal fun historic() {
        Config.getProperty("TEST_TIINGO") ?: return
        val feed = TiingoHistoricFeed()
        val tf = Timeframe.past(1.years)
        feed.retrieve("AAPL", "TSLA", "ERROR_SYMBOL", timeframe=tf)
        assertEquals(setOf("AAPL", "TSLA"), feed.assets.symbols.toSet())
        assertTrue(feed.timeline.size > 240)
    }

    @Test
    internal fun historicIntraDay() {
        Config.getProperty("TEST_TIINGO") ?: return
        val feed = TiingoHistoricFeed()
        val tf = Timeframe.past(10.days)
        feed.retrieveIntraday("AAPL", "TSLA", "ERROR_SYMBOL", timeframe=tf, frequency = "1hour")
        assertEquals(setOf("AAPL", "TSLA"), feed.assets.symbols.toSet())
        assertTrue(feed.timeline.size > 35)
    }




}
