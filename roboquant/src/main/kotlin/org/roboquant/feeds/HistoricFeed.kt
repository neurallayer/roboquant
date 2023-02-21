/*
 * Copyright 2020-2023 Neural Layer
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

import org.roboquant.common.*
import kotlin.random.Random

/**
 * Historic feed represents a feed of historic data, useful for back testing. Examples are CSV files with
 * stock data of the last years.
 *
 * This interface defines common functionality that can be used by subclasses implementing the [Feed] interface;
 * it contains access to the [timeline], [timeframe] and [assets] of the feed.
 */
interface HistoricFeed : AssetFeed {

    /**
     * Timeline of this feed
     */
    val timeline: Timeline

    /**
     * TimeFrame of this feed.
     */
    override val timeframe: Timeframe
        get() = timeline.timeframe

    /**
     * Draw a [random] sampled timeframe of a certain [size] from the historic feed and return a timeframe that
     * represents this sample.
     */
    fun sample(size: Int, samples: Int = 1, random: Random = Config.random) = timeline.sample(size, samples, random)

    /**
     * Split the timeframe of this feed in number of timeframes of equal [period].
     */
    fun split(period: TradingPeriod, overlap: TradingPeriod = 0.days) = timeframe.split(period, overlap)

    /**
     * Split the timeline of the feed in number of timeframes equal [size].
     */
    fun split(size: Int) = timeline.split(size)

}

