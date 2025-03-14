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

package org.roboquant.traders

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceItem
import org.roboquant.orders.*
import org.roboquant.strategies.Signal
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

/**
 * Configuration for the [FlexTrader] that allows to tune the behavior of the trader.
 *
 * @property orderPercentage The percentage of the equity value to allocate to a single order.
 * The default is 1.percent (0.01).
 * In the case of an account with high leverage, this value can be larger than 1 (100%).
 * @property shorting Can the trader create orders that potentially lead to short positions, default is false
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
 * This is the default trader that will be used if no other trader is specified. It includes some common behavior that
 * is typically required when bringing a solution live.
 *
 * There are several properties that can be specified during construction that change the underlying behavior by defining
 * a [FlexPolicyConfig].
 *
 * ```
 * val trader = FlexTrader {
 *      orderPercentage = 2.percent
 * }
 * ```
 *
 * See also [FlexPolicyConfig] for more details.
 *
 * Also, some methods can be overwritten in a subclass to provide even more flexibility.
 *
 *
 * @constructor Create a new instance of a FlexTrader
 * @param configure additional configuration parameters
 */
open class FlexTrader(
    configure: FlexPolicyConfig.() -> Unit = {}
) : Trader {

    protected val logger = Logging.getLogger(FlexTrader::class)

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
         * This trader uses a percentage of the available buying-power to calculate the order amount (in contrast
         * to the default implementation that uses a percentage of the equity):
         *
         * The used formula is:
         *
         * ```
         * orderAmount = buyingPower * orderPercentage
         * ```
         */
        fun singleAsset(configure: FlexPolicyConfig.() -> Unit = {}): FlexTrader {
            class SingleTrader : FlexTrader(
                configure
            ) {
                override fun amountPerOrder(account: Account): Amount {
                    return account.buyingPower * config.orderPercentage
                }
            }
            return SingleTrader()
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
                if (long && signal.isSell) return true
                if (short && signal.isBuy) return true
            }
        }
        return false
    }

    /**
     * Return the size that can be bought/sold with the provided  of the asset. This implementation
     * also takes into consideration the configured [FlexPolicyConfig.fractions] and the strength of the rating in the
     * signal.
     *
     * This method will only be invoked if the signal is not closing a position.
     */
    /**
     * Return the size that can be bought/sold with the provided [amount] and [price] of the asset. This implementation
     * also takes into consideration the configured [FlexPolicyConfig.fractions] and the strength of the rating in the
     * signal.
     *
     * This method will only be invoked if the signal is not closing a position.
     */
    open fun calcSize(amount: Double, signal: Signal, price: Double): Size {
        require(config.fractions >= 0) { "factions has to be >= 0, found ${config.fractions}" }
        val singleContractValue = signal.asset.value(Size.ONE, price).value
        val size = BigDecimal(amount / singleContractValue).setScale(config.fractions, RoundingMode.DOWN)
        return Size(size)
    }

    /**
     * Create a new order based on the [signal], [size] and current [priceItem].
     * This method should return null to indicate no order should be created at all.
     *
     * Overwrite this method if you want to create orders other than the default [Order].
     */
    open fun createOrder(signal: Signal, size: Size, priceItem: PriceItem): Order? {
        return Order(signal.asset, size, priceItem.getPrice())
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
        return account.equityAmount() * config.orderPercentage
    }

    /**
     * It the minimum price met for the provided [asset]. If the asset is in different currency than the minimum price,
     * a conversion will first take place.
     */
    private fun meetsMinPrice(asset: Asset, price: Double, time: Instant): Boolean {
        val minPrice = config.minPrice
        return minPrice == null || minPrice.convert(asset.currency, time).value <= price
    }

    /**
     * Returns true is the collection of orders contains at least one order for [asset], false otherwise.
     */
    private operator fun Collection<Order>.contains(asset: Asset) = any { it.asset == asset }


    protected fun log(signal: Signal, price: PriceItem?, position:Position, reason: String) {
        logger.info { "signal=$signal price=$price, position=$position, reason=$reason" }
    }

    /**
     * @see Trader.createOrders
     */
    @Suppress("ComplexMethod")
    override fun createOrders(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val instructions = mutableListOf<Order>()

        if (signals.isNotEmpty()) {
            val equityAmount = account.equityAmount()
            val safetyAmount = equityAmount * config.safetyMargin
            val time = event.time
            var buyingPower = account.buyingPower - safetyAmount.value
            val amountPerOrder = amountPerOrder(account)

            @Suppress("LoopWithTooManyJumpStatements")
            for (signal in signals) {
                val asset = signal.asset
                val position = account.positions.getOrDefault(asset, Position.empty(asset))

                // Don't create an order if we don't know the current price
                val priceItem = event.prices[asset]
                logger.debug {
                    "signal=${signal} position=$position buyingPower=$buyingPower amount=$amountPerOrder item=$priceItem"
                }

                if (priceItem == null) {
                    log(signal, null, position, "no price")
                    continue
                }

                if (config.oneOrderOnly && account.orders.contains(asset)) {
                    log(signal, priceItem, position, "one order only")
                    continue
                }

                val price = priceItem.getPrice(config.priceType)

                if (reducedPositionSignal(position, signal)) {
                    val order = createOrder(signal, -position.size, priceItem) // close position
                    instructions.addNotNull(order)
                } else {
                    if (position.open) continue // we don't increase position sizing
                    if (!signal.entry) continue // signal doesn't allow opening new positions
                    if (amountPerOrder.value > buyingPower.value) continue // not enough buying power left

                    val assetAmount = amountPerOrder.convert(asset.currency, time).value
                    val size = calcSize(assetAmount, signal, price)
                    if (size.iszero) continue
                    if (size.isNegative && !config.shorting) continue
                    if (!meetsMinPrice(asset, price, time)) continue

                    val order = createOrder(signal, size, priceItem)
                    if (order == null) {
                        logger.trace { "no order created time=$time signal=$signal" }
                        continue
                    }

                    val assetExposure = asset.value(size, price).absoluteValue
                    val exposure = assetExposure.convert(buyingPower.currency, time).value
                    instructions.add(order)
                    buyingPower -= exposure // reduce buying power with the exposed amount
                    logger.debug { "buyingPower=$buyingPower exposure=$exposure order=$order" }

                }

            }
        }
        return instructions
    }
}
