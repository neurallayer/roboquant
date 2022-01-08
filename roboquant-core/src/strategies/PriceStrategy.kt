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

package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Base class that can be extended by strategies that are only interested in
 * a single price for an asset and not other type of actions.
 *
 */
abstract class PriceStrategy(private val aspect: String = "DEFAULT", prefix: String = "strategy.") :
    RecordingStrategy(prefix) {

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for ((asset, priceAction) in event.prices) {
            val price = priceAction.getPrice(aspect)
            val signal = generate(asset, price, event.time)
            if (signal != null) signals.add(signal)
        }
        return signals
    }

    /**
     * Subclasses need to implement this method and return optional a signal.
     *
     * @param asset
     * @param price
     * @param now
     * @return
     */
    abstract fun generate(asset: Asset, price: Double, now: Instant): Signal?
}

