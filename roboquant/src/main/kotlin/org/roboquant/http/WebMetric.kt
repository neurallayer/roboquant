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

package org.roboquant.http

import org.roboquant.brokers.Account
import org.roboquant.brokers.lines
import org.roboquant.feeds.Event
import org.roboquant.metrics.Metric
import org.roboquant.orders.lines

/**
 * Metric used to capture basic information of a run that is displayed on the web pages.
 */
internal class WebMetric : Metric {

    var account: Account? = null
    var events = 0
    var actions = 0

    override fun calculate(account: Account, event: Event): Map<String, Double> {
        this.account = account
        events++
        actions += event.actions.size
        return emptyMap()
    }


    private fun List<List<Any>>.toTable(): String {
        val result = StringBuilder("<table class=table>")
        result.append("<tr>")

        result.append("</tr>")
        var c = "th"
        for (row in this) {
            result.append("<tr>")
            for (column in row) {
                result.append("<$c>$column</$c>")
            }
            result.append("</tr>")
            c = "td"
        }
        result.append("</table>")
        return result.toString()
    }


    override fun toString(): String {
        val acc = account ?: return "Not yet run"
        val summary = listOf(
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

        return """
                <div class=row>
                <h3>summary</h3>
                ${summary.toTable()}
                </div>
            
                <div class=row>
                <h3>cash</h3>
                ${acc.cash}
                </div>
                
                <div class=row>
                <h3>positions</h3> 
                ${acc.positions.lines().toTable()}
                </div>
                
                <div class=row>
                <h3>open orders</h3>
                ${acc.openOrders.lines().toTable()}
                </div>
                
                <div class=row>
                <h3>closed orders</h3>
                ${acc.closedOrders.lines().toTable()}
                </div>
                
                <div class=row>
                <h3>trades</h3>
                ${acc.trades.lines().toTable()} 
                </div>                               
            """.trimIndent()
    }

}
