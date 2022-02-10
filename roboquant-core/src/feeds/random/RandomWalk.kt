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

import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import java.time.Instant
import java.time.LocalDate
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
    timeline: Timeline,
    nAssets: Int = 10,
    generateBars: Boolean = true,
    minVolume: Int = 100_000,
    maxVolume: Int = 1000_000,
    maxDayRange: Double = 4.0,
    symbolLength: Int = 4,
    template: Asset = Asset("TEMPLATE"),
    private val random: Random = Config.random
) : HistoricPriceFeed() {


    init {
        repeat(nAssets) {
            val symbol = generateSymbol(symbolLength)
            val asset = template.copy(symbol = symbol)

            if (generateBars)
                generateBars(asset, timeline, minVolume, maxVolume, maxDayRange)
            else
                generateSinglePrice(asset, timeline, minVolume, maxVolume)
        }
    }

    companion object {

        private val logger = Logging.getLogger(RandomWalk::class)

        /**
         * Create a random walk for the last [years], generating daily prices
         */
        fun lastYears(years: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalk {
            val lastYear = LocalDate.now().year - 1
            val timeline = Timeframe.fromYears(lastYear - years + 1, lastYear).toDays(excludeWeekends = true)
            return RandomWalk(timeline, nAssets, generateBars)
        }

        /**
         * Create a random walk for the last [days], generating minute prices.
         */
        fun lastDays(days: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalk {
            val last = Instant.now()
            val first = last - days.days
            val timeline = Timeframe(first, last).toMinutes(excludeWeekends = true)
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
        timeline: Timeline,
        minVolume: Int,
        maxVolume: Int,
        maxDayRange: Double
    ) {
        var prevPrice = 100.0
        val plusVolume = maxVolume - minVolume
        val javaRandom = random.asJavaRandom()
        for (time in timeline) {
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
            add(time, action)

            prevPrice = if (newValue > 10.0) newValue else 10.0
        }
    }

    /**
     * Generate random single price actions
     */
    private fun generateSinglePrice(asset: Asset, timeline: Timeline, minVolume: Int, maxVolume: Int) {
        var prevPrice = 100.0
        val javaRandom = random.asJavaRandom()
        val plusVolume = maxVolume - minVolume
        for (time in timeline) {
            val newValue = javaRandom.nextGaussian() + prevPrice
            val volume = round(minVolume + (plusVolume * javaRandom.nextDouble()))
            val action = TradePrice(asset, newValue, volume)
            add(time, action)
            prevPrice = if (newValue > 10.0) newValue else 10.0
        }
    }

    /**
     * Generate a random symbol (ticker) name.
     */
    private fun generateSymbol(symbolLength: Int): String {
        val alphabet = ('A'..'Z').toList()
        return List(symbolLength) { alphabet.random(random) }.joinToString("")
    }


}

