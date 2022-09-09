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

import org.roboquant.common.Asset
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Base class for strategies that are only interested in a single asset and generate a Signal
 *
 * @property asset The asset for which to apply this strategy
 * @constructor Create new single asset strategy
 */
abstract class SingleAssetStrategy(
    protected val asset: Asset,
    prefix: String = "strategy.${asset.symbol}."
) : RecordingStrategy(prefix = prefix) {

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        val priceAction = event.prices[asset]
        if (priceAction != null) {
            val signal = generate(priceAction, event.time)
            result.addNotNull(signal)
        }
        return result
    }

    /**
     * Subclasses need to be implemented this method. It will only be invoked if there is
     * a price action for the asset.
     *
     * @param priceAction
     * @param now
     * @return
     */
    abstract fun generate(priceAction: PriceAction, now: Instant): Signal?

}