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
 * Abstract base class for different types of single orders.
 *
 * An important implementation detail of orders in roboquant is that the sign quantity decides if it is a BUY or SELL
 * order, it doesn't have to be specified separately. So positive quantities are BUY orders and negative quantities are
 * SELL orders.
 *
 * @property asset The asset
 * @property quantity The volume of the order, this doesn't include the contract size
 * @property tif The time in force policy [TimeInForce] to use for this order
 */
abstract class SingleOrder(asset: Asset, var quantity: Double, val tif: TimeInForce, val tag: String) : Order(asset) {

    var fill = 0.0

    init {
        require(quantity != 0.0) { "Cannot create an order with zero quantity" }
    }

    /**
     * Is this a BUY order
     */
    val buy: Boolean
        get() = quantity > 0

    /**
     * Is this a SELL order
     */
    val sell: Boolean
        get() = quantity < 0


    override val remaining
        get() = quantity - fill




    /**
     * Direction => 1 is for *BUY* and -1 is for *SELL*
     */
    val direction: Int
        get() = if (buy) 1 else -1


    override fun clone(): SingleOrder {
        throw NotImplementedError("Concrete subclass of SingleOrder need to override clone() method")
    }

    /**
     * Copy state into the passed object. This is used in the clone function of the subclasses
     *
     * @param result
     */
    protected fun copyTo(result: SingleOrder) {
        super.copyTo(result)
        result.fill = fill
    }


    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} qty=$quantity tif=$tif id=$id status=$status"
    }

}

/**
 * Buy or sell an asset at the market’s current best available price. A market order typically ensures
 * an execution, but it does not guarantee a specified price.
 *
 * @constructor
 *
 * @param quantity
 * @param tif
 */
class MarketOrder(
    asset: Asset,
    quantity: Double,
    tif: TimeInForce = GTC(),
    tag: String = ""
) : SingleOrder(asset, quantity, tif, tag) {

    override fun clone(): MarketOrder {
        val result = MarketOrder(asset, quantity, tif, tag)
        copyTo(result)
        return result
    }


}


/**
 *  Buy or sell an asset with a restriction on the maximum price to be paid or the minimum price
 *  to be received. If the order is filled, it will only be at the specified limit price or better.
 *  However, there is no assurance of execution.
 *
 * @property limit
 * @constructor
 *
 * @param quantity
 * @param tif
 */
class LimitOrder(
    asset: Asset,
    quantity: Double,
    val limit: Double,
    tif: TimeInForce = GTC(),
    tag: String = ""
) : SingleOrder(asset, quantity, tif, tag) {

    override fun clone(): LimitOrder {
        val result = LimitOrder(asset, quantity, limit, tif, tag)
        copyTo(result)
        return result
    }


    override fun toString(): String {
        return "${super.toString()} limit=$limit"
    }
}

/**
 * Buy or sell an asset at the market price once the asset has traded at or through a specified stop price.
 * If the asset reaches the stop price, the order becomes a market order and is filled at the next
 * available market price. If the asset fails to reach the stop price, the order is not executed.
 *
 * @property stop
 * @constructor
 *
 * @param quantity
 * @param tif
 */
open class StopOrder(
    asset: Asset,
    quantity: Double,
    var stop: Double,
    tif: TimeInForce = GTC(),
    tag: String = ""
) : SingleOrder(asset, quantity, tif, tag) {


    override fun clone(): StopOrder {
        val result = StopOrder(asset, quantity, stop, tif, tag)
        copyTo(result)
        return result
    }


    override fun toString(): String {
        return "${super.toString()} stop=$stop"
    }

}

/**
 * Buy or sell an asset at a limit price once the asset has traded at or through a specified stop price.
 * If the asset reaches the stop price, the order becomes a limit order and is only filled at the specified
 * limit price or better. If the asset fails to reach the stop price, the order is not executed.
 *
 * @property stop
 * @property limit
 * @constructor
 *
 * @param quantity
 * @param tif
 */
class StopLimitOrder(
    asset: Asset,
    quantity: Double,
    stop: Double,
    val limit: Double,
    tif: TimeInForce = GTC(),
    tag: String = ""
) : StopOrder(asset, quantity, stop, tif, tag) {

    override fun clone(): StopLimitOrder {
        val result = StopLimitOrder(asset, quantity, stop, limit, tif, tag)
        copyTo(result)
        return result
    }


    override fun toString(): String {
        return "${super.toString()} stop=$stop limit=$limit"
    }
}


/**
 * A trail order is a variation of a [StopOrder] that can be set at a defined percentage or amount away
 * from an asset's current market price. For a long position, the trailing stop loss is below the
 * current market price. For a short position, the trailing stop is above the current market price.
 *
 * A trailing stop is designed to protect gains by enabling a position to remain open and continue to profit as long as
 * the price is moving in the desired direction. The trail order fills if the price changes direction by
 * the specified percentage or amount.
 *
 * @property trail Trail amount as percentage, so 0.01 equals 1%
 * @constructor
 *
 * @param asset
 * @param quantity
 * @param tif
 * @param tag
 */
open class TrailOrder(
    asset: Asset,
    quantity: Double,
    val trail: Double,
    tif: TimeInForce = GTC(),
    tag: String = ""
) : SingleOrder(asset, quantity, tif, tag) {


    companion object {
        fun from(order: SingleOrder, trail: Double = 0.01) = TrailOrder(order.asset, -order.quantity, trail)
    }

    override fun toString(): String {
        return "${super.toString()} trail=$trail"
    }


    override fun clone(): TrailOrder {
        val result = TrailOrder(asset, quantity, trail, tif, tag)
        copyTo(result)
        return result
    }

}


/**
 * A trail limit order is a variation of a [StopLimitOrder] that can be set at a defined percentage or amount away
 * from an asset's current market price. For a long position, the trailing stop loss is below the
 * current market price. For a short position, the trailing stop is above the current market price.
 *
 *
 * @constructor
 *
 * @param asset
 * @param quantity
 * @param tif
 * @param tag
 */
class TrailLimitOrder(
    asset: Asset,
    quantity: Double,
    trail: Double,
    var limitOffset: Double,
    tif: TimeInForce = GTC(),
    tag: String = ""
) : TrailOrder(asset, quantity, trail, tif, tag) {


    companion object {
        fun from(order: SingleOrder, trail: Double, limit: Double) =
            TrailLimitOrder(order.asset, -order.quantity, trail, limit)
    }

    override fun toString(): String {
        return "${super.toString()} trail=$trail limit=$limitOffset"
    }

    override fun clone(): TrailLimitOrder {
        val result = TrailLimitOrder(asset, quantity, trail, limitOffset, tif, tag)
        copyTo(result)
        return result
    }

}