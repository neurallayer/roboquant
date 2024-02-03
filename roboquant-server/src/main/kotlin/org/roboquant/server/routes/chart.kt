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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.icepear.echarts.components.toolbox.Toolbox
import org.roboquant.charts.TimeSeriesChart
import org.roboquant.server.runs


internal fun Route.chart() {
    post("/echarts") {
        val parameters = call.receiveParameters()
        val metric = parameters.getOrFail("metric")
        val run = parameters.getOrFail("run")
        val logger = runs.getValue(run).roboquant.logger
        val data = logger.getMetric(metric, run)
        val chart = TimeSeriesChart(data)
        chart.title = metric

        // Remove the restore option from the toolbox, since it doesn't work nicely with incremental updates.
        chart.customize = {
            (toolbox as? Toolbox)?.feature?.remove("restore")
        }

        val json = chart.renderJson()
        call.respondText(json, ContentType.Application.Json)
    }
}


