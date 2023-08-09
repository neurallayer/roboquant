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

import com.sun.net.httpserver.HttpExchange

internal class DetailHandler(private val runs: Map<String, WebServer.RunInfo>) : BaseHttpHandler() {

    private val metrics = """
        <select id=metricnames>
            <option value="">None</option>
        %s
        </select>
        <div id="metricschart"></div>
        <script>
            %s
            
            document.getElementById("metricnames").onchange = updateMetricChart;
            const elem = document.getElementById("metricschart")    
            const chart = LightweightCharts.createChart(elem, {
                height: 300,
                autoSize: true
            });
            const lineSeries = chart.addLineSeries();
            function updateMetricChart(dropDown) {
                const value = document.getElementById("metricnames").value;
                if (value == "") return;
                fetch("/metrics/" + run + "/" + value).then(function(resp){
                    console.log(resp);
                    return resp.json();
                }).then(function(data) {
                    console.log(data);
                    lineSeries.setData(data);
                });
                console.log(value);
            };
        </script>
    """.trimIndent()

    override fun getContent(exchange: HttpExchange): String {
        val run = exchange.requestURI.path.split('/').last()
        // val params = queryToMap(exchange.requestURI.query)
        // val run = params.getValue("run")

        val result = StringBuilder()
        val info = runs.getValue(run)
        result.append("<h1>${run}</h1>")
        result.append(info.metric.toString())
        result.append(getMetricsCharts(run, info))
        result.append("<br/><a href='/'>Back</a>")
        return String.format(template, result.toString())
    }

    private fun getMetricsCharts(run: String, info: WebServer.RunInfo): String {
        val metricNames = info.roboquant.logger.metricNames
        val dropDown = metricNames.map { "<option value=$it>$it</option>" }
        val runStmt = """const run="$run";"""
        return String.format(metrics, dropDown, runStmt)
    }



}