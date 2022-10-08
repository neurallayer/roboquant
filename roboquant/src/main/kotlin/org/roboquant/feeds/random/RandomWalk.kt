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

package org.roboquant.feeds.random

import org.roboquant.common.*
import org.roboquant.feeds.*
import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random
import kotlin.random.asJavaRandom

/**
 * Random walk creates number of assets with a price history that follows a random walk using a
 * Normal distribution. It can be useful for testing, since if your strategy does well using this feed, there
 * might be something suspicious going on.
 *
 * Internally it uses a seeded random generator, so while is generates random data, the results can be reproduced if
 * instantiated with the same seed. It can generate [PriceBar] or [TradePrice] prices.
 *
 * ## Background
 * Random walk theory suggests that changes in stock prices have the same distribution and are independent of each
 * other. Therefore, it assumes the past movement or trend of a stock price or market cannot be used to predict its
 * future movement. In short, random walk theory proclaims that stocks take a random and unpredictable path that makes
 * all methods of predicting stock prices futile in the long run.
 *
 * @property timeline the timeline of this random walk.
 * @param nAssets the number of assets to generate
 * @property generateBars should PriceBars be generated or not
 * @property volumeRange
 * @property priceRange
 * @param template template to use when generating assets
 * @property seed seed to use for initializing the random generator
 *
 */
class RandomWalk(
    override val timeline: Timeline,
    nAssets: Int = 10,
    private val generateBars: Boolean = true,
    private val volumeRange: Int = 1000,
    private val priceRange: Double = 1.0,
    template: Asset = Asset("TEMPLATE"),
    private val seed: Int = 42
) : HistoricFeed {

    override val assets = 1.rangeTo(nAssets).map { template.copy(symbol = "Asset-$it") }.toSortedSet()

    init {
        logger.fine {
            "Created $nAssets assets with ${timeline.size} events and a timeline " +
                    "from ${timeline.first()} to ${timeline.last()}"
        }
    }

    private fun Random.firstPrice(): Double = nextDouble(50.0, 150.0)

    private fun Random.nextPrice(price: Double) : Double = price + asJavaRandom().nextGaussian()

    private fun Random.priceBar(asset: Asset, price: Double) : PriceAction {
        val v = mutableListOf(price)
        repeat(3) {
            v.add(price + (nextDouble(-priceRange, priceRange)))
        }
        v.sort()
        val volume = nextInt(volumeRange, volumeRange*2)
        return if (nextBoolean()) {
            PriceBar(asset, v[1], v[3], v[0], v[2], volume)
        } else {
            PriceBar(asset, v[2], v[3], v[0], v[1], volume)
        }
    }

    /**
     * Generate random single price actions
     */
    private fun Random.tradePrice(asset: Asset, price: Double) : PriceAction {
        val volume = nextInt(volumeRange, volumeRange*2)
        return TradePrice(asset, price, volume.toDouble())
    }

    override suspend fun play(channel: EventChannel) {
        val prices = mutableMapOf<Asset, Double>()
        val random = Random(seed)
        for (time in timeline) {
            val actions = mutableListOf<PriceAction>()
            for (asset in assets) {
                val lastPrice = prices[asset]
                val price =  if (lastPrice == null) random.firstPrice() else random.nextPrice(lastPrice)
                val action = if (generateBars) random.priceBar(asset, price) else random.tradePrice(asset, price)
                actions.add(action)
                prices[asset] = if (price < priceRange * 10) priceRange * 10 else price

            }
            val event = Event(actions, time)
            channel.send(event)
        }
    }

    /**
     * @suppress
     */
    companion object {

        private val logger = Logging.getLogger(RandomWalk::class)

        /**
         * Create a random walk for the last [years], generating daily prices
         */
        fun lastYears(years: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalk {
            val lastYear = LocalDate.now().year - 1
            val timeline = Timeframe.fromYears(lastYear - years + 1, lastYear).toTimeline(1.days)
            return RandomWalk(timeline, nAssets, generateBars)
        }

        /**
         * Create a random walk for the last [days], generating minute prices.
         */
        fun lastDays(days: Int = 1, nAssets: Int = 10): RandomWalk {
            val last = Instant.now()
            val first = last - days.days
            val timeline = Timeframe(first, last).toTimeline(1.minutes)
            return RandomWalk(timeline, nAssets)
        }
    }

}

