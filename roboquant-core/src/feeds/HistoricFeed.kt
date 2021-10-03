package org.roboquant.feeds

import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.common.getBySymbol
import org.roboquant.common.split
import java.time.Instant
import java.time.Period
import kotlin.random.Random

/**
 * Historic feed represents a feed with historic data, useful for back testing. Examples are CSV files with
 * stock data of the last years.
 *
 * It provides common functionality that can be used by subclasses implementing the [Feed] interface. However,
 * it is not mandatory and a feed can implement the Feed interface directly.
 *
 */
interface HistoricFeed : AssetFeed {

    /**
     * Timeline is a sorted list of all Instant instances contained in this feed
     */
    val timeline: List<Instant>

    /**
     * TimeFrame of this feed. If it cannot be determined, [TimeFrame.FULL] is returned instead.
     */
    override val timeFrame: TimeFrame
        get() = if (timeline.isEmpty()) TimeFrame.FULL else TimeFrame(timeline.first(), timeline.last().plusMillis(1))

    /**
     * Draw a random sample of event size from the historic feed and return the timeframe that represents this sample
     *
     * @param size Number of events to include
     * @param random Random generator to use
     * @return
     */
    fun sample(size: Int, random: Random = Random): TimeFrame {
        val tl = timeline
        val start = random.nextInt(tl.size - size)
        return TimeFrame(tl[start], tl[start + size])
    }

    /**
     * Split the timeline of the feed in number of equal periods
     *
     * @param period
     */
    fun split(period: Period) = timeline.split(period)

    /**
     * Split the timeline of the feed in number of equal size chunks
     *
     * @param size
     */
    fun split(size: Int) = timeline.split(size)

    /**
     * Find an asset by its symbol name. If there are multiple assets with the same symbol name,
     * the first one will be returned. If no asset is found, exception will be thrown
     *
     * @param symbol
     * @return The found asset or an exception
     */
    fun find(symbol: String): Asset = assets.getBySymbol(symbol)

}

