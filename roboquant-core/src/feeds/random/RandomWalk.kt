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

package org.roboquant.feeds.random

import org.roboquant.common.Config
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.*
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.round
import kotlin.random.Random
import kotlin.random.asJavaRandom


/**
 * Random walk creates a number of randomly named assets with a price history that follows a random walk using a
 * Normal distribution. It can be useful for testing, since if your strategy does well using this feed, there
 * might be something suspicious going on.
 *
 * It used a seeded random generator, so while is generates random data, the results can be reproduced if
 * instantiated with the same seed. It can generate both single prices and bar data. The data is all generated
 * upfront and stored in memory to ensure it is fully reproducible independent of the test scenario.
 *
 * # Background
 * Random walk theory suggests that changes in stock prices have the same distribution and are independent of each other.
 * Therefore, it assumes the past movement or trend of a stock price or market cannot be used to predict its future movement.
 * In short, random walk theory proclaims that stocks take a random and unpredictable path that makes all methods of
 * predicting stock prices futile in the long run.
 *
 * @property timeline The timeline of this random walk.
 *
 */
class RandomWalk(
    override val timeline: List<Instant>,
    nAssets: Int = 10,
    generateBars: Boolean = true,
    seed: Long = Config.seed,
    minVolume: Int = 100_000,
    maxVolume: Int = 1000_000,
    maxDayRange: Double = 4.0,
    symbolLength: Int = 4,
    template: Asset = Asset("TEMPLATE")
) : HistoricFeed {

    private val random = Random(seed)
    private val data = mutableMapOf<Asset, List<PriceAction>>()

    /**
     * The assets that are in this feed
     */
    override val assets
        get() = data.keys.toSortedSet()


    init {
        val size = timeline.size
        repeat(nAssets) {
            var asset: Asset?
            do {
                val symbol = generateSymbol(symbolLength)
                asset = AssetBuilderFactory.build(symbol, template)
            } while (asset in data)

            val prices = if (generateBars)
                generateBars(asset!!, size, minVolume, maxVolume, maxDayRange)
            else
                generateSinglePrice(asset!!, size, minVolume, maxVolume)
            data[asset] = prices
        }

    }

    companion object {

        private val logger = Logging.getLogger("RandomWalk")

        /**
         * Create a random walk for the last [years], generating daily prices
         */
        fun lastYears(years: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalk {
            val lastYear = LocalDate.now().year - 1
            val timeline = TimeFrame.fromYears(lastYear - years + 1, lastYear).toDays(excludeWeekends = true)
            return RandomWalk(timeline, nAssets, generateBars)
        }

        /**
         * Create a random walk for the last [days], generating minute prices.
         */
        fun lastDays(days: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalk {
            val last = Instant.now()
            val first = last.minus(days.toLong(), ChronoUnit.DAYS)
            val timeline = TimeFrame(first, last).toMinutes(excludeWeekends = true)
            return RandomWalk(timeline, nAssets, generateBars)
        }

    }


    init {
        logger.fine {
            "Created $nAssets assets with ${timeline.size} events and a timeline " +
                    "from ${timeline.first()} to ${timeline.last()}"
        }
    }


    /**
     * Generate random bars
     */
    private fun generateBars(
        asset: Asset,
        size: Int,
        minVolume: Int,
        maxVolume: Int,
        maxDayRange: Double
    ): List<PriceAction> {
        val data = mutableListOf<PriceBar>()
        var prevPrice = 100.0
        val plusVolume = maxVolume - minVolume
        val javaRandom = random.asJavaRandom()
        repeat(size) {
            val newValue = javaRandom.nextGaussian() + prevPrice
            val v = mutableListOf(newValue)
            repeat(3) {
                v.add(newValue + (javaRandom.nextDouble() * maxDayRange) - (maxDayRange / 2.0f))
            }
            v.sort()

            val volume = round(minVolume + (plusVolume * javaRandom.nextDouble()))
            val action = if (javaRandom.nextBoolean()) {
                PriceBar(asset, v[1], v[3], v[0], v[2], volume)
            } else {
                PriceBar(asset, v[2], v[3], v[0], v[1], volume)
            }
            data.add(action)

            prevPrice = if (newValue > 10.0) newValue else 10.0
        }
        return data
    }

    /**
     * Generate random single price actions
     */
    private fun generateSinglePrice(asset: Asset, size: Int, minVolume: Int, maxVolume: Int): List<PriceAction> {
        val data = mutableListOf<TradePrice>()
        var prevPrice = 100.0
        val javaRandom = random.asJavaRandom()
        val plusVolume = maxVolume - minVolume
        repeat(size) {
            val newValue = javaRandom.nextGaussian() + prevPrice
            val volume = round(minVolume + (plusVolume * javaRandom.nextDouble()))
            val action = TradePrice(asset, newValue, volume)
            data.add(action)
            prevPrice = if (newValue > 10.0) newValue else 10.0
        }
        return data
    }

    /**
     * Generate a random symbol (ticker) name.
     */
    private fun generateSymbol(symbolLength: Int): String {
        val alphabet = ('A'..'Z').toList()
        return List(symbolLength) { alphabet.random(random) }.joinToString("")
    }


    /**
     * See [Feed.play]
     */
    override suspend fun play(channel: EventChannel) {
        for ((i, now) in timeline.withIndex()) {
            !channel.timeFrame.contains(now) && continue
            val result = mutableListOf<PriceAction>()
            for (actions in data.values) {
                result.add(actions[i])
            }
            val event = Event(result, now)
            channel.send(event)
        }
    }


}

