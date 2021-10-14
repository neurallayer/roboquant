/*
 * Copyright 2021 Neural Layer
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

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Capture price actions for a particular [asset]. This can then be used to display it in a graph or perform
 * other post-run analysis. This metric also implements the [Feed] API.
 *
 * This metric is different from how most metrics work. It stores the result internally and does not
 * hand them over to a logger. However, just like other metrics it will reset its state at the beginning of a
 * phase.
 */
class PriceMetric(val asset: Asset) : Metric, Feed {

    private val _prices = mutableListOf<Pair<Instant, PriceAction>>()

    val prices
        get() = _prices.toList()

    override fun calculate(account: Account, event: Event) {
        val price = event.prices[asset]
        if (price != null) _prices.add(Pair(event.now, price))
    }

    override fun start(phase: Phase) {
        super.start(phase)
        _prices.clear()
    }

    override suspend fun play(channel: EventChannel) {
        for ((now, price) in _prices) {
            val event = Event(listOf(price), now)
            channel.send(event)
        }
    }
}