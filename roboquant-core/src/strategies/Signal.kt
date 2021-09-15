package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.MarketOrder

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
 * @property takeProfit An optional take profit price in the same currency as the asset
 * @property stopLoss An optional stop loss price in the same currency as the asset
 * @property probability Optional the probability (value between 0.0 and 1.0) that the rating is correct.
 * @property source Optional source of the signal, like the strategy name
 * @constructor Create a new Signal
 */
class Signal(
    val asset: Asset,
    val rating: Rating,
    val takeProfit: Double = Double.NaN,
    val stopLoss: Double = Double.NaN,
    val probability: Double = Double.NaN,
    val source: String = ""
) {

    /**
     * Create a new signal and based on the strategy that created it
     */
    constructor(asset: Asset, rating: Rating, strategy: Strategy) : this(asset, rating, source = "$strategy")

    /**
     * Does this signal conflict with another signal? Two signals conflict if they contain the same asset but opposite
     * ratings. So one has a positive outlook and the other one is  negative.
     *
     * @param other
     */
    fun conflicts(other: Signal) = asset == other.asset && rating.conflicts(other.rating)

    fun toMarketOrder(qty: Double) = MarketOrder(asset, qty, tag = source)

    fun toLimitOrder(qty: Double) : LimitOrder {
        require( ! takeProfit.isNaN()) { "Cannot create limit order since no take profit has been provided"}
        return LimitOrder(asset, qty, takeProfit, tag = source)
    }

}

/**
 * Resolve conflicting signals. For many strategies this might not be necessary since there is only 1 signal per
 * asset, but as strategies are combined, this issue might pop up.
 *
 * Currently, the following rules are supported:
 *
 * - NONE: don't do anything
 * - FIRST: the first found signal for an asset will be returned
 * - LAST: the last found signal for an asset will be returned
 * - NO_CONFLICT: if there are conflicting signals, nothing will be returned for that asset
 *
 * @param rule
 * @return the filtered list of signals
 */
fun List<Signal>.resolve(rule: String = "FIRST"): List<Signal> {
    if (size < 2) return this

    return when (rule) {
        "NONE" -> this
        "FIRST" -> distinctBy { it.asset }
        "LAST" -> asReversed().distinctBy { it.asset }.asReversed()
        "NO_CONFLICTS" -> filter { none { f -> f.conflicts(it) } }
        else -> {
            throw Exception("Unknown resolve rule $rule")
        }
    }
}
