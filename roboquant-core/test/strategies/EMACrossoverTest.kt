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

import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.junit.Test
import kotlin.test.assertTrue

internal class EMACrossoverTest {

    @Test
    fun simple() = runBlocking {
        val feed = RandomWalk.lastYears()

        val strategy = EMACrossover()

        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = SilentLogger() )
        roboquant.run( feed)
    }

    @Test
    fun simple2()  {
        val strategy1 = EMACrossover.shortTerm()
        val strategy2 = EMACrossover.longTerm()
        assertTrue(strategy1.fast < strategy2.fast)
        assertTrue(strategy1.slow < strategy2.slow)
    }

}