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

package org.roboquant.charts

import org.roboquant.common.Account
import org.roboquant.common.Position
import org.roboquant.common.*
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.journals.MemoryJournal
import org.roboquant.journals.metrics.AccountMetric
import org.roboquant.run
import org.roboquant.strategies.EMACrossover
import java.time.Instant

/**
 * Remove end-of-line characters so test results are the same on different operating-systems.
 */
@Suppress("unused")
internal fun String.removeEOL() = this.replace("\n", "").replace("\r", "")

/**
 * Test data used in unit tests
 */
object TestData {


    fun usAccount(): Account {
        val amount: Amount = 100_000.USD
        val asset1 = Stock("AAA")
        val asset2 = Stock("AAB")

        val p1 = Position(asset1, Size(100), 10.0)
        val p2 = Position(asset2, Size(100), 10.0)

        val order = Order(asset1, Size(100), 10.0)
        return Account(
            buyingPower = amount,
            cash = amount.toWallet(),
            positions = listOf(p1, p2),
            orders = listOf(order),
            trades = listOf(),
            lastUpdate = Instant.now(),
        )
    }


    val timeSeriesData by lazy {
        val feed = RandomWalk(Timeframe.fromYears(2020, 2021))
        val journal = MemoryJournal(AccountMetric())
        run(feed, EMACrossover(), journal = journal)
        journal.getMetric("account.equity")
    }

}
