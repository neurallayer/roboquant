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
import com.sun.net.httpserver.HttpHandler
import java.io.OutputStream


abstract class BaseHttpHandler : HttpHandler {

    protected val template = """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>roboquant runs</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM" crossorigin="anonymous">
            <script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>
          </head>
          <body>
            <div class=container>
            %s
            </div>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz" crossorigin="anonymous"></script>
          </body>
        </html>
        """.trimIndent()

    override fun handle(exchange: HttpExchange) {
        val response = getContent(exchange).encodeToByteArray()
        exchange.sendResponseHeaders(200, response.size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(response)
        os.close()
    }

    fun queryToMap(query: String?): Map<String, String> {
        if (query.isNullOrBlank()) return emptyMap()

        val result: MutableMap<String, String> = HashMap()
        for (param in query.split('&')) {
            val entry = param.split('=')
            if (entry.size > 1) {
                result[entry[0]] = entry[1]
            } else {
                result[entry[0]] = ""
            }
        }
        return result
    }

    abstract fun getContent(exchange: HttpExchange): String

}


internal class OverviewHandler(private val runs: Map<String, WebServer.RunInfo>) : BaseHttpHandler() {

    override fun getContent(exchange: HttpExchange): String {
        val params = queryToMap(exchange.requestURI.query)
        if (params.isNotEmpty()) {
            if (params.containsKey("pause")) pause(params.getValue("pause"))
            if (params.containsKey("resume")) resume(params.getValue("resume"))
        }

        val result = StringBuilder()
        result.append(
            """
            <table class=table><tr><th>Run</th><th>Timeframe</th><th>State</th><th>Orders</th><th>Last Update</th><th>Actions</th></tr>
        """.trimIndent()
        )
        for ((run, info) in runs) {
            val account = info.metric.account
            val orders = if (account != null) account.closedOrders.size + account.openOrders.size else 0
            val paused = (info.roboquant.policy as PausablePolicy).pause
            val state = if (paused) "paused" else "running"

            result.append("<tr>")
            result.append("<td>$run</td>")
            result.append("<td>${info.timeframe}</td>")
            result.append("<td>$state</td>")
            result.append("<td>$orders</td>")
            result.append("<td>${account?.lastUpdate}</td>")
            result.append(
                """
                <td>
                    <a href='/runs/$run'>Details</a>
                    <a href='/?pause=$run'>Pause</a>
                    <a href='/?resume=$run'>Resume</a>
                </td>"""
            )
            result.append("</tr>")
        }
        result.append("</table>")
        return String.format(template, result.toString())
    }

    private fun pause(run: String) {
        val info = runs.getValue(run)
        val policy = info.roboquant.policy as PausablePolicy
        policy.pause = true
    }

    private fun resume(run: String) {
        val info = runs.getValue(run)
        val policy = info.roboquant.policy as PausablePolicy
        policy.pause = false
    }

}