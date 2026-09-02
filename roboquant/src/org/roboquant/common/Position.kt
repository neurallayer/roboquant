/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.common

/**
 * This class holds the position of an asset in the portfolio. The implementation makes no assumptions about the
 * asset class, so it supports any type of asset class, ranging from stocks and options to cryptocurrencies.
 *
 * Position instances are immutable, so updating a position requires creating a new instance. The actual [size] of the
 * position is precise (doesn't lose precision like is the case with double) using the [Size] class.
 *
 * @property size size of the position, not including any contract multiplier defined at the asset contract level
 * @property avgPrice average price paid, in the currency denoted by the asset
 * @property mktPrice last known market price for this asset
 * @property info optional info for this position, can be used by Brokers
 * @constructor Create a new Position
 */
data class Position(
    val asset: Asset,
    val size: Size,
    val avgPrice: Double = 0.0,
    val mktPrice: Double = avgPrice,
    val info: Map<String, Any> = mapOf()
) {

    /**
     * @suppress
     */
    companion object {

        /**
         * Create an empty position with [size] and [mktPrice] set to 0
         */
        fun empty(asset: Asset): Position = Position(asset,Size.ZERO, mktPrice = 0.0)
    }

    /**
     * Returns true if this is a closed position ([size] == 0), false otherwise
     */
    val closed: Boolean
        get() = size.iszero

    /**
     * Returns true if this is a short position ([size] < 0), false otherwise
     */
    val short: Boolean
        get() = size.isNegative

    /**
     * Returns true if this is a long position ([size] > 0), false otherwise
     */
    val long: Boolean
        get() = size.isPositive

    /**
     * Returns true if this is an open position ([size] != 0), false otherwise
     */
    val open: Boolean
        get() = size.nonzero

    /**
     * Create an order that will close this position.
     * The info from the Position will be included in the Order
     */
    fun closeOrder(limit: Double? = null, tif: TIF = TIF.DAY): Order {
        return Order(asset, - size, limit, tif, info = info)
    }

    fun marketValue(): Amount {
        return asset.value(size, mktPrice)
    }

    fun unrealizedPNL(): Amount {
        return asset.value(size, mktPrice - avgPrice)
    }

}

