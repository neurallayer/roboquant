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
import org.roboquant.brokers.Position
import org.roboquant.brokers.getPosition
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Size
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import kotlin.math.max

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
 * @property singleOrder Should there be only maximum one open order per asset at any time, default is true
 * @constructor Create new Default Policy
 */
open class DefaultPolicy(
    private val orderPercentage: Double = 0.01,
    private val shorting: Boolean = false,
    private val priceType: String = "DEFAULt"
) : BasePolicy() {

    private val logger = Logging.getLogger(DefaultPolicy::class)

    /**
     * Calculate the exposure impact on the buying power
     */
    private fun getExposure(position: Position, asset: Asset, size: Size, price: Double): Amount {
        // No exposure if we reduce the overall position size, so that is always allowed
        return if (position.isReduced(size))  {
            Amount(asset.currency, 0.0)
        } else {
            asset.value(size, price).absoluteValue
        }
    }

    private fun createSellOrder(size: Size, signal: Signal, price: Double, position: Position): Order? {

        return when {
            position.long && signal.exit -> createOrder(signal, -position.size, price)
            position.closed && signal.entry && shorting -> createOrder(signal, -size, price)
            else -> null
        }

    }

    private fun createBuyOrder(size: Size, signal: Signal, price: Double, position: Position): Order? {

        return when {
            position.short && signal.exit -> createOrder(signal, -position.size, price)
            position.closed && signal.entry && size > 0 -> createOrder(signal, size, price)
            else -> null
        }

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
        var buyingPower = max(account.buyingPower.value, 0.0)

        for (signal in signals.filter { it.rating !== Rating.HOLD }) {
            val asset = signal.asset

            // We don't create an order if we don't know the current price
            val price = event.getPrice(asset, priceType)

            if (price !== null) {
                val amount = account.equityAmount * orderPercentage
                val size = calcSize(amount, signal.asset, price, event.time)
                val position = account.positions.getPosition(asset)

                val order = if (signal.rating.isNegative)
                        createSellOrder(size, signal, price, position)
                    else
                        createBuyOrder(size, signal, price, position)

                if (order != null) {
                    val exposure = account.convert(getExposure(position, signal.asset, size, price)).value
                    if (exposure <= buyingPower) {
                        logger.debug { "signal=${signal} amount=$amount exposure=$exposure order=$order" }
                        orders.add(order)
                        buyingPower -= exposure
                    }
                }

            }
        }
        record("policy.signals", signals.size)
        record("policy.orders", orders.size)
        return orders
    }
}
