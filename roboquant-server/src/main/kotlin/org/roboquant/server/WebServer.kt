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

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.common.Config
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set


// Store all the instantiated runs
internal val runs = ConcurrentHashMap<String, RunInfo>()

/**
 * Configuration for the webserver
 *
 * @property username the username in case of authentication, default is empty (so no authentication)
 * @property password the password in case of authentication, default is empty (so no authentication)
 * @property port the port, default is 8080
 * @property host the host, default is 127.0.0.1
 */
data class WebServerConfig(
    var username: String = Config.getProperty("server.username", ""),
    var password: String = Config.getProperty("server.password", ""),
    var port: Int = Config.getProperty("server.port", 8080),
    var host: String = Config.getProperty("server.host", "127.0.0.1")
)

/**
 * Create a server, optional with credentials.
 * The website will be protected using digest authentication if username and password are provided.
 */
class WebServer(configure: WebServerConfig.() -> Unit = {}) {

    private var runCounter = 0
    private val server: NettyApplicationEngine

    init {
        val config = WebServerConfig()
        config.configure()

        with(config) {

            server = embeddedServer(
                Netty,
                port = port,
                host = host,
                module = {
                    if (username.isNotBlank() && password.isNotBlank()) secureSetup(username, password) else setup()
                }
            ).start(wait = false)
        }
    }

    /**
     * Stop the web server and close all feeds
     */
    fun stop() {
        logger.info { "stopping web server" }
        server.stop()

        // Close the feeds
        runs.forEach {
            logger.info { "closing feed run=${it.key} " }
            it.value.feed.close()
        }
    }

    @Synchronized
    internal fun getRunName(): String {
        return "run-${runCounter++}"
    }

    /**
     * Start a new run and make core metrics available to the webserver. You can start multiple runs in the same
     * webserver instance. Each run will have its unique name.
     */
    fun run(
        roboquant: Roboquant,
        feed: Feed,
        timeframe: Timeframe,
        name: String = getRunName(),
        warmup: TimeSpan = TimeSpan.ZERO,
        paused: Boolean = false
    ) = runBlocking {
        runAsync(roboquant, feed, timeframe, name, warmup, paused)
    }

    /**
     * Start a new run and make core metrics available to the webserver. You can start multiple runs in the same
     * webserver instance. Each run will have its unique name.
     */
    suspend fun runAsync(
        roboquant: Roboquant,
        feed: Feed,
        timeframe: Timeframe,
        name: String = getRunName(),
        warmup: TimeSpan = TimeSpan.ZERO,
        paused: Boolean = false
    ) {
        require(!runs.contains(name)) { "run name has to be unique, name=$name is already in use by another run." }
        val policy = PausablePolicy(roboquant.policy, paused)
        val rq = roboquant.copy(policy = policy)
        val info = RunInfo(rq, feed, timeframe, warmup)
        runs[name] = info
        logger.info { "Starting new run name=$name timeframe=$timeframe" }
        rq.runAsync(feed, timeframe, name, warmup)
        info.done = true
    }

}

