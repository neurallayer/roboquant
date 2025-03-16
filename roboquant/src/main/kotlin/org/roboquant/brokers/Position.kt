/*
 * Copyright 2020-2024 Neural Layer
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
 * @property size size of the position, not including any contract multiplier defined at asset contract level
 * @property avgPrice average price paid, in the currency denoted by the asset
 * @property mktPrice last known market price for this asset
 * @property lastUpdate When was this position last updated, typically with a new market price
 * @constructor Create a new Position
 */
data class Position(
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
         * Create an empty position
         */
        fun empty(): Position = Position(Size.ZERO, 0.0, 0.0)
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

}

/**
 * Get all the long positions for a collection of positions
 */
val Map<Asset, Position>.long: Map<Asset, Position>
    get() = filterValues { it.long }

/**
 * Get all the short positions for a collection of positions
 */
val Map<Asset, Position>.short: Map<Asset, Position>
    get() = filterValues { it.short }



fun Map<Asset, Position>.marketValue(): Wallet {
    val result = Wallet()
    for ((asset, position) in this) {
        val positionValue = asset.value(position.size, position.mktPrice)
        result.deposit(positionValue)
    }
    return result
}


fun Map<Asset, Position>.pnl(): Wallet {
    val result = Wallet()
    for ((asset, position) in this) {
        val positionValue = asset.value(position.size, position.mktPrice - position.avgPrice)
        result.deposit(positionValue)
    }
    return result
}


fun Map<Asset, Position>.exposure(): Wallet {
    val result = Wallet()
    for ((asset, position) in this) {
        val positionValue = asset.value(position.size.absoluteValue, position.mktPrice)
        result.deposit(positionValue)
    }
    return result
}
