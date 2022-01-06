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

package org.roboquant.metrics

import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.MemoryLogger
import org.roboquant.strategies.EMACrossover
import org.junit.Test
import kotlin.test.assertTrue

internal class AccountBetaTest {

    @Test
    fun test() {
        val feed = RandomWalk.lastYears(10, 20)
        val marketAsset = feed.assets.first()
        val strategy = EMACrossover.shortTerm()
        val accountBetaMetric = AccountBeta(marketAsset, 50)
        val logger = MemoryLogger(false)
        val roboquant = Roboquant(strategy, accountBetaMetric, logger = logger)
        roboquant.run(feed)
        val beta = logger.getMetric("account.beta").last().value

        assertTrue(!beta.isNaN())
    }

}