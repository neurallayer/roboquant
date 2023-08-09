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
import java.net.InetSocketAddress

/**
 * Allows a policy to be paused, aka don't generate orders.
 */
internal class PausablePolicy(val policy: Policy, var pause: Boolean = false) : Policy by policy {

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

    internal data class RunInfo(
        val metric: WebMetric,
        val roboquant: Roboquant,
        val feed: Feed,
        val timeframe: Timeframe,
        val warmup: TimeSpan = TimeSpan.ZERO
    )



    /**
     * Start the webserver
     */
    fun start() {
        server.executor = null
        val ctx = server.createContext("/", OverviewHandler(runs))
        val ctx2 = server.createContext("/runs", DetailHandler(runs))
        val ctx3 = server.createContext("/metrics", MetricHandler(runs))

        if (roboquantAuthenticator != null) {
            ctx.setAuthenticator(roboquantAuthenticator)
            ctx2.setAuthenticator(roboquantAuthenticator)
            ctx3.setAuthenticator(roboquantAuthenticator)
        }

        server.start()
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