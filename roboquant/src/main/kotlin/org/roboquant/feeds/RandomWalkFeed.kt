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
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Random walk feed contains a number of assets with a price history that follows a random walk. It can be useful for
 * testing since, if your strategy does well using this feed, there might be something suspicious going on.
 *
 * Internally, it uses a seeded random generator. So while it generates random data, the results can be reproduced if
 * instantiated with the same seed. It can generate [PriceBar] or [TradePrice] prices.
 *
 * ## Background
 * Random walk theory suggests that changes in stock prices have the same distribution and are independent of each
 * other. Therefore, it assumes the past movement or trend of a stock price or market cannot be used to predict its
 * future movement. In short, random walk theory proclaims that stocks take a random and unpredictable path that makes
 * all methods of predicting stock prices futile in the long run.
 *
 * @property timeframe the timeframe of this random walk
 * @property timeSpan the timeSpan between two events, default is `1.days`
 * @param nAssets the number of assets to generate, symbol names will be ASSET1, ASSET2, ..., ASSET<N>. Default is 10.
 * @property generateBars should PriceBars be generated or plain TradePrice, default is true
 * @property volumeRange what is the volume range, default = 1000
 * @property priceRange the price range, the default is 1.0
 * @param template template to use when generating assets
 * @property seed seed to use for initializing the random generator, default is 42
 */
class RandomWalkFeed(
    override val timeframe: Timeframe,
    private val timeSpan: TimeSpan = 1.days,
    nAssets: Int = 10,
    private val generateBars: Boolean = true,
    private val volumeRange: Int = 1000,
    private val priceRange: Double = 1.0,
    template: Asset = Asset("ASSET"),
    private val seed: Int = 42
) : HistoricFeed {

    /**
     * The assets contained in this feed. Each asset has a unique symbol name of `template.symbol<nr>`
     */
    override val assets = 1.rangeTo(nAssets).map { template.copy(symbol = "${template.symbol}$it") }.toSortedSet()

    /**
     * The timeline
     */
    override val timeline: Timeline
        get() {
            val result = mutableListOf<Instant>()
            var time = timeframe.start
            while (timeframe.contains(time)) {
                result.add(time)
                time += timeSpan
            }
            return result
        }

    init {
        logger.debug { "assets=$nAssets timeframe=$timeframe" }
    }

    private fun SplittableRandom.firstPrice(): Double = nextDouble(50.0, 150.0)

    private fun SplittableRandom.nextPrice(price: Double): Double = price + nextDouble(-1.0, 1.0)

    private fun SplittableRandom.priceBar(asset: Asset, price: Double): PriceAction {
        val v = mutableListOf(price)
        repeat(3) {
            v.add(price + (nextDouble(-priceRange, priceRange)))
        }
        v.sort()
        val volume = nextInt(volumeRange, volumeRange * 2)
        return if (nextBoolean()) {
            PriceBar(asset, v[1], v[3], v[0], v[2], volume, timeSpan)
        } else {
            PriceBar(asset, v[2], v[3], v[0], v[1], volume, timeSpan)
        }
    }

    /**
     * Generate random single price actions
     */
    private fun SplittableRandom.tradePrice(asset: Asset, price: Double): PriceAction {
        val volume = nextInt(volumeRange, volumeRange * 2)
        return TradePrice(asset, price, volume.toDouble())
    }

    /**
     * @see Feed.play
     */
    override suspend fun play(channel: EventChannel) {
        val prices = mutableMapOf<Asset, Double>()
        val random = SplittableRandom(seed.toLong())
        var time = timeframe.start
        while (timeframe.contains(time)) {
            val actions = mutableListOf<PriceAction>()
            for (asset in assets) {
                val lastPrice = prices[asset]
                val price = if (lastPrice == null) random.firstPrice() else random.nextPrice(lastPrice)
                val action = if (generateBars) random.priceBar(asset, price) else random.tradePrice(asset, price)
                actions.add(action)
                prices[asset] = if (price < priceRange * 10) priceRange * 10 else price

            }
            val event = Event(actions, time)
            channel.send(event)
            time += timeSpan
        }
    }

    /**
     * @suppress
     */
    companion object {

        private val logger = Logging.getLogger(RandomWalkFeed::class)

        /**
         * Create a random walk for the last [years], generating daily prices
         */
        fun lastYears(years: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalkFeed {
            val lastYear = LocalDate.now().year
            val tf = Timeframe.fromYears(lastYear - years, lastYear)
            return RandomWalkFeed(tf, 1.days, nAssets, generateBars)
        }

        /**
         * Create a random walk for the last [days], generating minute prices.
         */
        fun lastDays(days: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalkFeed {
            val last = Instant.now()
            val first = last - days.days
            val tf = Timeframe(first, last)
            return RandomWalkFeed(tf, 1.minutes, nAssets, generateBars)
        }
    }

}

