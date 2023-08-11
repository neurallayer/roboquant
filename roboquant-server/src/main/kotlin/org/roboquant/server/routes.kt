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
import io.ktor.server.routing.*
import kotlinx.html.*

/*
val blocks = mutableMapOf<String, FlowContent.() -> Unit>()


fun FlowContent.serverside(id:String, block: FlowContent.() -> Unit) {
    blocks[id] = block
}


fun Application.routes() {
    routing {
        get("/serverside/{id}") {
            val id = call.parameters["id"]!!
            val block = blocks[id]!!

            call.respond(HttpStatusCode.OK) {
                createHTML().article {
                    this.block()
                }
            }

        }
    }
}

fun FlowContent.test() {
    val x = 12
    a(href = "/serverside/123") {
        hxBoost(true)
        serverside("123") {
           p {
               +"test $x"
           }
       }
    }

}
*/

private fun FlowContent.table(caption: String, list: List<List<Any>>)  {
    table(classes = "table text-end") {
        caption {
            +caption
        }
        tr {
            for (header in list.first()) {
                th { +header.toString() }
            }
        }

        for (row in list.drop(1)) {
            tr {
                for (c in row) {
                    td { +c.toString() }
                }
            }
        }
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
                        th { +"events"}
                        th { +"last update"}
                        th { +"actions"}
                    }
                    runs.forEach { (run, info) ->
                        val acc = info.metric.account!!
                        val policy = info.roboquant.policy as PausablePolicy
                        val state = if (policy.pause) "pause" else "running"
                        tr {
                            td {+run}
                            td {+state}
                            td {+info.metric.events.toString()}
                            td {+acc.lastUpdate.toString()}
                            td {
                                a(href = "/run/$run") {
                                    +"details"
                                }
                                br{}
                                a(href = "/?action=pause&run=$run") {
                                    +"pause"
                                }
                                br{}
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


fun Route.getRun() {
    get("/run/{id}") {
        val id = call.parameters["id"] ?: ""
        val info = runs.getValue(id)
        val metric = info.metric
        call.respondHtml(HttpStatusCode.OK) {
            page("Details $id") {
                a(href="/") {
                    +"Back to overview"
                }
                table("account summary",metric.getsummary())
                table("cash", metric.getCash())
                table("open positions", metric.getPositions())
                table("open orders", metric.getOpenOrders())
                table("closed orders", metric.getClosedOrders())
                table("trades", metric.getTrades())
            }
        }
    }
}
