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
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.zeroOrMore
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant
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
 * @property minAmount If an order has impact on buying power, what should be the minimum amount
 * @property maxAmount The maximum amount (in terms of buying power impact) for a single order
 * @property signalResolution The [SignalResolution] to use for resovling conflicting signals
 * @property shorting Should the policy create orders that lead to short positions
 * @property increasePosition Should the policy create orders increase already open positions
 * @property oneOrderPerAsset Should there be only maximum one open order per asset at any time
 * @constructor Create new Default Policy
 */
open class DefaultPolicy(
    private val minAmount: Double = 5000.0,
    private val maxAmount: Double = 20_000.0,
    private val signalResolution: SignalResolution = SignalResolution.NO_CONFLICTS,
    private val shorting: Boolean = false,
    private val increasePosition: Boolean = false,
    private val oneOrderPerAsset: Boolean = true,
    private val maxOrdersPerDay: Int = Int.MAX_VALUE
) : BasePolicy() {

    private val logger = Logging.getLogger(DefaultPolicy::class)

    init {
        require(maxOrdersPerDay > 0)
        require(minAmount <= maxAmount)
    }

    private fun reducedBuyingPower(account: Account, asset: Asset, qty: Double, price: Double): Double {
        val cost = Amount(asset.currency, asset.multiplier * qty * price).absoluteValue
        val baseCurrencyCost = account.convert(cost)
        return baseCurrencyCost.value
    }

    private fun createSellOrder(account: Account, signal: Signal, price: Double, amount: Double): Pair<Order?, Double> {
        val position = account.portfolio.getPosition(signal.asset)

        if (position.long) return Pair(createOrder(signal, -position.size, price), 0.0)
        if (!shorting) return Pair(null, 0.0)
        if (position.short && !increasePosition) return Pair(null, 0.0)

        val volume = floor(calcVolume(amount, signal.asset, price, account))
        if (volume <= 0.0) return Pair(null, 0.0)

        val bp = reducedBuyingPower(account, signal.asset, volume, price)
        val order = createOrder(signal, -volume, price)
        return Pair(order, bp)
    }

    private fun createBuyOrder(account: Account, signal: Signal, price: Double, amount: Double): Pair<Order?, Double> {
        val position = account.portfolio.getPosition(signal.asset)

        if (position.short) return Pair(createOrder(signal, -position.size, price), 0.0)
        if (position.long && !increasePosition) return Pair(null, 0.0)

        val volume = floor(calcVolume(amount, signal.asset, price, account))
        if (volume <= 0.0) return Pair(null, 0.0)

        val bp = reducedBuyingPower(account, signal.asset, volume, price)
        val order = createOrder(signal, volume, price)
        return Pair(order, bp)
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
        orders.filter { it.asset.exchange.sameDay(now, it.placed) }.size

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<Order>()
        var buyingPower = account.buyingPower.value
        val openOrderAssets = account.orders.open.map { it.asset }
        var remainingDayOrders = maxOrdersPerDay

        // Performance optimilization. Checking day orders is expensive so we only do it when required
        if (maxOrdersPerDay != Int.MAX_VALUE) remainingDayOrders -= getOrdersCurrentDay(event.time, account.orders)

        for (signal in signals.resolve(signalResolution)) {
            val asset = signal.asset

            // If there are open orders for the asset, we don't place an additional order
            if (oneOrderPerAsset && openOrderAssets.contains(asset)) continue

            // We don't place an order if we don't know the current price
            val price = event.getPrice(asset)
            if (price !== null) {
                val amount = min(maxAmount, buyingPower).zeroOrMore
                if (signal.rating.isNegative) {
                    val (order, cost) = createSellOrder(account, signal, price, amount)
                    if (order != null) {
                        logger.fine {"sell $amount $cost $order" }
                        orders.add(order)
                        buyingPower -= cost
                    }
                } else if (signal.rating.isPositive) {
                    val (order, cost) = createBuyOrder(account, signal, price, amount)
                    if (order != null) {
                        logger.fine {"buy $amount $cost $order" }
                        orders.add(order)
                        buyingPower -= cost
                    }
                }
            }
            if (orders.size >= remainingDayOrders) break
        }
        record("policy.signals", signals.size)
        record("policy.orders", orders.size)
        return orders
    }
}
