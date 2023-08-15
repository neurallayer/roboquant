/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.server

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.Roboquant
import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test

class WebServerTest {

    @Test
    fun basic() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = RandomWalkFeed(Timeframe.fromYears(2000, 2001))
        val rq = Roboquant(EMAStrategy(), logger = MemoryLogger(false))

        assertDoesNotThrow {
            runBlocking {
                val ws = WebServer(port=8081)
                ws.runAsync(rq, feed, feed.timeframe)
                ws.stop()
            }
        }
    }

}