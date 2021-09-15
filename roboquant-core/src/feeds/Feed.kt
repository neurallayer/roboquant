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
 * Feeds can represent historic data, for example during back testing, and live feeds during.
 *
 */
interface Feed {

    /**
     * Time frame of the feed. In case the timeframe unknown upfront, use the default [TimeFrame.FULL]
     */
    val timeFrame: TimeFrame
        get() = TimeFrame.FULL

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
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
 * Utility method to play a feed and filter the relevant actions.
 *
 * @param T the type of action
Updtaed  * @param timeFrame an optional timeframe, the default is full timeline
 * @param filter The filter you want to apply, If you want to receive all, use *{ true }*
 * @receiver
 */
inline fun <reified T : Action> Feed.filter(
    timeFrame: TimeFrame = TimeFrame.FULL,
    crossinline filter: (T) -> Boolean
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