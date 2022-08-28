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

package org.roboquant.strategies

import org.roboquant.common.Config
import org.roboquant.feeds.Event
import kotlin.random.Random


/**
 * Strategy that randomly generates a BUY or SELL signal for the assets in the feed.
 * The configured probability is the likelihood to generate a signal. And after that,
 * there is a 50/50 chance it will generate either a BUY or SELL signal.
 *
 * @property probability the likelihood to generate a signal, a value between 0.0 and 1.0
 * @constructor Create new Random strategy
 */
class RandomStrategy(private val probability: Double = 0.05, private val random : Random = Config.random) : Strategy {


    init {
        require(probability in 0.0..1.0) {
            "probability should be a value between 0.0 and 1.0, found $probability instead"
        }
    }

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for (asset in event.prices.keys) {
            if (random.nextDouble() < probability) {
                val rating = if (random.nextDouble() < 0.5) Rating.SELL else Rating.BUY
                signals.add(Signal(asset, rating))
            }
        }
        return signals
    }

}