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

package org.roboquant.brokers

import org.roboquant.common.*
import java.time.Instant

/**
 * This class holds the position of an asset in the portfolio. The implementation makes no assumptions about the
 * asset class, so it supports any type of asset class, ranging from stocks and options to cryptocurrencies.
 *
 * Position instances are immutable, so updating a position requires creating a new instance. The actual [size] of the
 * position is precise (doesn't lose precision like is the case with double) using the [Size] class.
 *
 * @property asset the asset of the position
 * @property size size of the position, not including any contract multiplier defined at asset contract level
 * @property avgPrice average price paid, in the currency denoted by the asset
 * @property mktPrice last known market price for this asset
 * @property lastUpdate When was this position last updated, typically with a new market price
 * @constructor Create a new Position
 */
data class Position(
    val asset: Asset,
    val size: Size,
    val avgPrice: Double = 0.0,
    val mktPrice: Double = avgPrice,
    val lastUpdate: Instant = Instant.MIN
) {

    /**
     * @suppress
     */
    companion object {

        /**
         * Create an empty position for the provided [asset] and return this.
         */
        fun empty(asset: Asset): Position = Position(asset, Size.ZERO, 0.0, 0.0)
    }

    /**
     * Add another position [p] to this position and return the result.
     */
    operator fun plus(p: Position): Position {

        val newSize = size + p.size

        return when {
            size.sign != newSize.sign -> p.copy(size = newSize)

            newSize.absoluteValue > size.absoluteValue -> {
                val newAvgPrice = (size.toDouble() * avgPrice + p.size.toDouble() * p.avgPrice) / newSize.toDouble()
                p.copy(size = newSize, avgPrice = newAvgPrice)
            }

            else -> p.copy(size = newSize, avgPrice = avgPrice)
        }

    }

    /**
     * How much PNL would be realized when an [update] to a position would happen.
     * This method doesn't modify the position itself, it just calculates the potential realized PNL.
     */
    fun realizedPNL(update: Position): Amount {
        val newSize = size + update.size
        return when {
            size.sign != newSize.sign -> asset.value(size, update.avgPrice - avgPrice)
            newSize.absoluteValue > size.absoluteValue -> Amount(asset.currency, 0.0)
            else -> asset.value(update.size, avgPrice - update.avgPrice)
        }
    }

    /**
     * Returns true if this is a closed position, false otherwise
     */
    val closed: Boolean
        get() = size.iszero

    /**
     * IReturns true if this is a short position, false otherwise
     */
    val short: Boolean
        get() = size.isNegative

    /**
     * Returns true if this is a long position, false otherwise
     */
    val long: Boolean
        get() = size.isPositive

    /**
     * Returns true if this is an open position, false otherwise
     */
    val open: Boolean
        get() = !size.iszero

    /**
     * Returns the unrealized profit & loss for this position based on the [avgPrice] and last known market [mktPrice],
     * in the currency denoted by the asset
     */
    val unrealizedPNL: Amount
        get() = asset.value(size, mktPrice - avgPrice)

    /**
     * The total market value for this position based on last known market price, in the currency denoted by the asset.
     * Short positions will typically return a negative value.
     */
    val marketValue: Amount
        get() = asset.value(size, mktPrice)

    /**
     * The gross exposure for this position based on last known market price, in the currency denoted by the asset.
     * The difference with the [marketValue] property is that short positions also result in a positive exposure.
     *
     * Please note that this is the position exposure and doesn't take into account (stop-) orders that might limit
     * the exposure.
     */
    val exposure: Amount
        get() = marketValue.absoluteValue

    /**
     * The total cost of this position, in the currency denoted by the asset. Short positions will typically return
     * a negative value.
     */
    val totalCost: Amount
        get() = asset.value(size, avgPrice)

    /**
     * Would the overall position size be reduced given the provided [additional] size
     */
    fun isReduced(additional: Size): Boolean = (size + additional).absoluteValue < size.absoluteValue

}

/**
 * Return the total market value for a collection of positions.
 */
val Collection<Position>.marketValue: Wallet
    get() {
        return sumOf { it.marketValue }
    }

/**
 * Return the total exposure for a collection of positions.
 */
val Collection<Position>.exposure: Wallet
    get() {
        return sumOf { it.exposure }
    }

/**
 * Return the difference between these positions and a target set of positions.
 */
fun Collection<Position>.diff(target: Collection<Position>): Map<Asset, Size> {
    val result = mutableMapOf<Asset, Size>()

    for (position in target) {
        val targetSize = position.size
        val sourceSize = getPosition(position.asset).size
        val value = targetSize - sourceSize
        if (!value.iszero) result[position.asset] = value
    }

    for (position in this) {
        if (position.asset !in result) result[position.asset] = -position.size
    }

    return result
}

/**
 * Get the set of distinct assets for a collection of positions
 */
val Collection<Position>.assets: Set<Asset>
    get() = map { it.asset }.distinct().toSet()

/**
 * Get all the long positions for a collection of positions
 */
val Collection<Position>.long: List<Position>
    get() = filter { it.long }

/**
 * Get all the short positions for a collection of positions
 */
val Collection<Position>.short: List<Position>
    get() = filter { it.short }

/**
 * Return the first position found for an [asset]. If no position is found, an empty position will be returned.
 */
fun Collection<Position>.getPosition(asset: Asset): Position {
    return firstOrNull { it.asset == asset } ?: Position.empty(asset)
}

/**
 * Return the total unrealized PNL for a collection of positions
 */
val Collection<Position>.unrealizedPNL: Wallet
    get() = sumOf { it.unrealizedPNL }

/**
 * Return the required sizes per asset to close the open positions. This method doesn't close the actual open positions,
 * just provides the information to do so as a Pair<Asset, Size>.
 * ```
 * val orders = positions.close().map { MarketOrder(it.key, it.value) }
 * ```
 */
val Collection<Position>.closeSizes: Map<Asset, Size>
    get() = diff(emptyList())

/**
 * Return the collection as table
 */
fun Collection<Position>.lines(): List<List<Any>> {
    val lines = mutableListOf<List<Any>>()
    lines.add(
        listOf(
            "symbol",
            "ccy",
            "size",
            "entry price",
            "mkt price",
            "mkt value",
            "unrlzd p&l"
        )
    )

    for (v in this) {
        val c = v.asset.currency
        val pos = v.size
        val avgPrice = Amount(c, v.avgPrice).formatValue()
        val price = Amount(c, v.mktPrice).formatValue()
        val value = v.marketValue.formatValue()
        val pnl = Amount(c, v.unrealizedPNL.value).formatValue()
        lines.add(listOf(v.asset.symbol, c.currencyCode, pos, avgPrice, price, value, pnl))
    }
    return lines
}

/**
 * Create a [Summary] of this portfolio that contains an overview of the open positions.
 */
@JvmName("summaryPositions")
fun Collection<Position>.summary(name: String = "positions"): Summary {
    val s = Summary(name)

    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val lines = lines()
        return lines.summary(name)
    }

    return s
}
