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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.*
import org.roboquant.feeds.*
import java.util.*

/**
 * Record events that can then be used to later display them in a graph or perform another post-run analysis.
 * This metric also implements the [Feed] API, so recorded events can be replayed afterwards as a normal feed.
 *
 * This metric works differently from most other metrics. It stores the events internally in memory and does not
 * return them to a MetricsLogger. However, just like other metrics, it will reset its state when `reset()` is invoked.
 *
 * @property timeSpan the timeSpan to record, default is 1 year.
 */
class EventRecorderMetric(val timeSpan: TimeSpan = 1.years) : Metric, AssetFeed {

    private val events = Collections.synchronizedList(LinkedList<Event>())

    override fun calculate(account: Account, event: Event): Map<String, Double> {
        events.add(event)
        val cutOff = event.time - timeSpan
        synchronized(events) {
            for (entry in events.toList()) {
                if (entry.time < cutOff) events.removeFirst() else break
            }
        }
        return emptyMap()
    }

    /**
     * Reset the state
     */
    override fun reset() {
        synchronized(events) {
            events.clear()
        }
    }

    override val assets: SortedSet<Asset>
        get() = synchronized(events) {
            events.map { it.actions.filterIsInstance<PriceAction>().map { action -> action.asset } }.flatten()
                .toSortedSet()
        }

    /**
     * The actual timeframe recorded so far.
     */
    override val timeframe
        get() = events.timeframe

    /**
     * Return the timeline of the events captured
     */
    val timeline: Timeline
        get() = events.map { it.time }

    /**
     * Play the events recorded so far
     */
    override suspend fun play(channel: EventChannel) {
        val localEvents = synchronized(events) { events.toList() }
        for (event in localEvents) {
            channel.send(event)
        }

    }
}
