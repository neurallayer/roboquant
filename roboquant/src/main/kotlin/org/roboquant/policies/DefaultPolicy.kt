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
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import kotlin.math.max

/**
 * This is the default policy that will be used if no other policy is specified. There are a number of parameters that
 * can be configured to change it behavior. By default, this policy will create Market Orders, but this can be
 * changed by providing a different [createOrder] method in a subclass.
 *
 * @property orderPercentage The percentage of overall equity value to allocate to a single order, default is 1% (0.01)
 * @property shorting Can the policy create orders that possibly lead to short positions, default is false
 * @property priceType The type of price to use, default is "DEFAULT"
 * @property fractions For fractional trading, the amount of fractions (decimals) to allow for. Default is 0
 * @constructor Create new Default Policy
 */
open class DefaultPolicy(
    private val orderPercentage: Double = 0.01,
    private val shorting: Boolean = false,
    private val priceType: String = "DEFAULT",
    private val fractions: Int = 0
) : BasePolicy() {

    private val logger = Logging.getLogger(DefaultPolicy::class)

    /**
     * Would the [signal] generate a reduced position size based on the current [position]
     */
    private fun reducedPositionOrder(position: Position, signal: Signal) : Boolean {
        with (position) {
            if (open && signal.exit) {
                if (long && signal.rating.isNegative) return true
                if (short && signal.rating.isPositive) return true
            }
        }
        return false
    }

    /**
     * Return the size that can be bought/sold with the provided [amount] given the [price] in the currency of
     * the asset at the provided [time]. This implementation will take into consideration the configured fractions
     */
    private fun calcSize(amount: Amount, signal: Signal, price: Double, time: Instant): Size {
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
        val amount = account.equityAmount * orderPercentage


        @Suppress("LoopWithTooManyJumpStatements")
        for (signal in signals) {
            val asset = signal.asset

            // We don't create an order if we don't know the current price
            val price = event.getPrice(asset, priceType)

            logger.debug { "signal=${signal} amount=$amount price=$price" }

            if (price !== null) {
                val position = account.positions.getPosition(asset)
                if (reducedPositionOrder(position, signal)) {
                    val order = createOrder(signal, -position.size, price) // close position
                    orders.addNotNull(order)
                } else {
                    if (position.open) continue // we don't increase position sizing
                    if (! signal.entry) continue // signal doesn't allow to open new positions

                    val size = calcSize(amount, signal, price, event.time)
                    if (size.iszero) continue
                    if (size < 0 && ! shorting) continue

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
