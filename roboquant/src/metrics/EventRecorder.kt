/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import java.time.temporal.TemporalAmount
import java.util.*

/**
 * Capture events that can then be used later to display them in a graph or perform other post-run analysis. This metric
 * also implements the [Feed] API.
 *
 * This metric is different from how most metrics work. It stores the result internally and does not
 * hand them over to a MetricsLogger. However, just like other metrics, it will reset its state at the beginning of a
 * new phase.
 */
class EventRecorder(private val maxDuration: TemporalAmount? = null) : Metric, Feed {

    private val events = LinkedList<Event>()


    override fun calculate(account: Account, event: Event) : MetricResults {
        record(event)
        return emptyMap()
    }

    fun record(event: Event) {
        events.add(event)
        if (maxDuration != null) {
            val firstTime = events.last().time - maxDuration
            while (events.first().time < firstTime) events.removeFirst()
        }
    }


    override fun reset() {
        events.clear()
    }

    override suspend fun play(channel: EventChannel) {
        for (event in events) {
            channel.send(event)
        }
    }
}