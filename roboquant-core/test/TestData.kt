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

package org.roboquant

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import org.roboquant.metrics.MetricResults
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.io.File
import java.time.Instant

object TestData {

    fun usStock() = Asset("AAPL")

    fun usAccount() : Account {
        val asset1 = Asset("AAA")
        val asset2 = Asset("AAB")
        val account = Account()
        account.portfolio.setPosition(Position(asset1, 100.0, 10.0))
        account.portfolio.setPosition(Position(asset2, 100.0, 10.0))

        val order = MarketOrder(asset1, 100.0)
        order.placed = Instant.now()
        order.status = OrderStatus.COMPLETED
        account.orders.add(order)

        return account
    }

    fun euStock() = Asset("AF", currencyCode = "EUR", exchangeCode = "AEB")

    fun dataDir(): String {
        if (File("./data").isDirectory)
            return "./data/"
        else if (File("../data").isDirectory)
            return "../data/"
        throw Exception("cannot find data directory for testing")
    }


    fun euMarketOrder() = MarketOrder(euStock(), 10.0)

    fun usMarketOrder() = MarketOrder(usStock(), 10.0)

    private fun priceAction(asset: Asset = usStock()) = TradePrice(asset, 10.0)

    fun priceBar(asset: Asset = usStock()) = PriceBar(asset, 10.0, 11.0, 9.0, 10.0, 1000.0)

    fun time() = Instant.parse("2020-01-03T12:00:00Z")!!

    fun event(time: Instant = time()) = Event(listOf(priceAction()), time)

    fun event2(time: Instant = time()) = Event(listOf(priceAction(usStock()), priceAction(euStock())), time)

    fun metricInput(time: Instant = time()): Pair<Account, Event> {
        val account = Account()
        val moment = Event(listOf(priceAction()), time)
        return Pair(account, moment)
    }

    fun getMetrics(): MetricResults {
        return mapOf("key1" to 12.0, "key2" to 13.0)
    }

    fun getRunInfo(): RunInfo {
        return RunInfo("run-1", 1, 10, Instant.now(), TimeFrame.INFINITY, RunPhase.MAIN)
    }


}