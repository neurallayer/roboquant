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

import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.TimeSpan
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import java.util.*

internal class RandomPriceGenerator(
    private val assets: List<Asset>,
    private val priceChange: Double,
    private val volumeRange: Int,
    private val timeSpan: TimeSpan,
    private val generateBars: Boolean,
    seed: Int
) {

    private val random = SplittableRandom(seed.toLong())

    private fun Double.nextPrice() = this * (1.0 + random.nextDouble(-priceChange, priceChange))

    // Create initial prices for all assets between 50 and 500
    private val prices = assets.map { random.nextDouble(50.0, 500.0) }.toMutableList()

    private fun priceBar(asset: Asset, price: Double): PriceAction {
        val v = DoubleArray(4) { price.nextPrice() }
        v.sort()

        val volume = random.nextInt(volumeRange / 2, volumeRange * 2)

        // Should open be higher than close
        return if (random.nextBoolean()) {
            PriceBar(asset, v[1], v[3], v[0], v[2], volume, timeSpan)
        } else {
            PriceBar(asset, v[2], v[3], v[0], v[1], volume, timeSpan)
        }
    }

    /**
     * Generate random single price actions
     */
    private fun tradePrice(asset: Asset, price: Double): PriceAction {
        val volume = random.nextInt(volumeRange / 2, volumeRange * 2)
        return TradePrice(asset, price, volume.toDouble())
    }

    internal fun next() = buildList {
        for ((idx, asset) in assets.withIndex()) {
            val lastPrice = prices[idx]
            val price = lastPrice.nextPrice().coerceAtLeast(priceChange * 2.0)
            val action = if (generateBars) priceBar(asset, price) else tradePrice(asset, price)
            add(action)
            prices[idx] = price
        }
    }


}


private fun randomNames(size: Int, len: Int): Set<String> {
    val result = mutableSetOf<String>()
    val charPool = ('A'..'Z')
    while (result.size < size) {
        val name = List(len) { charPool.random(Config.random) }.joinToString("")
        result.add(name)
    }
    return result
}

/**
 * Create assets with random names based on a template
 */
internal fun randomAssets(template: Asset, nAssets: Int): SortedSet<Asset> {
    val uniqueNames = randomNames(nAssets, 5)
    val assets = uniqueNames.map {
        val symbol = template.symbol.format(it)
        template.copy(symbol = symbol)
    }
    return assets.toSortedSet()
}