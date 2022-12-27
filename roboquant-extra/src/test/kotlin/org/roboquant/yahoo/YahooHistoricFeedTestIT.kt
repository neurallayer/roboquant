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

package org.roboquant.yahoo

import org.junit.jupiter.api.Test
import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.common.symbols
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class YahooHistoricFeedTestIT {


    @Test
    fun test() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = YahooHistoricFeed()
        val timeframe = Timeframe.fromYears(2019, 2020)
        feed.retrieve("AAPL", "IBM", timeframe = timeframe)
        assertContains(feed.assets.symbols, "AAPL")
        assertTrue(feed.timeframe.start >= timeframe.start)
        assertTrue(feed.timeframe.end <= timeframe.end)
        assertEquals(252, feed.timeline.size)
    }

}