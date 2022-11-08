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
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import org.roboquant.strategies.utils.MovingWindow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import kotlin.math.max

/**
 * This is the default policy that will be used if no other policy is specified. There are several properties that
 * can be specified during construction that changes its behavior.
 *
 * Also, many of its methods can be overwritten in a subclass to provide even more flexibility. For example, this
 * policy will create Market Orders by default, but this can be changed by overwriting the [createOrder] method in
 * a subclass.
 *
 * @property orderPercentage The percentage of overall equity value to allocate to a single order, default is 1% (0.01)
 * @property shorting Can the policy create orders that possibly lead to short positions, default is false
 * @property priceType The type of price to use, default is "DEFAULT"
 * @property fractions For fractional trading, the amount of fractions (decimals) to allow for. Default is 0
 * @property oneOrderOnly Only allow one order to be open for a given asset at a time, default is true
 * @property priceWindow Keep track of prices per asset for the provided period, default is 0
 * @constructor Create new Default Policy
 */
open class DefaultPolicy(
    private val orderPercentage: Double = 0.01,
    private val shorting: Boolean = false,
    private val priceType: String = "DEFAULT",
    private val fractions: Int = 0,
    private val oneOrderOnly: Boolean = true,
    private val priceWindow: Int = 0,
) : BasePolicy() {

    private val logger = Logging.getLogger(DefaultPolicy::class)
    private val prices = mutableMapOf<Asset, MovingWindow>()

    /**
     * Would the [signal] generate a reduced position size based on the current [position]. Reduced positions signals
     * have some unique properties:
     *
     * 1. They are allowed independent of buying power available. So you can always close a position.
     * 2. Only [Signal.exit] types are honoured
     */
    private fun reducedPositionSingal(position: Position, signal: Signal): Boolean {
        with(position) {
            if (open && signal.exit) {
                if (long && signal.rating.isNegative) return true
                if (short && signal.rating.isPositive) return true
            }
        }
        return false
    }

    /**
     * Stores prices for a [priceWindow] sized window. This can be used later for example in the [createOrder] method
     * to implement volatility based orders.
     */
    open fun storePrices(event: Event) {
        if (priceWindow > 0) {
            event.prices.forEach {
                val entry = prices.getOrPut(it.key) { MovingWindow(priceWindow)}
                val price = it.value.getPrice(priceType)
                entry.add(price)
            }
        }
    }

    /**
     * Reset its state
     */
    override fun reset() {
        super.reset()
        prices.clear()
    }


    /**
     * Return the size that can be bought/sold with the provided [amount] given the [price] in the currency of
     * the asset at the provided [time]. This implementation will take into consideration the configured fractions.
     *
     * This method will only be invoked if the signal is not a reduced position signal.
     */
    open fun calcSize(amount: Amount, signal: Signal, price: Double, time: Instant): Size {
        val asset = signal.asset
        val singleContractPrice = asset.value(Size.ONE, price).value
        val availableAssetCash = amount.convert(asset.currency, time).value
        return if (availableAssetCash <= 0.0) {
            Size.ZERO
        } else {
            val size = BigDecimal(availableAssetCash / singleContractPrice).setScale(fractions, RoundingMode.DOWN)
            Size(size) * signal.rating.direction
        }
    }

    /**
     * Create a new order based on the [signal], [size] and current [price]. Overwrite this method if you want to
     * create other orders types than the default [MarketOrder]. This method should return null to indicate no order
     * should be created at all.
     */
    open fun createOrder(signal: Signal, size: Size, price: Double): Order? {
        return MarketOrder(signal.asset, size)
    }

    /**
     * Get the amount of cash to allocate to a new order. Default implementation is
     *
     *      account.equityAmount * orderPercentage
     *
     * Closing a position is always possible since it doesn't use this amount.
     */
    open fun getOrderAmount(account: Account): Amount {
        return account.equityAmount * orderPercentage
    }

    /**
     * @see Policy.act
     */
    @Suppress("ComplexMethod")
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        storePrices(event)
        if (signals.isEmpty()) return emptyList()

        val orders = mutableListOf<Order>()
        var buyingPower = max(account.buyingPower.value, 0.0)
        val amount = getOrderAmount(account)

        @Suppress("LoopWithTooManyJumpStatements")
        for (signal in signals) {
            val asset = signal.asset

            if (oneOrderOnly && account.openOrders.any { it.asset == signal.asset }) continue

            // We don't create an order if we don't know the current price
            val price = event.getPrice(asset, priceType)

            logger.debug { "signal=${signal} buyingPower=$buyingPower amount=$amount price=$price" }

            if (price !== null) {

                val position = account.positions.getPosition(asset)
                if (reducedPositionSingal(position, signal)) {
                    val order = createOrder(signal, -position.size, price) // close position
                    orders.addNotNull(order)
                } else {
                    if (position.open) continue // we don't increase position sizing
                    if (!signal.entry) continue // signal doesn't allow to open new positions

                    val size = calcSize(amount, signal, price, event.time)
                    if (size.iszero) continue
                    if (size < 0 && !shorting) continue

                    val assetExposure = asset.value(size, price).absoluteValue
                    val exposure = account.convert(assetExposure, event.time).value
                    if (exposure > buyingPower) continue
                    val order = createOrder(signal, size, price)

                    if (order != null) {
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
