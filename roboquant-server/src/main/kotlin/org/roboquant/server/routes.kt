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
import org.roboquant.brokers.lines
import org.roboquant.charts.TimeSeriesChart
import org.roboquant.charts.renderJson
import org.roboquant.orders.lines

private fun FlowContent.table(caption: String, list: List<List<Any>>) {
    table(classes = "table text-end my-4") {
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


fun Route.getChart() {
    post("/echarts") {
        val parameters = call.receiveParameters()
        val metric = parameters.getOrFail("metric")
        val run = parameters.getOrFail("run")
        val logger = runs.getValue(run).roboquant.logger
        val data = logger.getMetric(metric)
        val chart = TimeSeriesChart(data)
        chart.title = metric
        val json = chart.getOption().renderJson()
        call.respondText(json)
    }
}

fun Route.listRuns() {
    get("/") {
        val params = call.request.queryParameters


        if (params.contains("action")) {
            val run = params["run"]!!
            val action = params["action"]!!
            val policy = runs.getValue(run).roboquant.policy as PausablePolicy
            when (action) {
                "pause" -> policy.pause = true
                "resume" -> policy.pause = false
            }
        }

        call.respondHtml(HttpStatusCode.OK) {
            page("Overview runs") {
                table(classes = "table text-end") {
                    tr {
                        th { +"run name" }
                        th { +"state" }
                        th { +"events" }
                        th { +"last update" }
                        th { +"actions" }
                    }
                    runs.forEach { (run, info) ->
                        val acc = info.metric.account!!
                        val policy = info.roboquant.policy as PausablePolicy
                        val state = if (policy.pause) "pause" else "running"
                        tr {
                            td { +run }
                            td { +state }
                            td { +info.metric.events.toString() }
                            td { +acc.lastUpdate.toString() }
                            td {
                                a(href = "/run/$run") {
                                    +"details"
                                }
                                br {}
                                a(href = "/?action=pause&run=$run") {
                                    +"pause"
                                }
                                br {}
                                a(href = "/?action=resume&run=$run") {
                                    +"resume"
                                }
                            }
                        }
                    }

                }

            }
        }
    }
}


private fun FlowContent.echarts(id: String, width: String = "100%", height: String = "800px") {
    div {
        attributes["id"] = id
        attributes["hx-ext"] = "echarts"
        style = "width:$width;height:$height;"
    }
    script(type = ScriptType.textJavaScript) {
        unsafe {
            raw(
                """
                (function() {    
                    let elem = document.getElementById('$id')
                    let chart = echarts.init(elem);
                    let resizeObserver = new ResizeObserver(() => chart.resize());
                    resizeObserver.observe(elem);
                    elem.style.setProperty('display', 'none');
                })();
                """.trimIndent()
            )
        }
    }
}

fun FlowContent.metricForm(target: String, run: String, metricNames: List<String>) {
    form {
        hxPost("/echarts")
        hxTarget(target)
        select(classes = "form-select") {
            name = "metric"
            metricNames.forEach {
                option { value = it; +it }
            }
        }

        input(type = InputType.hidden, name = "run") { value=run }

        button(type = ButtonType.submit, classes = "btn btn-primary") {
            +"Get Chart"

        }
    }
}


fun List<List<Any>>.takeLastPlusHeader(n: Int): List<List<Any>> {
    return listOf(first()) + drop(1).takeLast(n)
}

fun Route.getRun() {
    get("/run/{id}") {
        val id = call.parameters["id"] ?: ""
        val info = runs.getValue(id)
        val metric = info.metric
        val metricNames = info.roboquant.logger.metricNames
        val acc = info.metric.account!!
        call.respondHtml(HttpStatusCode.OK) {
            page("Details $id") {
                a(href = "/") { +"Back to overview" }
                table("account summary", metric.getsummary())
                div(classes = "row my-4") {
                    div(classes = "col-2") {
                        metricForm("#echarts123456", id, metricNames)
                    }
                    div(classes = "col-10") {
                        echarts("echarts123456", height = "400px")
                    }
                }
                table("cash", metric.getCash())
                table("open positions", acc.positions.lines())
                table("open orders", acc.openOrders.lines())
                table("closed orders", acc.closedOrders.lines().takeLastPlusHeader(10))
                table("trades", acc.trades.lines().takeLastPlusHeader(10))
            }
        }
    }
}
