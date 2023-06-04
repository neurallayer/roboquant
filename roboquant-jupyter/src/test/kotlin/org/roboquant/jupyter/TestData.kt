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

package org.roboquant.jupyter

import org.roboquant.Roboquant
import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Size
import org.roboquant.common.USD
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.TestStrategy

/**
 * Test data used in unit tests
 */
object TestData {

    val fullAccount by lazy {
        val feed = RandomWalkFeed.lastYears()
        val rq = Roboquant(TestStrategy(), logger = SilentLogger())
        rq.run(feed)
        rq.broker.account
    }

    fun usAccount(): Account {
        val amount: Amount = 100_000.USD
        val asset1 = Asset("AAA")
        val asset2 = Asset("AAB")
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
        val rq = Roboquant(TestStrategy(), AccountMetric(), logger = MemoryLogger(false))
        rq.run(feed, name = "run1")
        rq.run(feed, name = "run2")
        rq.logger.getMetric("account.equity")
    }

    fun loadFile(name: String): String {
        val classloader = Thread.currentThread().contextClassLoader
        val bytes = classloader.getResourceAsStream(name)!!.readAllBytes()
        return String(bytes)
    }

}