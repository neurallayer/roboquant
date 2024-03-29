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

package org.roboquant.ta

import org.roboquant.run
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.strategies.NoSignalStrategy
import kotlin.test.Test
import kotlin.test.assertTrue

internal class BettingAgainstBetaPolicyTest {

    @Test
    fun test() {
        val feed = RandomWalkFeed.lastYears(nAssets = 20)
        val assets = feed.assets.toList()
        val marketAsset = assets.first()

        val policy = BettingAgainstBetaPolicy(assets, marketAsset, maxPositions = 6)
        val broker = SimBroker(accountModel = MarginAccount())
        val account = run(
            feed,
            NoSignalStrategy(),
            broker = broker,
            policy = policy,
        )

        assertTrue(account.positions.size <= 6)
        assertTrue(account.positions.size > 3)


    }

}
