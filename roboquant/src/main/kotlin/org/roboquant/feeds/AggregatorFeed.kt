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

package org.roboquant.feeds

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import org.roboquant.common.Asset
import org.roboquant.common.TimePeriod
import org.roboquant.common.Timeframe
import org.roboquant.common.plus
import java.time.Instant
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min

/**
 * Aggregate prices in a [feed] to a [PriceBar]. The [aggregationPeriod] is configurable. Right now there is support for
 * aggregating the following types of price actions:
 *
 * 1. PriceBar
 * 2. TradePrice
 * 3. PriceQuote (midpoint)
 * 4. OrderBook (midpoint)
 *
 * If an action is not recognized, it is immediately forwarded. Leftovers are not transmitted.
 *
 * @property feed the feed to use that has the prices that need to be aggregated
 * @property aggregationPeriod the aggregation period, for example `15.minutes`
 */
class AggregatorFeed(val feed: Feed, private val aggregationPeriod: TimePeriod) : Feed {

    private class Entry(val bar: PriceBar, val expiration: Instant)

    /**
     * Provide the timeframe, this can be off (shorter) since upfront it is not known what the actions are that are
     * in the underlying feed.
     */
    override val timeframe: Timeframe
        get() = feed.timeframe

    private operator fun PriceBar.plus(other: PriceBar) : PriceBar {
        val high = max(other.high, high)
        val low = min(other.low, low)
        return PriceBar(asset, open, high, low, other.close, volume + other.volume)
    }

    private fun getPriceBar(action: Action) : PriceBar? {
        return when (action) {

            is PriceBar -> action

            is TradePrice -> {
                val price = action.price
                PriceBar(action.asset, price, price, price, price, action.volume)
            }

            is PriceQuote -> {
                val price = action.getPrice("MIDPOINT")
                PriceBar(action.asset, price, price, price, price, action.volume)
            }

            is OrderBook -> {
                val price = action.getPrice("MIDPOINT")
                PriceBar(action.asset, price, price, price, price, action.volume)
            }

            else -> null
        }
    }

    /**
     * @suppress
     */
    override suspend fun play(channel: EventChannel) {
        val c = EventChannel(channel.capacity, channel.timeframe)
        val scope = CoroutineScope(Dispatchers.Default + Job())
        val job = scope.launch {
            c.use {
                feed.play(it)
            }
        }

        try {
            val history = mutableMapOf<Asset, Entry>()
            while (true) {
                val result = mutableListOf<Action>()
                val event = c.receive()
                val time = event.time
                for (action in event.actions) {
                    val pb = getPriceBar(action)

                    // If we don't recognize it as a supported PriceAction, just directly forward the action
                    if (pb == null) {
                        result.add(action)
                        continue
                    }

                    val asset = pb.asset
                    val entry = history[asset]
                    if (entry == null) {
                        val expiration = time + aggregationPeriod
                        history[asset] = Entry(pb, expiration)
                    } else {
                        val newPb = entry.bar + pb
                        if (time > entry.expiration) {
                            result.add(newPb)
                            history.remove(asset)
                        } else {
                            history[asset] = Entry(newPb, entry.expiration)
                        }
                    }
                }

                if (result.isNotEmpty()) {
                    val newEvent = Event(result, time)
                    channel.send(newEvent)
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // NOP
        } finally {
            if (job.isActive) job.cancel()
        }
    }

}