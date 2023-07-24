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

import com.sun.net.httpserver.BasicAuthenticator
import com.sun.net.httpserver.HttpServer
import org.roboquant.Roboquant
import org.roboquant.brokers.Account
import org.roboquant.brokers.lines
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.feeds.Feed
import org.roboquant.metrics.Metric
import org.roboquant.orders.lines
import java.io.OutputStream
import java.net.InetSocketAddress


private class WebMetric : Metric {

    var account: Account? = null
    var events = 0
    var actions = 0

    override fun calculate(account: Account, event: Event): Map<String, Double> {
        this.account = account
        events++
        actions += event.actions.size
        return emptyMap()
    }


    fun List<List<Any>>.toTable(): String {
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
        return """
                <div>
                    <h3>summary</h3>
                    <p>events: $events</p>
                    <p>actions: $actions</p>
                    <p>buying power: ${acc.buyingPower}</p>
                    <p>equity: ${acc.equity}</p>
                
                    <h3>cash</h3>
                    ${acc.cash}
                    
                    <h3>positions</h3> 
                    ${acc.positions.lines().toTable()}
                    
                    <h3>open orders</h3>
                    ${acc.openOrders.lines().toTable()}
                    
                    <h3>closed orders</h3>
                    ${acc.closedOrders.lines().toTable()}
                    
                    <h3>trades</h3>
                    ${acc.trades.lines().toTable()}                   
                </div>
            """.trimIndent()
    }

}

internal class Authenticator(private val username: String, private val password: String) :
    BasicAuthenticator("roboquant") {
    override fun checkCredentials(username: String?, password: String?): Boolean {
        return (username == this.username && password == this.password)
    }

}

/**
 * Very lightweight webserver that enables to run a trading strategy and view some key metrics while it is running.
 * There is support for basic-auth, but this is not very secure since username & password are sent in plain-text
 * to the server.
 *
 * This server might be replaced in the future for a more secure solution.
 *
 * @param port The port the webserver should be running on
 */
class WebServer(port: Int = 8000, username: String = "", password: String = "") {

    private val runs = mutableMapOf<String, RunInfo>()
    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)
    private var run = 0
    private val authenticator: Authenticator? = if (username.isNotEmpty()) Authenticator(username, password) else null

    private data class RunInfo(
        val metric: WebMetric,
        val roboquant: Roboquant,
        val feed: Feed,
        val timeframe: Timeframe,
        val warmup: TimeSpan = TimeSpan.ZERO
    )


    private val template = """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>roboquant runs</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM" crossorigin="anonymous">
          </head>
          <body>
            %s
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz" crossorigin="anonymous"></script>
          </body>
        </html>
        """.trimIndent()

    private fun detailsRun(run: String): String {
        val result = StringBuilder()
        val info = runs.getValue(run)
        result.append("<h2>${run}</h2>")
        result.append(info.metric.toString())
        result.append("<br/><a href='/'>Back</a>".trimIndent())
        return String.format(template, result)
    }


    private fun overviewRuns(): String {
        val result = StringBuilder()
        result.append(
            """
            <table class=table><tr><th>Run</th><th>Timeframe</th><th>Orders</th><th>Last Update</th><th>Actions</th></tr>
        """.trimIndent()
        )
        for ((run, info) in runs) {
            val account = info.metric.account
            val orders = if (account != null) account.closedOrders.size + account.openOrders.size else 0

            result.append("<tr>")
            result.append("<td>$run</td>")
            result.append("<td>${info.timeframe}</td>")
            result.append("<td>$orders</td>")
            result.append("<td>${account?.lastUpdate}</td>")
            result.append("<td><a href='/runs/$run'>Details</a></td>")
            result.append("</tr>")
        }
        result.append("</table>")
        return String.format(template, result)
    }

    /**
     * Start the webserver
     */
    fun start() {
        server.executor = null
        val ctx = server.createContext("/") {
            val path = it.requestURI.path
            val content = when {
                path.startsWith("/runs/") -> detailsRun(path.split('/').last())
                else -> overviewRuns()
            }
            println(it.requestURI.path)
            val response = content.encodeToByteArray()
            it.sendResponseHeaders(200, response.size.toLong())
            val os: OutputStream = it.responseBody
            os.write(response)
            os.close()
        }

        if (authenticator != null) ctx.setAuthenticator(authenticator)

        server.start()
    }

    /**
     * Stop the server, optionally provide a [delay] in seconds
     */
    fun stop(delay: Int = 0) {
        server.stop(delay)
    }

    /**
     * Start a new run and make core metrics available to the webserver. You can start multiple runs in the same
     * webserver. Each run will have a unique sequential name.
     */
    suspend fun runAsync(roboquant: Roboquant, feed: Feed, timeframe: Timeframe, warmup: TimeSpan = TimeSpan.ZERO) {
        val metric = WebMetric()
        val run = "run-${run++}"

        val rq = roboquant.copy(metrics = roboquant.metrics + metric)
        runs[run] = RunInfo(metric, rq, feed, timeframe, warmup)
        rq.runAsync(feed, timeframe, warmup, run)
    }


}