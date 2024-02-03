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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.exposure
import org.roboquant.brokers.getPosition
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.orders.*
import org.roboquant.strategies.Signal
import java.time.Instant

/**
 * Configuration for the [FlexPolicy] that allows to tune the behavior of the policy.
 *
 * @property orderPercentage The percentage of the equity value to allocate to a single order.
 * The default is 1.percent (0.01).
 * In the case of an account with high leverage, this value can be larger than 1 (100%).
 * @property shorting Can the policy create orders that potentially lead to short positions, default is false
 * @property priceType The type of price to use, default is "DEFAULT"
 * @property fractions For fractional trading, the number of fractions (decimals) to allow for. Default is 0
 * @property oneOrderOnly Only allow one order to be open for a given asset at a given time, default is true
 * @property safetyMargin the percentage of the equity value that don't get allocated to orders. This way, you
 * are more likely to stay away from bounced orders and margin calls. Default is same percentage as [orderPercentage]
 * @property minPrice the minimal price for an asset before opening a position, default is null (no minimum). This
 * can be used to avoid trading penny stock and other high volatile assets.
 */
open class FlexPolicyConfig(
    var orderPercentage: Double = 1.percent,
    var shorting: Boolean = false,
    var priceType: String = "DEFAULT",
    var fractions: Int = 0,
    var oneOrderOnly: Boolean = true,
    var safetyMargin: Double = orderPercentage,
    var minPrice: Amount? = null,
)

/**
 * This is the default policy that will be used if no other policy is specified.
 * There are several properties that can be specified during construction that change the underlying behavior:
 *
 * ```
 * val policy = FlexPolicy {
 *      orderPercentage = 2.percent
 * }
 * ```
 *
 * See also [FlexPolicyConfig] for more details.
 *
 * Also, some methods can be overwritten in a subclass to provide even more flexibility.
 * For example, this policy will create [MarketOrder]s by default, but this can be changed by overwriting
 * the [createOrder] method in a subclass.
 *
 * When [enableMetrics] is set to true, it will record the number of `actions`, `signals` and `orders`.
 *
 * @constructor Create a new instance of a FlexPolicy
 * @param configure additional configuration parameters
 */
