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
import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.brokers.Account
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.feeds.Feed
import org.roboquant.orders.Order
import org.roboquant.policies.Policy
import org.roboquant.strategies.Signal
import java.io.OutputStream
import java.net.InetSocketAddress

/**
 * Allows a policy to be paused, aka don't generate orders.
 */
private class PausablePolicy(val policy: Policy, var pause: Boolean = false) : Policy by policy {

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        // Still invoke the policy so any state can be updated if required.
        val orders = policy.act(signals, account, event)
        return if (pause) emptyList() else orders
    }

}

internal class RoboquantAuthenticator(private val username: String, private val password: String) :
    BasicAuthenticator("roboquant") {
    override fun checkCredentials(username: String?, password: String?): Boolean {
        return (username == this.username && password == this.password)
    }

}

/*
internal class Authenticator2(private val username: String, private val password: String) : Authenticator() {


    private fun ByteArray.toHex() : String {
        val sb = java.lang.StringBuilder()
        for (b in this) sb.append(String.format("%02X ", b))
        return sb.toString().uppercase()
    }

    private fun getOnce() : String {
        val md = MessageDigest.getInstance("MD5")
        val uuid = UUID.randomUUID().toString().encodeToByteArray()
        return md.digest(uuid).toHex()
    }

    override fun authenticate(exchange: HttpExchange): Result {
        val auth = exchange.requestHeaders["Authorization"]
        if (auth.isNullOrEmpty()) {
            exchange.responseHeaders["WWW-Authenticate"] = """Digest realm="roboquant"""""
            return Retry(401)
        }
        return Success(HttpPrincipal(username, "roboquant"))
    }


}
*/


/**
 * Very lightweight webserver that enables you to run a trading strategy and view some key metrics while it is running.
 * There is support for basic-auth, but this is not very secure since username & password are sent in
 * plain-text to the server.
 *
 * Example:
 *
 * ```
 * val ws = WebServer()
 * ws.start()
 * ws.runAsync(roboquant, feed, Timeframe.next(8.hours))
 * ```
 *
 * This server might be replaced in the future for a more secure solution.
 *
 * @param port the port the webserver should be running on
 * @param username the username to be used for basic authentication
 * @param password the password to be used for basic authentication
 */
class WebServer(port: Int = 8000, username: String, password: String) {

    /**
     * Create a web server without basic authentication
     */
    constructor(port: Int = 8000) : this(port, "", "")

    private val runs = mutableMapOf<String, RunInfo>()
    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)
    private var run = 0
    private val roboquantAuthenticator: RoboquantAuthenticator? =
        if (username.isNotEmpty()) RoboquantAuthenticator(username, password) else null

    private data class RunInfo(
        val metric: WebMetric,
        val roboquant: Roboquant,
        val feed: Feed,
        val timeframe: Timeframe,
        val warmup: TimeSpan = TimeSpan.ZERO
    )

    // basic bootstrap page
    private val template = """
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
                fetch("/runs/" + run + "/metrics/" + value).then(function(resp){
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


    private fun getMetricsCharts(run: String, info: RunInfo): String {
        val metricNames = info.roboquant.logger.metricNames
        val dropDown = metricNames.map { "<option value=$it>$it</option>" }
        val runStmt = """const run="$run";"""
        return String.format(metrics, dropDown, runStmt)
    }


    private fun detailsRun(run: String): String {
        val result = StringBuilder()
        val info = runs.getValue(run)
        result.append("<h1>${run}</h1>")
        result.append(info.metric.toString())
        result.append(getMetricsCharts(run, info))
        result.append("<br/><a href='/'>Back</a>")
        return String.format(template, result)
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


    private fun overviewRuns(query: String): String {
        println(query)
        if (query.isNotBlank()) {
            val (action, run) = query.split('=')
            when (action) {
                "pause" -> pause(run)
                "resume" -> resume(run)
            }
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
        return String.format(template, result)
    }

    /**
     * Start the webserver
     */
    fun start() {
        server.executor = null
        val ctx = server.createContext("/") {
            val path = it.requestURI.path
            val elems = path.split('/')
            val content = when {
                path.matches(Regex("/runs/.*/metrics/.*")) ->  metricsData(elems[4], elems[2])
                path.startsWith("/runs/") -> detailsRun(path.split('/').last())
                path.startsWith("/metrics/") -> metricsData(path.split('/').last(), "run-0")
                else -> overviewRuns(it.requestURI.query ?: "")
            }
            val response = content.encodeToByteArray()
            it.sendResponseHeaders(200, response.size.toLong())
            val os: OutputStream = it.responseBody
            os.write(response)
            os.close()
        }

        if (roboquantAuthenticator != null) ctx.setAuthenticator(roboquantAuthenticator)

        server.start()
    }

    private fun metricsData(metricName: String, run: String): String {
        println("$metricName $run")
        val data = StringBuilder("[")
        val info = runs.getValue(run)
        val metrics = info.roboquant.logger.getMetric(metricName, run)
        metrics.forEachIndexed { index, observation ->
            data.append("""{"time":${observation.time.epochSecond},"value":${observation.value}}""")
            if (index < (metrics.size -1)) data.append(",")
        }
        data.append("]")
        println(data.toString())
        return data.toString()
    }

    /**
     * Stop the server, optionally provide a [delay] in seconds. This will also close the feeds
     */
    fun stop(delay: Int = 0) {
        server.stop(delay)
        for (info in runs.values) try {
            info.feed.close()
        } catch (_: RuntimeException) {
        }
    }


    fun run(roboquant: Roboquant, feed: Feed, timeframe: Timeframe, warmup: TimeSpan = TimeSpan.ZERO) = runBlocking {
        runAsync(roboquant, feed, timeframe, warmup)
    }

    /**
     * Start a new run and make core metrics available to the webserver. You can start multiple runs in the same
     * webserver instance. Each run will have its unique name.
     */
    suspend fun runAsync(roboquant: Roboquant, feed: Feed, timeframe: Timeframe, warmup: TimeSpan = TimeSpan.ZERO) {
        val metric = WebMetric()
        val run = "run-${run++}"

        val rq = roboquant.copy(metrics = roboquant.metrics + metric, policy = PausablePolicy(roboquant.policy))
        runs[run] = RunInfo(metric, rq, feed, timeframe, warmup)
        rq.runAsync(feed, timeframe, run, warmup)
    }


}