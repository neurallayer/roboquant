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

package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Observation
import org.roboquant.common.Timeframe
import org.roboquant.common.Timeserie
import java.time.Instant
import java.util.*
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.filter
import kotlin.collections.filterIsInstance
import kotlin.collections.groupBy
import kotlin.collections.map
import kotlin.collections.mapValues
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toDoubleArray
import kotlin.math.absoluteValue

/**
 * Interface that any feed needs to implement. A feed can deliver any type of information, ranging from
 * stock prices to content of social media.
 *
 * Feeds can represent historic data, for example during back testing, and live feeds during live trading.
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
     * A feed that may hold resources (such as file or socket handles) until it is closed. Calling the close() method
     * on a feed ensures prompt release of these resources, avoiding resource exhaustion.
     *
     * This is part of the [AutoCloseable] interface that any feed can override. If not implemented, the default
     * behavior is to do nothing.
     */
    override fun close() {
        // default is to do nothing
    }

}

/**
 * Implementations of AssetFeed need to provide the [assets] it contains.
 */
interface AssetFeed : Feed {

    /**
     * Unique sorted collection of assets contained in the feed
     */
    val assets: SortedSet<Asset>

}

/**
 * Convenience method to play a feed and return the actions of a certain type [T]. Additionally, the feed can be
 * restricted to a certain [timeframe] and if required an additional [filter] can be provided.
 */
inline fun <reified T : Action> Feed.filter(
    timeframe: Timeframe = Timeframe.INFINITE,
    crossinline filter: (T) -> Boolean = { true }
): List<Pair<Instant, T>> = runBlocking {

    val channel = EventChannel(timeframe = timeframe)
    val result = mutableListOf<Pair<Instant, T>>()

    val job = launch {
        play(channel)
        channel.close()
    }

    try {
        while (true) {
            val o = channel.receive()
            val newResults = o.actions.filterIsInstance<T>().filter(filter).map { Pair(o.time, it) }
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
 * Validate a feed for possible errors in the prices and return the result in the format Pair<Instant, PriceAction>.
 * Optionally provide a [timeframe] and the [maxDiff] value when to flag a change as an error.
 */
fun Feed.validate(
    timeframe: Timeframe = Timeframe.INFINITE,
    maxDiff: Double = 0.5,
): List<Pair<Instant, PriceAction>> = runBlocking {

    val channel = EventChannel(timeframe = timeframe)

    val job = launch {
        play(channel)
        channel.close()
    }

    val lastPrices = mutableMapOf<Asset, Double>()
    val errors = mutableListOf<Pair<Instant, PriceAction>>()

    try {
        while (true) {
            val o = channel.receive()
            for ((asset, priceAction) in o.prices) {
                val price = priceAction.getPrice()
                val prev = lastPrices[asset]
                if (prev != null) {
                    val diff = (price - prev) / prev
                    if (diff.absoluteValue > maxDiff) errors.add(Pair(o.time, priceAction))
                }
                lastPrices[asset] = price
            }

        }

    } catch (_: ClosedReceiveChannelException) {

    } finally {
        channel.close()
        if (job.isActive) job.cancel()
    }
    return@runBlocking errors
}


/**
 * Return a map with assets and their [Timeserie]
 */
inline fun <reified T : PriceAction> List<Pair<Instant, T>>.timeseries(
    type: String = "DEFAULT"
): Map<Asset, Timeserie> {
    return groupBy { it.second.asset }.mapValues { it2 ->
        it2.value.map { Observation(it.first, it.second.getPrice(type)) }
    }
}

/**
 * Convert a collection of price actions to a double array
 */
fun Collection<PriceAction>.toDoubleArray(type: String = "DEFAULT"): DoubleArray =
    this.map { it.getPrice(type) }.toDoubleArray()
