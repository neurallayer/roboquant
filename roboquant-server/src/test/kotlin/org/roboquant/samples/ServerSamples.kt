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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.feeds.random.RandomWalkLiveFeed
import org.roboquant.journals.MemoryJournal
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.PriceMetric
import org.roboquant.server.WebServer
import org.roboquant.strategies.EMAStrategy
import kotlin.system.exitProcess
import kotlin.test.Ignore
import kotlin.test.Test


internal class ServerSamples {

    private fun getRoboquant() =
        Roboquant(EMAStrategy())

    private fun getJournal() =
        MemoryJournal(PriceMetric("CLOSE"), AccountMetric())

    /**
     * You can run this sample to start a server with three runs
     */
    @Test
    @Ignore
    internal fun run() {
        val server = WebServer()

        val jobs = ParallelJobs()

        val tf1 = Timeframe.next(10.minutes)
        val tf2 = Timeframe.next(30.minutes)
        val tf3 = Timeframe.next(60.minutes)

        // Start three runs
        jobs.add {
            server.runAsync(
                getRoboquant(),
                RandomWalkLiveFeed(200.millis, nAssets = 3),
                getJournal(),
                tf1,
                "run-fast"
            )
        }
        jobs.add {
            server.runAsync(
                getRoboquant(),
                RandomWalkLiveFeed(5.seconds, nAssets = 10),
                getJournal(),
                tf2,
                "run-medium"
            )
        }
        jobs.add {
            server.runAsync(
                getRoboquant(),
                RandomWalkLiveFeed(30.seconds, nAssets = 50),
                getJournal(),
                tf3,
                "run-slow"
            )
        }

        jobs.joinAllBlocking()
        server.stop()
        exitProcess(0)
    }

}
