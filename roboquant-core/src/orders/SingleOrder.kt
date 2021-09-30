package org.roboquant.orders

import org.roboquant.brokers.sim.Execution
import org.roboquant.common.Asset
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.math.round


/**
 * Abstract base class for all different types of single orders.
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


    init {
        require(quantity != 0.0) { "Cannot create an order with zero quantity" }
    }

    /**
     * How much of the order is filled
     */
    var fill = 0.0

    /**
     * How much of the order quantity is remaining
     */
    val remaining
        get() = quantity - fill

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

    /**
     * The quantity as a rounded absolute integer. By default roboquant support fractional orders and makes no assumption
     * about the order size. But some brokers support only whole orders.
     */
    val absInt: Int
        get() = round(quantity.absoluteValue).toInt()

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

    override fun execute(price: Double, time: Instant): List<Execution> {
        place(price, time)
        val qty = fill(price)

        if (tif.isExpired(placed, time, qty, quantity)) {
            status = OrderStatus.EXPIRED
            return listOf()
        }

        return if (qty != 0.0) {
            fill += qty
            if (remaining == 0.0) status = OrderStatus.COMPLETED
            listOf(Execution(asset, qty, price))
        } else listOf()
    }

    protected abstract fun fill(price: Double): Double

    /**
     * Did (part of) the order get already filled
     */
    val executed
        get() = fill != 0.0


    /**
     * Calculates the remaining value of the order given the market price provided. The order value is expressed
     * in the currency of the underlying asset. Note that sell orders will result in a negative value.
     *
     * @param price Price to use for calculating the remaining order value
     */
    override fun getValue(price: Double) = asset.multiplier * remaining * price

    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} qty=$quantity tif=$tif"
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

    override fun fill(price: Double): Double {
        return remaining
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


    override fun fill(price: Double): Double {
        if (buy && price < limit) return quantity
        if (sell && price > limit) return quantity
        return 0.0
    }

    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} limit=$limit qty=$quantity tif=$tif"
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

    protected var triggered = false

    override fun clone(): StopOrder {
        val result = StopOrder(asset, quantity, stop, tif, tag)
        copyTo(result)
        return result
    }

    override fun fill(price: Double): Double {
        if (! triggered) {
            if ((sell && price <= stop) || (buy && price >= stop)) triggered = true
        }

        return if (triggered) remaining else 0.0
    }


    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} stop=$stop qty=$quantity tif=$tif"
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


    override fun fill(price: Double): Double {
        if (triggered) {
            if ((sell && price <= stop) || (buy && price >= stop)) triggered = true
        }

        if (triggered) {
            if ((sell && price <= limit) || buy && price >= limit) return remaining
        }

        return 0.0
    }


    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} stop=$stop limit=$limit qty=$quantity tif=$tif"
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

    protected var triggered = false

    private var correction: Double = if (buy) (1.0 + trail) else (1.0 - trail)
    private var stop: Double = if (buy) Double.MAX_VALUE else Double.MIN_VALUE

    companion object {
        fun from(order: SingleOrder, trail: Double = 0.01) = TrailOrder(order.asset, -order.quantity, trail)
    }

    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} trail=$trail qty=$quantity tif=$tif"
    }

    protected fun updateStop(price: Double) {
        if (status == OrderStatus.INITIAL) {
            val newStop = price * correction
            if (buy && newStop < stop) stop = newStop
            if (sell && newStop > stop) stop = newStop
            if ((sell && price <= stop) || (buy && price >= stop)) triggered = true
        }
    }

    override fun fill(price: Double): Double {
        updateStop(price)
        return if (triggered) remaining else 0.0
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

    var limit = Double.NaN

    companion object {
        fun from(order: SingleOrder, trail: Double, limit: Double) =
            TrailLimitOrder(order.asset, -order.quantity, trail, limit)
    }

    override fun toString(): String {
        return "${this::class.simpleName} asset=${asset.symbol} trail=$trail limit=$limit qty=$quantity tif=$tif"
    }

    override fun fill(price: Double): Double {
        updateStop(price)
        if (triggered) {
            if (limit.isNaN()) limit = price + limitOffset
            if ((sell && price <= limit) || buy && price >= limit) {
                return remaining
            }
        }
        return 0.0
    }

    override fun clone(): TrailLimitOrder {
        val result = TrailLimitOrder(asset, quantity, trail, limit, tif, tag)
        copyTo(result)
        return result
    }

}