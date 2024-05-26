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

package org.roboquant.server.routes

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.html.*
import org.roboquant.brokers.Account
import org.roboquant.server.*

private fun FlowContent.table(caption: String, list: List<List<Any>>) {
    table(classes = "table text-end my-5") {
        caption {
            +caption
        }
        thead {
            tr {
                for (header in list.first()) {
                    th { +header.toString() }
                }
            }
        }

        tbody {
            for (row in list.drop(1)) {
                tr {
                    for (c in row) {
                        td { +c.toString() }
                    }
                }
            }
        }
    }
}


private fun FlowContent.echarts(
    elemId: String,
    height: String = "800px",
    width: String = "100%",
    initialHidden: Boolean = true
) {
    div {
        id = elemId
        hxExt = "echarts"
        style = "width:$width;height:$height;"
        if (initialHidden) style += "display:None;"
    }
}

private fun FlowContent.metricForm(target: String, run: String, info: RunInfo) {
    val metricNames = info.journal.getMetricNames()
    form {
        hxPost = "/echarts"
        hxTarget = target
        select(classes = "form-select") {
            name = "metric"
            metricNames.forEach {
                option { value = it; +it }
            }
        }

        input(type = InputType.hidden, name = "run") { value = run }

        button(type = ButtonType.submit, classes = "mt-2 btn btn-primary") { +"Update Chart" }
    }
}


private fun getAccountSummary(acc: Account): List<List<Any>> {
    return listOf(
        listOf("name", "value"),
        listOf("last update", acc.lastUpdate),
        listOf("buying power", acc.buyingPower),
        listOf("cash", acc.cash),
        listOf("equity", acc.equity),
        listOf("open positions", acc.positions.size),
        listOf("open orders", acc.openOrders.size),
        listOf("closed orders", acc.closedOrders.size),
        listOf("trades", acc.trades.size),
    )
}

internal fun Route.details() {
    get("/run/{id}") {
        val id = call.parameters.getOrFail("id")
        val info = runs.getValue(id)
        val acc = info.roboquant.broker.sync()
        call.respondHtml(HttpStatusCode.OK) {
            page("Details $id") {
                a(href = "/") { +"Back to overview" }
                table("account summary", getAccountSummary(acc))
                div(classes = "row my-5") {
                    div(classes = "col-3") {
                        metricForm("#echarts-1", id, info)
                    }
                    div(classes = "col-9") {
                        echarts("echarts-1", height = "400px")
                    }
                }
                // table("cash", metric.getCash())
                // table("open positions", acc.positions.lines())
                // table("open orders", acc.openOrders.lines())
                // table("closed orders (last 10)", acc.closedOrders.lines().takeLastPlusHeader(10))
                // table("trades (last 10)", acc.trades.lines().takeLastPlusHeader(10))
            }
        }
    }
}
