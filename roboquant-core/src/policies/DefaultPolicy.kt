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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.brokers.Orders
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.min


/**
 * This is the default policy that will be used if no other policy is specified. There are a number of parameters that
 * can be configured to change it behavior.
 *
 * By default, this policy will create Market Orders, but this can be changed by providing a different [createOrder]
 * method.
 *
 * It will adhere to the following rules when it converts signals into orders:
 *
 * - Remove conflicting signals (configurable)
 * - If there still is an open order outstanding for the same asset, don't do anything
 * - If there is already an open position for the same asset, don't place an additional BUY order
 * - A SELL order will liquidate the full position for that asset in the portfolio if available
 * - Never go short for an asset (configurable)
 * - Never create BUY orders of which the total value is below a minimum amount (configurable)
 *
 *
 * @property minAmount
 * @property maxAmount
 * @constructor Create new Never short policy
 */
open class DefaultPolicy(
    private val minAmount: Double = 5000.0,
    private val maxAmount: Double = 20_000.0,
    private val signalResolve: SignalResolution = SignalResolution.NO_CONFLICTS,
    private val shorting: Boolean = false,
    private val increasePosition: Boolean = false,
    private val oneOrderPerAsset: Boolean = true,
    private val maxOrdersPerDay: Int = Int.MAX_VALUE,
) : BasePolicy() {

    private fun createSellOrder(account: Account, signal: Signal, price: Double, buyingPower: Double): Order? {
        val position = account.portfolio.getPosition(signal.asset)

        // Check if we are going to short
        if (!shorting && !position.long) return null

        return if (position.long) {
            createOrder(signal, -position.size, price)
        } else {
            if (position.short && !increasePosition) return null
            val amount = min(buyingPower, maxAmount).absoluteValue
            val volume = floor(calcVolume(amount, signal.asset, price, account))
            if (volume > 0) createOrder(signal, -volume, price) else null
        }

    }

    private fun createBuyOrder(account: Account, signal: Signal, price: Double, buyingPower: Double): Order? {
        val position = account.portfolio.getPosition(signal.asset)

        // If we have already a position don't increase it
        if (position.long) return null

        val amount = min(buyingPower, maxAmount)
        val volume = floor(calcVolume(amount, signal.asset, price, account))
        return if (volume > 0) createOrder(signal, volume, price) else null
    }


    open fun getAvailableCash(account: Account) {

    }


    /**
     * Create a new order based on the [signal], [qty] and current [price]. Overwrite this method if you want to
     * create other orders types than the default MarketOrder.
     */
    open fun createOrder(signal: Signal, qty: Double, price: Double): Order? = signal.toMarketOrder(qty)

    /**
     * How many orders do we have for the current trading day. This takes into account that different orders may be
     * trading on different exchanges with different timezones.
     */
    private fun getOrdersCurrentDay(now: Instant, orders: Orders) =
        orders.filter { it.asset.exchange.sameDay(now, it.placed) }

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<Order>()
        var buyingPower = account.freeAmount
        val openOrderAssets = account.orders.open.map { it.asset }
        val remainingDayOrders =
            if (maxOrdersPerDay == Int.MAX_VALUE) Int.MAX_VALUE else maxOrdersPerDay - getOrdersCurrentDay(
                event.now,
                account.orders
            ).size

        for (signal in signals.resolve(signalResolve)) {
            val asset = signal.asset

            // If there are open orders, we don't place a new order
            if (oneOrderPerAsset && openOrderAssets.contains(asset)) continue
            val price = event.getPrice(asset)
            if (price !== null) {
                if (signal.rating.isNegative) {
                    val order = createSellOrder(account, signal, price, buyingPower)
                    orders.addNotNull(order)
                } else if (signal.rating.isPositive && buyingPower >= minAmount) {
                    val order = createBuyOrder(account, signal, price, buyingPower)
                    if (order != null) {
                        orders.add(order)
                        val cost = order.getValue(price)
                        if (!cost.isNaN()) {
                            val baseCurrencyCost = account.convertToCurrency(asset.currency, cost)
                            buyingPower -= baseCurrencyCost
                        }
                    }
                }
            }
            if (orders.size > remainingDayOrders) break
        }
        record("policy.signals", signals.size)
        record("policy.orders", orders.size)
        return orders
    }
}
