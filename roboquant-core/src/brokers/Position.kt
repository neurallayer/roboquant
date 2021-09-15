package org.roboquant.brokers

import org.roboquant.common.Asset
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Position of an asset in the portfolio. This implementation makes no assumptions about the asset class, so it supports
 * any type of asset class from stocks and options to crypto currencies.
 *
 * TODO: possible move to an read-only data structure for Position
 *
 * @property asset the asset
 * @property quantity volume of the asset, not including any contract multiplier defined by the asset
 * @property cost average cost for an asset, in the currency denoted by the asset
 * @property price Last known market price for this asset
 * @property lastUpdate When was the market price last updated
 * @constructor Create a new Position
 */
data class Position(
    val asset: Asset,
    var quantity: Double,
    var cost: Double = 0.0,
    var price: Double = cost,
    var lastUpdate: Instant = Instant.MIN
) {


    /**
     * Total size of a position is the position quantity times the asset multiplier. For many asset classes the
     * multiplier will be 1, but for example for option contracts it will often be 100
     */
    val totalSize: Double
        get() = quantity * asset.multiplier

    companion object Factory {
        /**
         * Create an empty position for the provided asset
         *
         * @param asset the asset
         * @return the empty position
         */
        fun empty(asset: Asset): Position = Position(asset, 0.0, 0.0)
    }


    /**
     * Update the current position with a new position and return the P&L realized by this update.
     *
     * @param position The new position
     * @return the realized P&L that results from this update.
     */
    fun update(position: Position): Double {
        val newQuantity = quantity + position.quantity
        var pnl = 0.0

        // Check if we need to update the average price and pnl
        when {
            quantity.sign != newQuantity.sign -> {
                pnl = totalSize * (position.cost - cost)
                cost = position.cost
            }

            newQuantity.absoluteValue > quantity.absoluteValue -> {
                cost = (cost * quantity + position.cost * position.quantity) / newQuantity
            }

            else -> {
                pnl = position.quantity * asset.multiplier * (cost - position.cost)
            }
        }

        quantity = newQuantity
        return pnl
    }

    /**
     * Is this a short position
     */
    val short: Boolean
        get() = quantity < 0

    /**
     * is this a long position
     */
    val long: Boolean
        get() = quantity > 0

    /**
     * Is this an open position
     */
    val open: Boolean
        get() = quantity != 0.0

    /**
     * The unrealized profit & loss for this position based on avg cost and last known market price,
     * in the currency denoted by the asset
     *
     * @return the pnl amount
     */
    val pnl: Double
        get() = totalSize * (price - cost)


    /**
     * The total value for this position based on last known market price, in the currency denoted by the asset. Please
     * note that short positions will return a negative value.
     *
     * @return the value amount
     */
    val value: Double
        get() = totalSize * price

    /**
     * The gross exposure for this position based on last known market price, in the currency denoted by the asset.
     *
     * The difference with the [value] property is that short positions also result in a positive exposure.
     *
     * @return
     */
    val exposure: Double
        get() = totalSize.absoluteValue * price


    /**
     * Total cost of this position, in the currency denoted by the asset. Please note that short positions will return
     * a negative cost.
     *
     * @return
     */
    val totalCost: Double
        get() = totalSize * cost


}

