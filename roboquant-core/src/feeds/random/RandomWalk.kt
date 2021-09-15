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
 * Random walk creates a number of imaginary assets with a price history that follows a random walk using a
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
 * @property timeline The timeline to use for this random walk.
 *
 * @constructor Create a new RandomWalk feed
 *
 * @param nAssets
 */
class RandomWalk(
    override val timeline: List<Instant>,
    nAssets: Int = 10,
    private val generateBars: Boolean = true,
    seed: Long = Config.seed,
    private val minVolume: Int = 100_000,
    private val maxVolume: Int = 1000_000,
    private val maxDayRange: Float = 4.0f,
    private val symbolLength: Int = 4
) : HistoricFeed {

    private val random = Random(seed)
    private val data = walk(timeline.size, nAssets)

    /**
     * What are the assets in this feed
     */
    override val assets: Set<Asset>
        get() = data.keys

    companion object {

        private val logger = Logging.getLogger("RandomWalk")

        /**
         * Create a random walk for the last years, generating daily prices
         *
         * @param nAssets
         * @return
         */
        fun lastYears(years: Int = 1, nAssets: Int = 10, generateBars: Boolean = true): RandomWalk {
            val lastYear = LocalDate.now().year - 1
            val timeline = TimeFrame.fromYears(lastYear - years + 1, lastYear).toDays(excludeWeekends = true)
            return RandomWalk(timeline, nAssets, generateBars)
        }

        /**
         * Create a random walk for the last days, generating minute prices.
         *
         * @param nAssets
         * @return
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
     * Perform a random walk for the given number of events and assets
     *
     * @param size
     * @param nAssets
     * @return
     */
    private fun walk(size: Int, nAssets: Int): Map<Asset, List<PriceAction>> {
        val data = mutableMapOf<Asset, List<PriceAction>>()

        repeat(nAssets) {
            var asset: Asset?
            do {
                val symbol = generateSymbol()
                asset = Asset(symbol)
            } while (asset in data)

            val prices = if (generateBars)
                generateBars(asset!!, size)
            else
                generateSinglePrice(
                    asset!!, size
                )
            data[asset] = prices
        }
        return data
    }

    /**
     * Generate random bars
     *
     * @param asset
     * @param size
     * @return
     */
    private fun generateBars(asset: Asset, size: Int): List<PriceAction> {
        val data = mutableListOf<PriceBar>()
        var prevPrice = 100.0f
        val plusVolume = maxVolume - minVolume
        val javaRandom = random.asJavaRandom()
        repeat(size) {
            val newValue = (javaRandom.nextGaussian() + prevPrice).toFloat()
            val v = mutableListOf(newValue)
            repeat(3) {
                v.add(newValue + (javaRandom.nextFloat() * maxDayRange) - (maxDayRange / 2.0f))
            }
            v.sort()

            val volume = round(minVolume + (plusVolume * javaRandom.nextFloat()))
            val action = if (javaRandom.nextBoolean()) {
                PriceBar(asset, v[1], v[3], v[0], v[2], volume)
            } else {
                PriceBar(asset, v[2], v[3], v[0], v[1], volume)
            }
            data.add(action)

            prevPrice = if (newValue > 10.0f) newValue else 10.0f
        }
        return data
    }

    /**
     * Generate single price actions
     *
     * @param asset
     * @param size
     * @return
     */
    private fun generateSinglePrice(asset: Asset, size: Int): List<PriceAction> {
        val data = mutableListOf<TradePrice>()
        var prevPrice = 100.0
        val javaRandom = random.asJavaRandom()
        repeat(size) {
            val newValue = javaRandom.nextGaussian() + prevPrice
            val action = TradePrice(asset, newValue)
            data.add(action)
            prevPrice = if (newValue > 10.0) newValue else 10.0
        }
        return data
    }

    /**
     * Generate a random symbol (ticker) name.
     *
     * @return
     */
    private fun generateSymbol(): String {
        val alphabet = ('A'..'Z').toList()
        return List(symbolLength) { alphabet.random(random) }.joinToString("")
    }


    /**
     * See [Feed.play]
     *
     * @param channel
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

