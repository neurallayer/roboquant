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

package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.common.getBySymbol
import java.io.Closeable
import java.time.Instant
import java.util.*

/**
 * Interface that any feed needs to implement. A feed can deliver any type of information, ranging from
 * stock prices to content of social media.
 *
 * Feeds can represent historic data, for example during back testing, and live feeds during live trading.
 */
interface Feed : Closeable {

    /**
     * Timeframe of the feed. In case the timeframe is not known upfront, return the full timeframe [Timeframe.INFINITY]
     */
    val timeframe: Timeframe
        get() = Timeframe.INFINITY

    /**
     * (Re)play the events of the feed on put these events on the provided [channel]. Once done, return from this method.
     * Implementations that hold resources like open file descriptors, should carefully handle Channel related exceptions
     * and make sure these resources are released before returning from the method.
     */
    suspend fun play(channel: EventChannel)

    /**
     * Close the feed and free resources held by the feed. This is part of the [Closeable] interface that any feed
     * can implement. If not implemented, the default behavior is to do nothing.
     */
    override fun close() {}

}

/**
 * Implementations of AssetFeed need to provide the [assets] it contains.
 *
 */
interface AssetFeed : Feed {

    /**
     * Unique sorted collection of assets contained in the feed
     */
    val assets: SortedSet<Asset>

    /**
     * Find an asset by its [symbol] name. If there are multiple assets with the same symbol name,
     * the first one will be returned. If no asset is found, exception will be thrown
     */
    fun find(symbol: String): Asset = assets.getBySymbol(symbol)
}


/**
 * Convenience method to play a feed and return the actions of a certain type [T]. Additionally, the feed can be
 * restricted to a certain [timeframe] and if required an additional [filter] can be provided.
 */
inline fun <reified T : Action> Feed.filter(
    timeframe: Timeframe = Timeframe.INFINITY,
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

    } finally {
        channel.close()
        if (job.isActive) job.cancel()
    }
    return@runBlocking result
}

/**
 * Convert a collection of price actions to a double array
 */
fun Collection<PriceAction>.toDoubleArray(type:String = "DEFAULT") : DoubleArray = this.map { it.getPrice(type) }.toDoubleArray()