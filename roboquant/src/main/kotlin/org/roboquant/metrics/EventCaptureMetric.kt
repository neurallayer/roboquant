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
import org.roboquant.common.Timeframe
import org.roboquant.common.Timeline
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import java.util.*

/**
 * Capture events that can then be used later to display them in a graph or perform other post-run analysis. This metric
 * also implements the [Feed] API, so captured events can be replayed afterwards as a feed.
 *
 * This metric works differently from most other metrics. It stores the result internally in memory and does not
 * return them to a MetricsLogger. However, just like other metrics, it will reset its state at the beginning of a
 * new phase.
 *
 * @property timeframe the timeframe to capture, default is [Timeframe.INFINITE] (capture everything)
 */
class EventCaptureMetric(timeframe: Timeframe = Timeframe.INFINITE) : Metric, Feed {

    private val limit = timeframe
    private val events = LinkedList<Event>()

    override fun calculate(account: Account, event: Event): MetricResults {
        if (event.time in limit) events.add(event)
        return emptyMap()
    }


    override fun reset() {
        events.clear()
    }

    override val timeframe
        get() = Timeframe(events.first.time, events.last.time, true)

    /**
     * Return the timeline of the events captured
     */
    val timeline: Timeline
        get() = events.map { it.time }

    override suspend fun play(channel: EventChannel) {
        for (event in events) {
            channel.send(event)
        }
    }
}