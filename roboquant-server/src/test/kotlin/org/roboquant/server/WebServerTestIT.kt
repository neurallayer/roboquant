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

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.Roboquant
import org.roboquant.common.Timeframe
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.strategies.EMAStrategy
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class WebServerTestIT {


    private fun makeRequest(url: String): Int {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        val response = client.send(
            request,
            HttpResponse.BodyHandlers.discarding()
        )

        return response.statusCode()
    }

    @Test
    fun basic() {
        // System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        val feed = RandomWalkFeed(Timeframe.fromYears(2000, 2001))
        val rq = Roboquant(EMAStrategy())


        assertDoesNotThrow {
            runBlocking {
                val ws = WebServer {
                    port = 8081
                    host = "127.0.0.1"
                    username = ""
                    password = ""
                }
                assertFalse(ws.secured)
                ws.runAsync(rq, feed, timeframe = feed.timeframe, name="run-1")
                val status = makeRequest("http://127.0.0.1:8081/")
                assertEquals(200, status)

                val status2 = makeRequest("http://127.0.0.1:8081/run/run-1")
                assertEquals(200, status2)
                ws.stop()
            }
        }
    }

}
