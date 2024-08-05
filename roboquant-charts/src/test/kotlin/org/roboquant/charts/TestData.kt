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

package org.roboquant.charts

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.journals.MemoryJournal
import org.roboquant.metrics.AccountMetric
import org.roboquant.orders.MarketOrder
import org.roboquant.run
import org.roboquant.strategies.EMACrossover

/**
 * Remove end-of-line characters so test results are the same on different operating-systems.
 */
@Suppress("unused")
internal fun String.removeEOL() = this.replace("\n", "").replace("\r", "")

/**
 * Test data used in unit tests
 */
object TestData {

    val fullAccount by lazy {
        val feed = RandomWalk.lastYears()
        val account = run(feed, EMACrossover())
        account
    }

    fun usAccount(): Account {
        val amount: Amount = 100_000.USD
        val asset1 = USStock("AAA")
        val asset2 = USStock("AAB")
        val account = InternalAccount(amount.currency)
        account.cash.deposit(amount)
        account.buyingPower = amount
        account.setPosition(Position(asset1, Size(100), 10.0))
        account.setPosition(Position(asset2, Size(100), 10.0))
        account.initializeOrders(listOf(MarketOrder(asset1, 100)))
        return account.toAccount()
    }


    val data by lazy {
        val feed = HistoricTestFeed(50..150)
        val journal = MemoryJournal(AccountMetric())
        run(feed, EMACrossover(), journal = journal)
        journal.getMetric("account.equity")
    }

    /*
    private fun loadFile(name: String): String {
        val classloader = Thread.currentThread().contextClassLoader
        val bytes = classloader.getResourceAsStream(name)!!.readAllBytes()
        return String(bytes)
    }


    fun testFile(chart: Chart, baseName: String) {
        val classloader = Thread.currentThread().contextClassLoader
        val fileName = "$baseName.json"
        val url = classloader.getResource(fileName)
        if (url === null) {
            val fullName = "src/test/resources/$fileName"
            val json = chart.renderJson()
            File(fullName).writeText(json)
        }

        val str = loadFile(fileName)
        assertEquals(str.removeEOL(), chart.renderJson().removeEOL())
    }
    */

}
