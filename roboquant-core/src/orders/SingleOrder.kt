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


/**
 * SingleOrder types are plain non-combined orders with a pre-defined quantity and Time in Force policy. Many well-known
 * order types fall under this category, like Market-, Limit- and Trail-orders.
 */
abstract class SingleOrder(asset: Asset, val quantity: Double,  val tif: TimeInForce, id: String ) : Order(asset, id)  {


    init {
        require(quantity != 0.0) { "Orders require a non zero quantity"}
    }

    /**
     * Is this a sell order
     */
    val sell
        get() = quantity < 0.0

    /**
     * Is this a buy order
     */
    val buy
        get() = quantity > 0.0

    /**
     * Direction of the order, 1 being BUY and -1 being SELL
     */
    val direction
        get() = if (quantity > 0.0) 1 else -1

}


/**
 * Buy or sell an asset at the market’s current best available price. A market order typically ensures
 * an execution, but it does not guarantee a specified price.
 *
 * @property asset
 * @property quantity
 * @property tif
 * @property id
 * @constructor Create new Market order
 */
class MarketOrder(
    asset: Asset,
    quantity: Double,
    tif: TimeInForce = GTC(),
    id: String = nextId(),
) : SingleOrder(asset, quantity, tif, id)

/**
 *  Buy or sell an asset with a restriction on the maximum price to be paid or the minimum price
 *  to be received. If the order is filled, it will only be at the specified limit price or better.
 *  However, there is no assurance of execution.
 *
 * @property asset
 * @property quantity
 * @property limit
 * @property tif
 * @property id
 * @constructor Create empty Limit order
 */
class LimitOrder(
    asset: Asset,
    quantity: Double,
    val limit: Double,
    tif: TimeInForce = GTC(),
    id: String = nextId(),
) : SingleOrder(asset, quantity, tif, id)




/**
 * Stop order
 *
 * @property asset
 * @property quantity
 * @property stop
 * @property tif
 * @property id
 * @constructor Create empty Stop order
 */
class StopOrder(
    asset: Asset,
    quantity: Double,
    val stop: Double,
    tif: TimeInForce = GTC(),
    id: String = nextId(),
) : SingleOrder(asset, quantity, tif, id)


/**
 * Stop limit order
 *
 * @property asset
 * @property quantity
 * @property stop
 * @property limit
 * @property tif
 * @property id
 * @constructor Create empty Stop limit order
 */
class StopLimitOrder(
    asset: Asset,
    quantity: Double,
    val stop: Double,
    val limit: Double,
    tif: TimeInForce = GTC(),
    id: String = nextId(),
) : SingleOrder(asset, quantity, tif, id)

/**
 * Trail order
 *
 * @property asset
 * @property quantity
 * @property trailPercentage
 * @property tif
 * @property id
 * @constructor Create empty Trail order
 */
open class TrailOrder(
    asset: Asset,
    quantity: Double,
    val trailPercentage: Double,
    tif: TimeInForce = GTC(),
    id: String = nextId(),
) : SingleOrder(asset, quantity, tif, id) {

    init {
        require(trailPercentage > 0.0) {"trailPrecentage should be a positive value"}
    }


}

/**
 * Trail limit order
 *
 * example: We want to sell 25 stocks of XYZ when it reaches 5% below its high with a limit price of -1 below that high.
 *
 * val order = TrailLimitOrder(Asset("XYZ"), -25, 0.05, -1.0)
 *
 * @property asset
 * @property quantity
 * @property trailPercentage trailing percentage to be used to calculate the stop value
 * @property limitOffset offset for the limit compared to the stop value, negative value being a lower limit
 * @property tif
 * @property id
 * @constructor Create new Trail limit Order
 */
class TrailLimitOrder(
    asset: Asset,
    quantity: Double,
    trailPercentage: Double,
    val limitOffset: Double,
    tif: TimeInForce = GTC(),
    id: String = nextId(),
) : TrailOrder(asset, quantity, trailPercentage, tif, id) {




}

