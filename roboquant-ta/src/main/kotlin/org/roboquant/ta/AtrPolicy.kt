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

package org.roboquant.ta

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.Size
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.orders.*
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.FlexPolicyConfig
import org.roboquant.strategies.Signal


/**
 * This policy is a subclass of [FlexPolicy] and uses ATR (Average True Range) to:
 *
 * - Create a [BracketOrder] with ATR based take profit and stop loss values
 * - Optionally reduce the sizing based on the ATR.
 *
 * The bracket order will by default have the following contained orders:
 *
 * - The entry order is a [MarketOrder]
 * - The takeProfit is a [LimitOrder] with the limit set at an offset of the current price of [atrProfit] * ATR
 * - The stopLoss is a [StopOrder] with the stop set at an offset of the current price of [atrLoss] * ATR
 *
 * But you can subclass this class and overwrite the corresponding methods like getEntryOrder(...)
 *
 * The implementation will take care of ensuring offsets are done correctly based on a BUY or SELL order.
 *
 * @property atrPeriod period to use for calculating the atr, default is 20
 * @property atrProfit the ATR multiplier for profit orders to use
 * @property atrLoss the ATR multiplier for stop loss orders to use
 * @property atRisk max percentage of the order value that can be at risk.
 * If null, no risk-based sizing will be applied.
 */
open class AtrPolicy(
    private val atrPeriod: Int = 20,
    private val atrProfit: Double = 4.0,
    private val atrLoss: Double = 2.0,
    private val atRisk: Double? = null,
    configure: FlexPolicyConfig.() -> Unit = {}
) : FlexPolicy(configure = configure) {


    init {
        require(atRisk == null || atRisk in 0.0..1.0)
    }

    private val data = AssetPriceBarSeries(atrPeriod + 1) // we need one observation extra
    private val talib = TaLib()

    /**
     * Resize the order size if the value at risk is too large.
     */
    private fun resize(maxSize: Size, atr: Double, price: Double): Size {
        if (atRisk == null) return maxSize
        val risk = atr * atrLoss
        val limit = atRisk * price

        // Reduce the size if risk is bigger than limit
        return if (risk > limit) {
            val newSize = maxSize * limit / risk
            newSize.round(config.fractions)
        } else {
            maxSize
        }
    }

    /**
     * @see FlexPolicy.act
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        data.addAll(event)
        return super.act(signals, account, event)
    }

    /**
     * Calculate the ATR for an [asset].
     */
    private fun getAtr(asset: Asset): Double {
        val serie = data[asset]
        if (serie == null || !serie.isFull()) return Double.NaN
        return talib.atr(serie, atrPeriod)
    }

    /**
     * Return the entry order to use in the bracket order. The default is a [MarketOrder].
     */
    open fun getEntryOrder(asset: Asset, size: Size, atr: Double, price: Double): SingleOrder? =
        MarketOrder(asset, size)

    /**
     * Return the take profit order to use in the bracket order. The default is a [LimitOrder].
     */
    open fun getTakeProfitOrder(asset: Asset, size: Size, atr: Double, price: Double): SingleOrder? {
        val limitPrice = price - (atr * atrProfit * size.sign)
        if (limitPrice <= 0) return null
        return LimitOrder(asset, size, limitPrice)

    }

    /**
     * Return the stop loss order to use in the bracket order. The default is a [StopOrder].
     */
    open fun getStopLossOrder(asset: Asset, size: Size, atr: Double, price: Double): SingleOrder? {
        val stopPrice = price + (atr * atrLoss * size.sign)
        if (stopPrice <= 0) return null
        return StopOrder(asset, size, stopPrice)
    }

    /**
     * @see FlexPolicy.calcSize
     *
     * This implementation adds functionality that if the value at risk is larger than the defined [atRisk] percentage
     * of the total order amount, the size will be reduced accordingly.
     */
    override fun calcSize(amount: Double, signal: Signal, price: Double): Size {
        val maxSize = super.calcSize(amount, signal, price)
        return if (atRisk == null) maxSize else {
            val atr = getAtr(signal.asset)
            if (!atr.isFinite()) Size.ZERO else resize(maxSize, atr, price)
        }
    }

    /**
     * @see FlexPolicy.createOrder
     */
    override fun createOrder(signal: Signal, size: Size, priceAction: PriceAction): Order? {
        val asset = signal.asset

        // Calculate the ATR
        val atr = getAtr(asset)
        if (!atr.isFinite() || atr == 0.0) return null

        // Create the actual orders
        val price = priceAction.getPrice(config.priceType)
        val entry = getEntryOrder(asset, size, atr, price)
        val profit = getTakeProfitOrder(asset, -size, atr, price)
        val loss = getStopLossOrder(asset, -size, atr, price)

        return if (entry != null && profit != null && loss != null)
            BracketOrder(entry, profit, loss)
        else
            null
    }

    /**
     * @see FlexPolicy.reset
     */
    override fun reset() {
        super.reset()
        data.clear()
    }

}