open class FlexPolicy(
    configure: FlexPolicyConfig.() -> Unit = {}
) : BasePolicy() {

    protected val logger = Logging.getLogger(FlexPolicy::class)

    // Holds all the configuration
    protected val config = FlexPolicyConfig()

    init {
        config.configure()
    }

    /**
     * Set of predefined FlexPolicies
     */
    companion object {

        /**
         * This policy uses a percentage of the available buying-power to calculate the order amount (in contrast
         * to the default implementation that uses a percentage of the equity):
         *
         * The used formula is:
         *
         * ```
         * orderAmount = buyingPower * orderPercentage
         * ```
         */
        fun singleAsset(configure: FlexPolicyConfig.() -> Unit = {}): FlexPolicy {
            class SinglePolicy : FlexPolicy(
                configure
            ) {
                override fun amountPerOrder(account: Account): Amount {
                    return account.buyingPower * config.orderPercentage
                }
            }
            return SinglePolicy()
        }

        /**
         * Capital-based flex policy.
         */
        fun capitalBased(
            configure: FlexPolicyConfig.() -> Unit = {}
        ): FlexPolicy {
            class CapiltalBasedPolicy : FlexPolicy(configure) {
                override fun amountPerOrder(account: Account): Amount {
                    val capital = account.positions.exposure + account.buyingPower
                    val amount = account.convert(capital)
                    return amount * config.orderPercentage
                }
            }
            return CapiltalBasedPolicy()
        }

        /**
         * Return a FlexPolicy that generates bracket orders, with the following characteristics:
         * - a market order for entry
         * - trail-order for take profit with provided [trailPercentage], default is 5%
         * - stop-loss order for limiting loss with provided [stopPercentage], default is 1%
         *
         * @see FlexPolicy
         * @see BracketOrder.marketTrailStop
         */
        fun bracketOrders(
            trailPercentage: Double = 5.percent,
            stopPercentage: Double = 1.percent,
            configure: FlexPolicyConfig.() -> Unit = {}
        ): FlexPolicy {
            class MyPolicy : FlexPolicy(configure = configure) {

                override fun createOrder(signal: Signal, size: Size, priceAction: PriceAction): Order {
                    val price = priceAction.getPrice(config.priceType)
                    return BracketOrder.marketTrailStop(signal.asset, size, price, trailPercentage, stopPercentage)
                }
            }

            return MyPolicy()
        }

        /**
         * FlexPolicy that generates limit orders for entry orders.
         *
         * If [limitPercentage] is a positive value, the following logic applies,
         *
         * ```
         * BUY orders have a below market price limit, and SELL order above market price.
         * ```
         *
         * @param limitPercentage the limit as percentage of the price
         *
         */
        fun limitOrders(
            limitPercentage: Double = 1.percent,
            configure: FlexPolicyConfig.() -> Unit = {}
        ): FlexPolicy {
            class MyPolicy : FlexPolicy(configure = configure) {

                override fun createOrder(signal: Signal, size: Size, priceAction: PriceAction): Order {
                    val price = priceAction.getPrice(config.priceType)

                    // BUY orders have a below market price limit, and SELL order above
                    val limitOffset = limitPercentage * size.sign
                    val limitPrice = price * (1.0 - limitOffset)
                    return LimitOrder(signal.asset, size, limitPrice)
                }
            }

            return MyPolicy()
        }

    }

    /**
     * Would the [signal] generate a reduced position size based on the current [position]. Reduced positions signals
     * have some unique properties:
     *
     * 1. They are allowed independent of buying power available. So you can always close a position.
     * 2. Only signals that are compatible with an exit strategy are allowed
     */
    private fun reducedPositionSignal(position: Position, signal: Signal): Boolean {
        with(position) {
            if (open && signal.exit) {
                if (long && signal.rating.isNegative) return true
                if (short && signal.rating.isPositive) return true
            }
        }
        return false
    }

    /**
     * Return the size that can be bought/sold with the provided [amount] and [price] of the asset. This implementation
     * also takes into consideration the configured [FlexPolicyConfig.fractions].
     *
     * This method will only be invoked if the signal is not closing a position.
     */
    open fun calcSize(amount: Double, signal: Signal, price: Double): Size {
        return signal.asset.contractSize(amount, price, config.fractions) * signal.rating.direction
    }

    /**
     * Create a new order based on the [signal], [size] and current [priceAction].
     * This method should return null to indicate no order should be created at all.
     *
     * Overwrite this method if you want to create orders other than the default [MarketOrder].
     */
    open fun createOrder(signal: Signal, size: Size, priceAction: PriceAction): Order? {
        return MarketOrder(signal.asset, size)
    }

    /**
     * Return the amount that should be allocated to a single order.
     *
     * The default implementation is:
     * ```
     * amount per order = account.equityAmount * orderPercentage
     * ```
     *
     * Since this formula is based on equity, the amount stays relatively stable.
     *
     * When trading a single asset only, you might want to overwrite this method and base the order amount on the
     * `account.buyingPower` instead.
     */
    open fun amountPerOrder(account: Account): Amount {
        return account.equityAmount * config.orderPercentage
    }

    /**
     * It the minimum price met for the provided [asset]. If the asset is in different currency than the minimum price,
     * a conversion will first take place.
     */
    private fun meetsMinPrice(asset: Asset, price: Double, time: Instant): Boolean {
        val minPrice = config.minPrice
        return minPrice == null || minPrice.convert(asset.currency, time) <= price
    }

    /**
     * Returns true is the collection of orders contains at least one order for [asset], false otherwise.
     */
    private operator fun Collection<Order>.contains(asset: Asset) = any { it.asset == asset }

    /**
     * Record basic metrics: `actions`, `signals`, `orders.new`, `orders.open`,
     * `positions` and `buyingpower`.
     *
     * The main purpose is to better understand when the policy is not behaving as expected.
     */
    open fun record(orders: List<Order>, signals: List<Signal>, event: Event, account: Account) {
        record("actions", event.actions.size)
        record("signals", signals.size)
        record("orders.new", orders.size)
        record("orders.open", account.openOrders.size)
        record("positions", account.positions.size)
        record("buyingpower", account.buyingPower.value)
    }

    /**
     * @see Policy.act
     */
    @Suppress("ComplexMethod")
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<Order>()

        if (signals.isNotEmpty()) {
            val equityAmount = account.equityAmount
            val safetyAmount = equityAmount * config.safetyMargin
            val time = event.time
            var buyingPower = account.buyingPower - safetyAmount.value
            val amountPerOrder = amountPerOrder(account)

            @Suppress("LoopWithTooManyJumpStatements")
            for (signal in signals) {
                val asset = signal.asset

                if (config.oneOrderOnly && (account.openOrders.contains(asset) || orders.contains(asset))) continue

                // Don't create an order if we don't know the current price
                val priceAction = event.prices[asset] ?: continue

                val price = priceAction.getPrice(config.priceType)
                logger.debug { "signal=${signal} buyingPower=$buyingPower amount=$amountPerOrder action=$priceAction" }

                val position = account.positions.getPosition(asset)
                if (reducedPositionSignal(position, signal)) {
                    val order = createOrder(signal, -position.size, priceAction) // close position
                    orders.addNotNull(order)
                } else {
                    if (position.open) continue // we don't increase position sizing
                    if (!signal.entry) continue // signal doesn't allow opening new positions
                    if (amountPerOrder > buyingPower) continue // not enough buying power left

                    val assetAmount = amountPerOrder.convert(asset.currency, time).value
                    val size = calcSize(assetAmount, signal, price)
                    if (size.iszero) continue
                    if (size.isNegative && !config.shorting) continue
                    if (!meetsMinPrice(asset, price, time)) continue

                    val order = createOrder(signal, size, priceAction)
                    if (order == null) {
                        logger.trace { "no order created time=$time signal=$signal" }
                        continue
                    }

                    val assetExposure = asset.value(size, price).absoluteValue
                    val exposure = assetExposure.convert(buyingPower.currency, time).value
                    orders.add(order)
                    buyingPower -= exposure // reduce buying power with the exposed amount
                    logger.debug { "buyingPower=$buyingPower exposure=$exposure order=$order" }

                }

            }
        }
        if (enableMetrics) record(orders, signals, event, account)
        return orders
    }
}
