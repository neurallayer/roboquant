/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Size
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal

/**
 * This is the default policy that will be used if no other policy is specified. There are a number of parameters that
 * can be configured to change it behavior. By default, this policy will create Market Orders, but this can be
 * changed by providing a different [createOrder] method in a subclass.
 *
 * It will adhere to the following rules when it converts signals into orders:
 *
 * - If there still is an open order outstanding for the same asset, don't do anything
 * - If there is already an open position for the same asset, don't increase the position
 * - A SELL order will liquidate the full position for that asset in the portfolio if available
 * - Never go short for an asset (configurable)
 * - Never create BUY orders of which the total value is below a minimum amount (configurable)
 *
 * @property orderPercentage The percentage of overall equity value for a single order, default is 1% (0.01)
 * @property shorting Should the policy create orders that lead to short positions, default is false
 * @property oneOrderPerAsset Should there be only maximum one open order per asset at any time, default is true
 * @constructor Create new Default Policy
 */
open class DefaultPolicy(
    private val orderPercentage: Double = 0.01,
    private val shorting: Boolean = false,
) : BasePolicy() {

    private val logger = Logging.getLogger(DefaultPolicy::class)
    private val noOrder: Pair<Order?, Double> = Pair(null, 0.0)

    /**
     * Calculate the exposure impact on the buying power
     */
    private fun getExposure(account: Account, asset: Asset, size: Size, price: Double): Double {
        val cost = asset.value(size, price).absoluteValue
        val baseCurrencyCost = account.convert(cost)
        return baseCurrencyCost.value
    }


    @Suppress("ReturnCount")
    private fun createSellOrder(account: Account, signal: Signal, price: Double, amount: Amount): Pair<Order?, Double> {
        val position = account.portfolio.getValue(signal.asset)

        position.short && return noOrder
        position.long && signal.exit && return Pair(createOrder(signal, -position.size, price), 0.0)
        position.long && !signal.exit && return noOrder
        position.closed && ! signal.entry && return noOrder
        position.closed && ! shorting && return noOrder

        val size = calcSize(amount, signal.asset, price, account)
        if (size <= 0.0) return noOrder

        val bp = getExposure(account, signal.asset, size, price)
        val order = createOrder(signal, -size, price)
        return Pair(order, bp)
    }

    @Suppress("ReturnCount")
    private fun createBuyOrder(account: Account, signal: Signal, price: Double, amount: Amount): Pair<Order?, Double> {
        val position = account.portfolio.getValue(signal.asset)

        position.long && return noOrder
        position.short && ! signal.exit && return noOrder
        position.short && return Pair(createOrder(signal, -position.size, price), 0.0)
        position.closed && !signal.entry &&  return noOrder

        val size = calcSize(amount, signal.asset, price, account)
        if (size <= 0.0) return noOrder

        val bp = getExposure(account, signal.asset, size, price)
        val order = createOrder(signal, size, price)
        return Pair(order, bp)
    }

    /**
     * Create a new order based on the [signal], [size] and current [price]. Overwrite this method if you want to
     * create other orders types than the default [MarketOrder].
     */
    open fun createOrder(signal: Signal, size: Size, price: Double): Order? = MarketOrder(signal.asset, size)

    /**
     * @see Policy.act
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        if (signals.isEmpty()) return emptyList()

        val orders = mutableListOf<Order>()
        var buyingPower = account.buyingPower.value

        for (signal in signals.filter { it.rating !== Rating.HOLD }) {
            val asset = signal.asset

            // We don't place an order if we don't know the current price
            val price = event.getPrice(asset)

            if (price !== null) {
                val amount = account.equityAmount * orderPercentage

                val (order, exposure) = if (signal.rating.isNegative)
                        createSellOrder(account, signal, price, amount)
                    else
                        createBuyOrder(account, signal, price, amount)

                if (order != null && exposure < buyingPower) {
                    logger.fine { "signal=${signal} amount=$amount exposure=$exposure order=$order" }
                    orders.add(order)
                    buyingPower -= exposure
                }

            }
        }
        record("policy.signals", signals.size)
        record("policy.orders", orders.size)
        return orders
    }
}
