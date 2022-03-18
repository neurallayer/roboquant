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
import org.roboquant.common.Timeframe
import org.roboquant.common.Timeline
import org.roboquant.common.split
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
     * Timeline of this feed
     */
    val timeline: Timeline

    /**
     * TimeFrame of this feed. If it cannot be determined, [Timeframe.INFINITE] is returned instead.
     */
    override val timeframe: Timeframe
        get() = if (timeline.isEmpty()) Timeframe.INFINITE else Timeframe.inclusive(timeline.first(), timeline.last())

    /**
     * Draw a [random] sampled timeframe of a cetain [size] from the historic feed and return the timeframe that
     * represents this sample.
     *
     * @param size Number of events that the timeframe should contain
     * @param random Random generator to use, if none provided will use [Config.random]
     * @return
     */
    fun sample(size: Int, random: Random = Config.random) : Timeframe {
        val tl = timeline
        val start = random.nextInt(tl.size - size)
        return Timeframe(tl[start], tl[start + size])
    }


    /**
     * Split the timeframe of this feed in number of equal periods.
     *
     * @param period
     */
    fun split(period: Period) = timeframe.split(period)

    /**
     * Split the timeline of the feed in number of equal size chunks
     *
     * @param size
     */
    fun split(size: Int) = timeline.split(size)


}

