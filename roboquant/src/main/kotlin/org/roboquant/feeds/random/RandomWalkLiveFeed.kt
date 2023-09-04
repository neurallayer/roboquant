/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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



/**
 * Random walk live feed contains a number of assets with prices that follow a random walk.
 * When using this feed in multiple runs, each run will receive its own unique random-walk.
 *
 * Internally, it uses a seeded random generator. So while it generates random data, the results can be reproduced if
 * instantiated with the same seed. It can generate [PriceBar] or [TradePrice] prices.
 *
 * @see RandomWalkFeed for a historic data random walk
 *
 * @property timeSpan the timeSpan between two events, default is `1.seconds`
 * @param nAssets the number of assets to generate, default is 10.
 * @property generateBars should PriceBars be generated or plain TradePrice, default is true
 * @property volumeRange what is the volume range, default = 1000
 * @property priceChange the price range, the default is 10 bips (0.1%)
 * @param template template to use when generating assets. The symbol name will be used as a template string
 * @property seed seed to use for initializing the random generator, default is 42
 */
class RandomWalkLiveFeed(
    private val timeSpan: TimeSpan = 1.seconds,
    nAssets: Int = 10,
    private val generateBars: Boolean = true,
    private val volumeRange: Int = 1000,
    private val priceChange: Double = 10.bips,
    template: Asset = Asset("%s"),
    private val seed: Int = 42,
) : LiveFeed(), AssetFeed {

    /**
     * The assets contained in this feed. Each asset has a unique symbol name
     */
    override val assets = randomAssets(template, nAssets)

    private val logger = Logging.getLogger(RandomWalkLiveFeed::class)

    init {
        logger.debug { "assets=$nAssets" }
    }

    /**
     * @see Feed.play
     */
    override suspend fun play(channel: EventChannel) {
        val gen = RandomPriceGenerator(assets.toList(), priceChange, volumeRange, timeSpan, generateBars, seed)
        var time = Instant.now()
        while (true) {
            if (channel.closed) return
            val actions = gen.next()
            val event = Event(actions, time)
            channel.send(event)
            time += timeSpan
            time.delayUntil()
        }
    }


}

