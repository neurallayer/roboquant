/*
 * Copyright 2020-2025 Neural Layer
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

@file:Suppress("RunBlocking")

package org.roboquant.feeds

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.roboquant.common.Asset
import org.roboquant.common.Event
import org.roboquant.common.Item
import org.roboquant.common.PriceItem
import org.roboquant.common.Timeframe
import java.time.Instant
import kotlin.collections.set
import kotlin.math.absoluteValue

/**
 * Interface that any data feed needs to implement. A feed can deliver any type of information, ranging from
 * stock prices to content of social media.
 *
 * Feeds can represent historic data, for example, during back testing, and live feeds during live trading.
 */
interface Feed : AutoCloseable {

    /**
     * Timeframe of the feed. In case the timeframe is not known upfront, the default is to return [Timeframe.INFINITE]
     */
    val timeframe: Timeframe
        get() = Timeframe.INFINITE

    /**
     * (Re)play the events of the feed by putting the events on the provided [channel]. Once done, return from this
     * method.
     */
    suspend fun play(channel: EventChannel)

    /**
     * A feed may hold resources (such as file or socket handles) until it is closed. Calling the close() method
     * on a feed ensures prompt release of these resources, avoiding resource exhaustion.
     *
     * This is part of the [AutoCloseable] interface that any feed can override. If not implemented, the default
     * behavior is to do nothing.
     */
    override fun close() {
        // default is to do nothing
    }

    /**
     * (Re)play the events of the feed in the background without blocking the current thread and returns
     * a reference to the coroutine as a [Job].
     *
     * The channel will be closed after the replay of events has finished.
     *
     * @see play
     */
    fun playBackground(channel: EventChannel): Job {

        // The default CoroutineDispatcher is backed by a shared pool of threads.
        // The maximum number of threads is equal to the number of CPU cores but is at least two.
        val scope = CoroutineScope(Dispatchers.Default)

        return scope.launch {
            channel.use { play(it) }
        }

    }

}

/**
 * Implementations of AssetFeed need to list the [assets] it contains.
 */
interface AssetFeed : Feed {

    /**
     * Returns a sorted set of all the assets contained in this feed
     */
    val assets: Set<Asset>

}

/**
 * Convenience method to play a feed and return the items of a certain type [T]. Additionally, the feed can be
 * restricted to a certain [timeframe] and if required an additional [filter] can be provided.
 */
@Suppress("RunBlocking")
inline fun <reified T : Item> Feed.filter(
    timeframe: Timeframe = Timeframe.INFINITE,
    timeOutMillis: Long = -1,
    crossinline filter: (T) -> Boolean = { true },
): List<Pair<Instant, T>> = runBlocking {

    val channel = EventChannel(timeframe = timeframe)
    val result = mutableListOf<Pair<Instant, T>>()
    val job = playBackground(channel)

    try {
        while (true) {
            val o = channel.receive( timeOutMillis )
            val newResults = o.items.filterIsInstance<T>().filter(filter).map { Pair(o.time, it) }
            result.addAll(newResults)
        }

    } catch (_: ClosedReceiveChannelException) {
        // Intentionally left empty
    } finally {
        channel.close()
        if (job.isActive) job.cancel()
    }
    return@runBlocking result
}


/**
 * Convenience method to apply some logic to a feed
 */
inline fun <reified T : Item> Feed.apply(
    timeframe: Timeframe = Timeframe.INFINITE,
    crossinline block: (T, Instant) -> Unit
) = runBlocking {

    val channel = EventChannel(timeframe = timeframe)
    val job = playBackground(channel)

    try {
        while (true) {
            val o = channel.receive()
            o.items.filterIsInstance<T>().forEach { block(it, o.time) }
        }

    } catch (_: ClosedReceiveChannelException) {
        // Intentionally left empty
    } finally {
        if (job.isActive) job.cancel()
    }

}

/**
 * Convenience method to apply some logic to a feed
 */
inline fun Feed.applyEvents(
    timeframe: Timeframe = Timeframe.INFINITE,
    crossinline block: (Event) -> Unit
) = runBlocking {

    val channel = EventChannel(timeframe = timeframe)
    val job = playBackground(channel)

    try {
        while (true) {
            val o = channel.receive()
            block(o)
        }

    } catch (_: ClosedReceiveChannelException) {
        // Intentionally left empty
    } finally {
        if (job.isActive) job.cancel()
    }

}

/**
 * Convert a feed to a list of events, optionally limited to the provided [timeframe].
 */
@Suppress("RunBlocking")
fun Feed.toList(
    timeframe: Timeframe = Timeframe.INFINITE,
): List<Event> = runBlocking {

    val channel = EventChannel(timeframe = timeframe)
    val result = mutableListOf<Event>()

    val job = playBackground(channel)

    try {
        while (true) {
            val event = channel.receive()
            result.add(event)
        }
    } catch (_: ClosedReceiveChannelException) {
        // Intentionally left empty
    } finally {
        channel.close()
        if (job.isActive) job.cancel()
    }
    return@runBlocking result
}

/**
 * Validate a feed for possible errors in the prices and return the result in the format Pair<Instant, PriceItem>.
 * Optionally provide a [timeframe] and the [maxDiff] value when to flag a change as an error.
 */
fun Feed.validate(
    timeframe: Timeframe = Timeframe.INFINITE,
    maxDiff: Double = 0.5,
    priceType: String = "DEFAULT"
): List<Pair<Instant, PriceItem>> = runBlocking {

    val channel = EventChannel(timeframe = timeframe)
    val job = playBackground(channel)

    val lastPrices = mutableMapOf<Asset, Double>()
    val errors = mutableListOf<Pair<Instant, PriceItem>>()

    try {
        while (true) {
            val o = channel.receive()
            for ((asset, priceItem) in o.prices) {
                val price = priceItem.getPrice(priceType)
                val prev = lastPrices[asset]
                if (prev != null) {
                    val diff = (price - prev) / prev
                    if (diff.absoluteValue > maxDiff) errors.add(Pair(o.time, priceItem))
                }
                lastPrices[asset] = price
            }

        }

    } catch (_: ClosedReceiveChannelException) {
        // Intentionally left empty
    } finally {
        channel.close()
        if (job.isActive) job.cancel()
    }
    return@runBlocking errors
}

/**
 * Convert a collection of price items to a double array
 */
fun Collection<PriceItem>.toDoubleArray(type: String = "DEFAULT"): DoubleArray =
    this.map { it.getPrice(type) }.toDoubleArray()

