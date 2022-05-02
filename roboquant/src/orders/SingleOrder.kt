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

package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.common.Size
import java.math.BigDecimal

/**
 * SingleOrder types are plain non-combined orders with a pre-defined quantity and Time in Force policy. Many well-known
 * order types fall under this category, like Market-, Limit- and Trail-orders.
 *
 * @property size the size of the order
 * @property tif the Time In Force policy to use
 */
abstract class SingleOrder(asset: Asset, val size: Size, val tif: TimeInForce, id: Int, tag: String = "") :
    Order(asset, id, tag) {


    init {
        require(size.nonzero) { "Orders require a non zero size" }
    }

    /**
     * Returns true if this is a buy order, false otherwise.
     */
    val buy
        get() = size > 0.0

    /**
     * Returns true if this is a sell order, false otherwise.
     */
    val sell
        get() = size < 0.0

    /**
     * Returns the direction of the order, 1 being BUY and -1 being SELL
     */
    val direction
        get() = if (buy) 1 else -1

}

/**
 * Buy or sell an asset at the market’s current best available price. A market order typically ensures
 * an execution, but it does not guarantee a specified price.
 *
 * @property asset
 * @property size
 * @property tif
 * @property id
 * @constructor Create new Market order
 */
class MarketOrder(
    asset: Asset,
    size: Size,
    tif: TimeInForce = GTC(),
    id: Int = nextId(),
    tag: String = ""
) : SingleOrder(asset, size, tif, id, tag) {

    constructor(asset: Asset, quantity: Number) : this(asset, Size(BigDecimal.valueOf(quantity.toDouble())))

    override fun info() = sortedMapOf("quantity" to size, "tif" to tif)

}

/**
 *  Buy or sell an asset with a restriction on the maximum price to be paid or the minimum price
 *  to be received. If the order is filled, it will only be at the specified limit price or better.
 *  However, there is no assurance of execution.
 *
 * @property asset
 * @property size
 * @property limit
 * @property tif
 * @property id
 * @constructor Create empty Limit order
 */
class LimitOrder(
    asset: Asset,
    size: Size,
    val limit: Double,
    tif: TimeInForce = GTC(),
    id: Int = nextId(),
    tag: String = ""
) : SingleOrder(asset, size, tif, id, tag) {

    override fun info() = sortedMapOf("quantity" to size, "limit" to limit, "tif" to tif)

}

/**
 * Stop order
 *
 * @property asset
 * @property size
 * @property stop
 * @property tif
 * @property id
 * @constructor Create empty Stop order
 */
class StopOrder(
    asset: Asset,
    size: Size,
    val stop: Double,
    tif: TimeInForce = GTC(),
    id: Int = nextId(),
    tag: String = ""
) : SingleOrder(asset, size, tif, id, tag) {

    override fun info() = sortedMapOf("quantity" to size, "stop" to stop, "tif" to tif)
}

/**
 * Stop limit order
 *
 * @property asset
 * @property size
 * @property stop
 * @property limit
 * @property tif
 * @property id
 * @constructor Create empty Stop limit order
 */
class StopLimitOrder(
    asset: Asset,
    size: Size,
    val stop: Double,
    val limit: Double,
    tif: TimeInForce = GTC(),
    id: Int = nextId(),
    tag: String = ""
) : SingleOrder(asset, size, tif, id, tag) {

    override fun info() = sortedMapOf("quantity" to size, "stop" to stop, "limit" to limit, "tif" to tif)
}

/**
 * Trail order
 *
 * @property asset
 * @property size
 * @property trailPercentage
 * @property tif
 * @property id
 * @constructor Create empty Trail order
 */
open class TrailOrder(
    asset: Asset,
    size: Size,
    val trailPercentage: Double,
    tif: TimeInForce = GTC(),
    id: Int = nextId(),
    tag: String = ""
) : SingleOrder(asset, size, tif, id, tag) {

    init {
        require(trailPercentage > 0.0) { "trailPrecentage should be a positive value" }
    }

    override fun info() = sortedMapOf("quantity" to size, "trailPercentage" to trailPercentage, "tif" to tif)

}

/**
 * Trail limit order
 *
 * example: We want to sell 25 stocks of XYZ when it reaches 5% below its high with a limit price of -1 below that high.
 *
 * val order = TrailLimitOrder(Asset("XYZ"), -25, 0.05, -1.0)
 *
 * @property asset
 * @property size
 * @property trailPercentage trailing percentage to be used to calculate the stop value
 * @property limitOffset offset for the limit compared to the stop value, negative value being a lower limit
 * @property tif
 * @property id
 * @constructor Create new Trail limit Order
 */
class TrailLimitOrder(
    asset: Asset,
    size: Size,
    trailPercentage: Double,
    val limitOffset: Double,
    tif: TimeInForce = GTC(),
    id: Int = nextId(),
    tag: String = ""
) : TrailOrder(asset, size, trailPercentage, tif, id, tag) {

    override fun info() = sortedMapOf(
        "quantity" to size,
        "trailPercentage" to trailPercentage,
        "limitOffset" to limitOffset,
        "tif" to tif
    )
}
