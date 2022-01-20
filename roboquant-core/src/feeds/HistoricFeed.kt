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

import org.roboquant.common.Config
import org.roboquant.common.TimeFrame
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
     * TimeFrame of this feed. If it cannot be determined, [TimeFrame.INFINITY] is returned instead.
     */
    override val timeFrame: TimeFrame
        get() = if (timeline.isEmpty()) TimeFrame.INFINITY else TimeFrame(timeline.first(), timeline.last().plusMillis(1))

    /**
     * Draw a random sampled time-frame of a [size] from the historic feed and return the timeframe that represents this sample
     *
     * @param size Number of events that the timeframe should contain
     * @param random Random generator to use, if none provided will use [Config.random]
     * @return
     */
    fun sample(size: Int, random: Random = Config.random) : TimeFrame {
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


}

