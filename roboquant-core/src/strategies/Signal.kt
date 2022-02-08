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

package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.MarketOrder

/**
 * Is the signal generated meant for entry, exit or both.
 */
enum class SignalType {

    /**
     * Used only to enter of a position, meaning to open or increase a position size
     */
    ENTRY,

    /**
     * Used only to exit a position, meaning to close or decrease a position size
     */
    EXIT,

    /**
     * Can be used for entry or exit of a position
     */
    BOTH
}


/**
 * Signal provides a [Rating] for an [Asset] and is typically created by a strategy.
 *
 * Besides, the asset and rating, a signal can also optionally provide the following information:
 *
 * - Take profit price for that asset
 * - Stop loss price for that asset
 * - The probability, or in other words how sure the strategy is about the signal
 *
 * It depends on the policy how these signals are translated into actual orders, but possible scenarios are:
 *
 *  - A BUY rating results in going long for that asset. A SELL rating results in going short
 *  - The target price will become an additional take profit order
 *  - The probability determines the stake (volume) of the order. The more confident, the larger the stake
 *
 * @property asset The asset for which this rating applies
 * @property rating The rating for the asset
 * @property type The type of signal, entry, exit or both. [SignalType.BOTH] is the default
 * @property takeProfit An optional take profit price in the same currency as the asset
 * @property stopLoss An optional stop loss price in the same currency as the asset
 * @property probability Optional the probability (value between 0.0 and 1.0) that the rating is correct.
 * @property source Optional the source of the signal, like the strategy name
 * @constructor Create a new Signal
 */
class Signal(
    val asset: Asset,
    val rating: Rating,
    val type: SignalType = SignalType.BOTH,
    val takeProfit: Double = Double.NaN,
    val stopLoss: Double = Double.NaN,
    val probability: Double = Double.NaN,
    val source: String = ""
) {

    /**
     * Does this signal allow to function as a exit signal, so to close or decrease a position
     */
    val exit
        get() = type === SignalType.EXIT || type === SignalType.BOTH

    /**
     * Does this signal allow to function as a entry signal, so to open or increase a position
     */
    val entry
        get() = type === SignalType.ENTRY || type === SignalType.BOTH

    /**
     * Create a new signal and based on the strategy that created it
     */
    constructor(asset: Asset, rating: Rating, strategy: Strategy) : this(asset, rating, source = "$strategy")

    /**
     * Does this signal conflict with an [other] signal. Two signals conflict if they contain the same asset but opposite
     * ratings. So one signal has a positive outlook and the other one is negative.
     */
    fun conflicts(other: Signal) = asset == other.asset && rating.conflicts(other.rating)

    fun toMarketOrder(qty: Double) = MarketOrder(asset, qty, tag = source)

    fun toLimitOrder(qty: Double): LimitOrder {
        require(!takeProfit.isNaN()) { "Cannot create limit order since no take profit has been provided" }
        return LimitOrder(asset, qty, takeProfit, tag = source)
    }

}
