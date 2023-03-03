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

package org.roboquant.strategies

import org.roboquant.common.AssetFilter
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction

/**
 * Only allow price actions that meet the provided [assetFilter] to be passed to the underlying [strategy].
 *
 * @see Strategy.filter
 *
 * @property strategy the strategy to use after the filter has been applied
 * @property assetFilter the asset filter to apply
 * @constructor Create new Asset filter strategy
 */
class AssetFilterStrategy(private val strategy: Strategy, private val assetFilter: AssetFilter) : Strategy by strategy {

    override fun generate(event: Event): List<Signal> {
        val actions = event.actions.filterIsInstance<PriceAction>().filter { assetFilter.filter(it.asset, event.time) }
        val newEvent = Event(actions, event.time)
        return strategy.generate(newEvent)
    }

}

/**
 * Convenience extension method to create a [AssetFilterStrategy].
 *
 * @see AssetFilterStrategy
 *
 * @param assetFilter the asset filter to apply
 */
fun Strategy.filter(assetFilter: AssetFilter) = AssetFilterStrategy(this, assetFilter)