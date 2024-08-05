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
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.roboquant.common.Asset
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.common.plus
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.collections.set
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * Aggregate prices in a live [feed] to a [PriceBar]. The [aggregationPeriod] is configurable. Right now there is
 * support for aggregating the following types of price actions:
 *
 * 1. PriceBar
 * 2. TradePrice
 * 3. PriceQuote (midpoint)
 * 4. OrderBook (midpoint)
 *
 * If an action is not recognized, it is ignored.
 *
 * @property feed the feed to use that has the prices that need to be aggregated
 * @property aggregationPeriod the aggregation period, for example `15.minutes`
 * @property remaining should any remaining actions be sent, default is true
 *
 */
@Suppress("unused")
class AggregatorLiveFeed(
    private val feed: LiveFeed,
    private val aggregationPeriod: TimeSpan,
    private val remaining: Boolean = true,
    private val continuation: Boolean = true,
    private val restrictType: KClass<*>? = null
) : Feed {

    private fun Instant.expirationTime(): Instant {
        val intervalMillis = until(this + aggregationPeriod, ChronoUnit.MILLIS)
        val adjustedInstantMillis = toEpochMilli() / intervalMillis * intervalMillis
        return Instant.ofEpochMilli(adjustedInstantMillis) + aggregationPeriod
    }

    init {
        require(!aggregationPeriod.isZero)
    }

    /**
     * Provide the timeframe, this can be slightly off since upfront it is not known what the actions are that are
     * in the underlying feed.
     */
    override val timeframe: Timeframe
        get() = feed.timeframe.extend(aggregationPeriod)

    private operator fun PriceBar.plus(other: PriceBar): PriceBar {
        val high = max(other.high, high)
        val low = min(other.low, low)
        return PriceBar(asset, open, high, low, other.close, volume + other.volume, aggregationPeriod)
    }

    private suspend fun send(channel: EventChannel, time: Instant, history: MutableMap<Asset, PriceBar>) {
        val newEvent = synchronized(history) {
            val items = history.values.toList()
            val newEvent = Event(time, items)
            history.clear()
            if (continuation)
               items.forEach {
                   val c = it.close
                   val v = if (it.volume.isFinite()) 0.0 else Double.NaN
                   history[it.asset] = PriceBar(it.asset, c,c,c,c, v, it.timeSpan)
               }
            newEvent
        }

        channel.sendNotEmpty(newEvent)
    }

    /**
     * @see Feed.play
     */
    @Suppress("CyclomaticComplexMethod")
    override suspend fun play(channel: EventChannel) {
        val inputChannel = channel.clone()
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val history = mutableMapOf<Asset, PriceBar>()
        val now = Instant.now()
        var expiration: Instant = now.expirationTime()
        assert(expiration > now)

        val job2 = scope.launch {
            while (true) {
                send(channel, expiration, history)
                expiration += aggregationPeriod
                val intervalMillis = Instant.now().until(expiration, ChronoUnit.MILLIS)
                delay(intervalMillis)
            }
        }

        val job = feed.playBackground(inputChannel)

        try {
            while (true) {
                val event = inputChannel.receive()
                val items = event.items

                // Send heart beats from the original feed
                if (items.isEmpty()) {
                    channel.send(event)
                    continue
                }

                synchronized(history) {
                    for (item in items) {
                        if (restrictType != null && ! restrictType.isInstance(item)) continue
                        val pb = getPriceBar(item, aggregationPeriod) ?: continue
                        val asset = pb.asset
                        val entry = history[asset]
                        if (entry == null) history[asset] = pb else history[asset] = entry + pb
                    }
                }

            }
        } catch (_: ClosedReceiveChannelException) {
            // NOP
        } finally {
            // Send remaining
            if (remaining) send(channel, expiration, history)
        }
        if (job.isActive) job.cancel()
        if (job2.isActive) job2.cancel()
    }
}


