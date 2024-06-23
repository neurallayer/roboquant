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

package org.roboquant.feeds

import kotlinx.coroutines.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.common.*
import org.roboquant.journals.MemoryJournal
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertTrue

internal class LiveFeedTest {

    @Test
    fun combined() = runBlocking {

        class MyLiveFeed : LiveFeed()

        val feed1 = MyLiveFeed()
        val feed2 = MyLiveFeed()
        val feed = CombinedLiveFeed(feed1, feed2)

        val job = feed.playBackground(EventChannel())

        assertDoesNotThrow {
            feed.close()
            feed.close()
        }
        if (job.isActive) job.cancel()

    }

    @Test
    fun concurrency() {

        class MyLiveFeed : LiveFeed() {

            var stop = false

            fun start(delayInMillis: Long) {
                val scope = CoroutineScope(Dispatchers.Default + Job())

                scope.launch {
                    val asset = Asset("ABC")
                    val actions = listOf(TradePrice(asset, 100.0))

                    while (true) {
                        try {
                            sendAsync(event = Event(Instant.now(), actions))
                            delay(delayInMillis)
                            if (stop) break
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                }

            }


        }

        val feed = MyLiveFeed()
        val tf = Timeframe.next(1.seconds)

        val jobs = ParallelJobs()


        tf.sample(200.millis, 10, resolution = ChronoUnit.MILLIS).forEach {
            jobs.add {
                val j = MemoryJournal(ProgressMetric())
                org.roboquant.runAsync(feed, EMACrossover(), j, it)
                val actions = j.getMetric("progress.actions").values.last()
                assertTrue(actions > 2)
            }
        }

        feed.start(delayInMillis = 50)
        // println("feed started")

        jobs.joinAllBlocking()
        // println("runs are done")
        feed.stop = true
    }


}
