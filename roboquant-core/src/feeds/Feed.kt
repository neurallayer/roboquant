package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import java.time.Instant

/**
 * Interface that any feed needs to implement. A feed can deliver any type of information, ranging from
 * stock prices to content of social media.
 *
 * Feeds can represent historic data, for example during back testing, and live feeds during live trading.
 */
interface Feed {

    /**
     * Timeframe of the feed. In case the timeframe is not known upfront, return the full timeframe [TimeFrame.FULL]
     */
    val timeFrame: TimeFrame
        get() = TimeFrame.FULL

    /**
     * (Re)play the events of the feed using the provided [channel] to publish the events
     */
    suspend fun play(channel: EventChannel)

}

/**
 * Implementations of AssetFeed need to provide the assets they contain.
 *
 * @constructor Create empty Asset feed
 */
interface AssetFeed : Feed {

    /**
     * Unique collection of assets contained in the feed
     */
    val assets: Collection<Asset>
}


/**
 * Convenience method to play a feed and return the actions of a certain type [T]. Additionally, the feed can be
 * restricted to a certain [timeFrame] and an additional [filter] can be provided.
 */
inline fun <reified T : Action> Feed.filter(
    timeFrame: TimeFrame = TimeFrame.FULL,
    crossinline filter: (T) -> Boolean = { true }
): List<Pair<Instant, T>> = runBlocking {

    val channel = EventChannel(timeFrame = timeFrame)
    val result = mutableListOf<Pair<Instant, T>>()

    val job = launch {
        play(channel)
        channel.close()
    }

    try {
        while (true) {
            val o = channel.receive()
            val newResults = o.actions.filterIsInstance<T>().filter(filter).map { Pair(o.now, it) }
            result.addAll(newResults)
        }

    } catch (e: ClosedReceiveChannelException) {

    } finally {
        channel.close()
        if (job.isActive) job.cancel()
    }
    return@runBlocking result
}