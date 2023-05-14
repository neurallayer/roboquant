/*
 * Copyright 2020-2023 Neural Layer
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
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.Signal
import kotlin.math.absoluteValue



/**
 * This policy uses ATR (Average True Range) to
 * - create a [BracketOrder] with ATR based take profit and stop loss values
 * - optionally reduce the sizing based on the ATR.
 *
 * The bracket order will have the following contained orders:
 *
 * - entry order is a [MarketOrder]
 * - takeProfit is a [LimitOrder] with the limit set at an offset of the current price of [atrProfit] * ATR
 * - stopLoss is a [StopOrder] with the stop set at an offset of the current price of [atrLoss] * ATR
 *
 * The implementation will take care ensuring offsets are done correctly based on a BUY or SELL order.
 *
 * @property atrPeriod
 * @property atrProfit
 * @property atrLoss
 * @property atRisk max percentage of the orderPercentage that can be at risk. If null, no risk based sizing will be
 * applied/
 */
class AtrPolicy(
    private val atrPeriod: Int = 10,
    private val atrProfit: Double = 4.0,
    private val atrLoss: Double = 2.0,
    orderPercentage: Double = 0.02,
    private val atRisk: Double? = null,
    shorting: Boolean = false
) : FlexPolicy(orderPercentage = orderPercentage, shorting = shorting) {


    init {
        require(atRisk == null || atRisk in 0.0..1.0)
    }

    private val data = PriceBarSeries(atrPeriod + 1) // we need 1 observation extra
    private val talib = TaLib()

    /**
     * @see FlexPolicy.calcSize
     */
    override fun calcSize(amount: Double, signal: Signal, price: Double): Size {
        val asset = signal.asset

        // Calculate the max size
        val maxSize = asset.contractSize(amount, price, fractions)
        if ( atRisk == null ) return maxSize

        val atr = getAtr(asset, price)
        if (! atr.isFinite()) return Size.ZERO

        // Calculate the max loss based on the max size and atr
        val maxLoss = asset.value(maxSize, atr * atrLoss).value

        // Reduce size if maxLoss is bigger than value at risk
        val diff = maxLoss / (atRisk * amount)
        return if (diff > 1) {
            val newSize = maxSize / diff
            newSize.round(fractions) * signal.rating.direction
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

    private fun getAtr(asset: Asset, price: Double): Double {
        val serie = data[asset]
        if (serie == null || !serie.isFull()) return Double.NaN

        val minAtr = (price * 0.0001).absoluteValue
        return talib.atr(serie, atrPeriod).coerceAtLeast(minAtr)
    }

    /**
     * @see FlexPolicy.createOrder
     */
    override fun createOrder(signal: Signal, size: Size, price: Double): Order? {
        val asset = signal.asset

        // Calculate the ATR and make it relative to the direction of the size
        val atr = getAtr(asset, price) * size.sign
        if (! atr.isFinite()) return null

        // Create the actual orders
        val entry = MarketOrder(asset, size)

        val limitPrice = price + (atr * atrProfit)
        val stopPrice = price - (atr * atrLoss)
        if (limitPrice <= 0 || stopPrice <= 0) return null // unlikely, be better be safe
        val takeProfit = LimitOrder(asset, -size, limitPrice)
        val stopLoss = StopOrder(asset, -size, stopPrice)
        return BracketOrder(entry, takeProfit, stopLoss)
    }

    /**
     * @see FlexPolicy.start
     */
    override fun start(run: String, timeframe: Timeframe) {
        super.start(run, Timeframe.INFINITE)
        data.clear()
    }

}
