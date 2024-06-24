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

package org.roboquant.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.roboquant.brokers.Broker
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.journals.MemoryJournal
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.Policy
import org.roboquant.strategies.Strategy
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
    private val server: EmbeddedServer<*, *>

    /**
     * Returns true if this webserver is secured with credentials, false otherwise
     */
    val secured: Boolean

    init {
        val config = WebServerConfig()
        config.configure()
        secured = config.username.isNotBlank() && config.password.isNotBlank()

        with(config) {

            server = embeddedServer(
                Netty,
                port = port,
                host = host,
                module = {
                    if (secured) secureSetup(username, password) else setup()
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
    }

    @Synchronized
    internal fun getRunName(): String {
        return "run-${runCounter++}"
    }

    suspend fun addRun(
        name: String = getRunName(),
        feed: Feed,
        strategy: Strategy,
        journal: MemoryJournal = MemoryJournal(),
        timeframe: Timeframe = Timeframe.INFINITE,
        policy: Policy = FlexPolicy(),
        broker: Broker = SimBroker(),
        channel: EventChannel = EventChannel(timeframe, 10),
        timeOutMillis: Long = -1
    ) {
        require(!runs.contains(name)) { "run name has to be unique, name=$name is already in use by another run." }
        val pausubalePolicy = PausablePolicy(policy)
        val info = RunInfo(journal, timeframe, pausubalePolicy, broker)
        runs[name] = info
        logger.info { "Starting new run name=$name timeframe=$timeframe" }
        org.roboquant.runAsync(feed, strategy, journal, timeframe, pausubalePolicy, broker, channel, timeOutMillis)
    }

}

