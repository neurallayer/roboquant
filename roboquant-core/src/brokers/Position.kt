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

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Cash
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * The 3 possible position changes, that impact PNL and avg price calculations
 */
enum class PositionChange {

    // Go from a long to short position or visa verse
    DIRECTION,

    // Increase the position size, either long or short
    INCREASE,

    // Decrease position size, either long or short
    DECREASE

}

/**
 * Position of an asset in the portfolio. This implementation makes no assumptions about the asset class, so it supports
 * any type of asset class from stocks and options to crypto currencies.
 *
 * @property asset the asset
 * @property size size of the position, not including any contract multiplier defined at asset level
 * @property avgPrice average price paid, in the currency denoted by the asset
 * @property spotPrice Last known market price for this asset
 * @property lastUpdate When was the market price last updated
 * @constructor Create a new Position
 */
data class Position(
    val asset: Asset,
    val size: Double,
    val avgPrice: Double = 0.0,
    val spotPrice: Double = avgPrice,
    val lastUpdate: Instant = Instant.MIN
) {

    /**
     * Total size of a position is the position size times the asset multiplier. For many asset classes the
     * multiplier will be 1, but for example for option contracts it will often be 100
     */
    val totalSize: Double
        get() = size * asset.multiplier

    /**
     * The currency of this position, aka the currency of the underlying asset
     */
    val currency
        get() = asset.currency

    companion object Factory {

        /**
         * Create an empty position for the provided [asset] and return this.
         */
        fun empty(asset: Asset): Position = Position(asset, 0.0, 0.0, 0.0)
    }


    fun getPositionChange(change: Position) : PositionChange {
        return when {
            size.sign == change.size.sign -> PositionChange.INCREASE
            size.absoluteValue - change.size.absoluteValue > 0 -> PositionChange.DECREASE
            else -> PositionChange.DIRECTION
        }
    }

    operator fun plus(p: Position) : Position {

        val newQuantity = size + p.size

        return when {
            size.sign != newQuantity.sign -> p.copy(size = newQuantity)

            newQuantity.absoluteValue > size.absoluteValue -> {
                val newAvgPrice = (avgPrice * size + p.avgPrice * p.size) / newQuantity
                p.copy(size = newQuantity, avgPrice = newAvgPrice)
            }

            else -> p.copy(size = newQuantity, avgPrice = avgPrice)
        }

    }

    /**
     * How much PNL would be realized when [update] a position. This doesn't change the position itself, just
     * calculates the potential realized PNL.
     */
    fun realizedPNL(update: Position) : Double {
        val newQuantity = size + update.size

        return when {
            size.sign != newQuantity.sign -> totalSize * (update.avgPrice - avgPrice)
            newQuantity.absoluteValue > size.absoluteValue -> 0.0
            else -> update.totalSize * (avgPrice - update.avgPrice)
        }

    }

    /**
     * Is this an empty position
     */
    fun isEmpty(): Boolean = size == 0.0

    /**
     * Is this a short position
     */
    val short: Boolean
        get() = size < 0

    /**
     * is this a long position
     */
    val long: Boolean
        get() = size > 0

    /**
     * Is this an open position
     */
    val open: Boolean
        get() = size != 0.0

    /**
     * The unrealized profit & loss for this position based on the [avgPrice] and last known market [spotPrice],
     * in the currency denoted by the asset
     */
    val unrealizedPNL: Amount
        get() = Amount(asset.currency, totalSize * (spotPrice - avgPrice))


    /**
     * The total value for this position based on last known spot price, in the currency denoted by the asset.
     * Short positions will typically return a negative value.
     */
    val marketValue: Amount
        get() = Amount(asset.currency,totalSize * spotPrice)

    /**
     * The gross exposure for this position based on last known market price, in the currency denoted by the asset.
     * The difference with the [marketValue] property is that short positions also result in a positive exposure.
     */
    val exposure: Amount
        get() = Amount(asset.currency, totalSize.absoluteValue * spotPrice)


    /**
     * The total cost of this position, in the currency denoted by the asset. Short positions will typically return
     * a negative value.
     */
    val totalCost: Amount
        get() =  Amount(asset.currency,totalSize * avgPrice)


}

val Collection<Position>.value : Cash
        get() {
            val result = Cash()
            for (position in this) result.deposit(position.marketValue)
            return result
        }
