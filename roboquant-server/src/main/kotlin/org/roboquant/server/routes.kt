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

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.html.*
import org.icepear.echarts.components.toolbox.Toolbox
import org.icepear.echarts.components.toolbox.ToolboxDataZoomFeature
import org.icepear.echarts.components.toolbox.ToolboxMagicTypeFeature
import org.icepear.echarts.components.toolbox.ToolboxSaveAsImageFeature
import org.roboquant.brokers.Account
import org.roboquant.brokers.lines
import org.roboquant.charts.TimeSeriesChart
import org.roboquant.charts.renderJson
import org.roboquant.orders.lines
import java.time.temporal.ChronoUnit

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

/**
 * Toolbox without the restore option
 */
private fun getToolbox(): Toolbox {
    val features = mutableMapOf(
        "saveAsImage" to ToolboxSaveAsImageFeature(),
        "dataZoom" to ToolboxDataZoomFeature().setYAxisIndex("none"),
        "magicType" to ToolboxMagicTypeFeature().setType(arrayOf("line", "bar")),
    )
    return Toolbox().setFeature(features)
}


fun Route.getChart() {
    post("/echarts") {
        val parameters = call.receiveParameters()
        val metric = parameters.getOrFail("metric")
        val run = parameters.getOrFail("run")
        val logger = runs.getValue(run).roboquant.logger
        val data = logger.getMetric(metric, run)
        val chart = TimeSeriesChart(data)
        chart.title = metric
        val option = chart.getOption().setToolbox(getToolbox())
        val json = option.renderJson()
        call.respondText(json)
    }
}

fun FlowContent.pauseInfo(pause: Boolean) {
    if (pause) p(classes = "text-warning") {
        +"paused"
    } else p(classes = "text-success") {
        +"running"
    }
}

@Suppress("LongMethod")
fun Route.listRuns() {
    get("/") {
        val params = call.request.queryParameters

        if (params.contains("action")) {
            val action = params.getOrFail("action")
            if (action == "stop") {
                // Always exit, so no triggering of shutdown sequence
                Runtime.getRuntime().halt(1)
            }

            val run = params["run"]!!
            val policy = runs.getValue(run).roboquant.policy as PausablePolicy
            when (action) {
                "pause" -> policy.pause = true
                "resume" -> policy.pause = false
            }
        }

        call.respondHtml(HttpStatusCode.OK) {
            page("Overview runs") {
                table(classes = "table text-end my-5") {
                    tr {
                        th { +"run name" }
                        th { +"state" }
                        th { +"timeframe"}
                        th { +"events" }
                        th { +"signals" }
                        th { +"orders" }
                        th { +"last update" }
                        th { +"actions" }
                    }
                    runs.forEach { (run, info) ->
                        val policy = info.roboquant.policy as PausablePolicy
                        tr {
                            td { +run }
                            td { pauseInfo(policy.pause) }
                            td { +info.timeframe.toPrettyString() }
                            td {
                                +"total = ${policy.totalEvents}"
                                br
                                +"empty = ${policy.emptyEvents}"
                                br
                                +"actions = ${policy.totalActions}"
                            }
                            td {
                                +"buy = ${policy.buySignals}"
                                br
                                +"sell = ${policy.sellSignals}"
                                br
                                +"hold = ${policy.holdSignals}"
                            }
                            td { +policy.totalOrders.toString() }
                            td { +policy.lastUpdate.truncatedTo(ChronoUnit.MILLIS).toString() }
                            td {
                                a(href = "/run/$run") { +"details" }
                                br
                                a(href = "/?action=pause&run=$run") { +"pause" }
                                br
                                a(href = "/?action=resume&run=$run") { +"resume" }
                            }
                        }
                    }

                }
                div(classes = "row justify-content-md-center mt-5") {
                    button(classes = "btn btn-danger btn-lg col-lg-2") {
                        hxGet = "/?action=stop"
                        hxConfirm = "Are you sure you want to stop the server?"
                        +"Stop Server"
                    }
                }

            }
        }
    }
}


private fun FlowContent.echarts(elemId: String, width: String = "100%", height: String = "800px") {
    div {
        id = elemId
        hxExt = "echarts"
        style = "width:$width;height:$height;"
    }
}

private fun FlowContent.metricForm(target: String, run: String, info: RunInfo) {
    val metricNames = info.roboquant.logger.metricNames
    form {
        hxPost = "/echarts"
        hxTarget = target
        select(classes = "form-select") {
            name = "metric"
            metricNames.forEach {
                option { value = it; +it }
            }
        }

        input(type = InputType.hidden, name = "run") { value=run }

        button(type = ButtonType.submit, classes = "mt-2 btn btn-primary") { +"Update Chart" }
    }
}


private fun List<List<Any>>.takeLastPlusHeader(n: Int): List<List<Any>> {
    return listOf(first()) + drop(1).takeLast(n)
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

fun Route.getRun() {
    get("/run/{id}") {
        val id = call.parameters.getOrFail("id")
        val info = runs.getValue(id)
        val acc = info.roboquant.broker.account
        call.respondHtml(HttpStatusCode.OK) {
            page("Details $id") {
                a(href = "/") { +"Back to overview" }
                table("account summary", getAccountSummary(acc))
                div(classes = "row my-5") {
                    div(classes = "col-2 pe-0") {
                        metricForm("#echarts123456", id, info)
                    }
                    div(classes = "col-10") {
                        echarts("echarts123456", height = "400px")
                    }
                }
                // table("cash", metric.getCash())
                table("open positions", acc.positions.lines())
                table("open orders", acc.openOrders.lines())
                table("closed orders (last 10)", acc.closedOrders.lines().takeLastPlusHeader(10))
                table("trades (last 10)", acc.trades.lines().takeLastPlusHeader(10))
            }
        }
    }
}
