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

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.feeds.Event
import org.roboquant.metrics.Metric

/**
 * Metric used to capture basic information of a run that is displayed on the web pages.
 */
class WebMetric : Metric {

    var account: Account? = null
    var events = 0
    private var actions = 0

    override fun calculate(account: Account, event: Event): Map<String, Double> {
        this.account = account
        events++
        actions += event.actions.size
        return emptyMap()
    }

    fun  getCash(): List<List<Any>> {
        val acc = account ?: return emptyList()
        val result = listOf(listOf("currency", "amount"))
        return result + acc.cash.toMap().map { listOf(it.key.toString(), Amount(it.key, it.value).formatValue()) }
    }


    fun getsummary(): List<List<Any>> {
        val acc = account ?: return emptyList()
        return listOf(
            listOf("name", "value"),
            listOf("last update", acc.lastUpdate),
            listOf("events", events),
            listOf("actions", actions),
            listOf("buying power", acc.buyingPower),
            listOf("equity", acc.equity),
            listOf("open positions", acc.positions.size),
            listOf("open orders", acc.openOrders.size),
            listOf("closed orders", acc.closedOrders.size),
            listOf("trades", acc.trades.size),
        )
    }


}
