package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.SingleOrder
import org.roboquant.strategies.Signal
import org.roboquant.strategies.SignalResolution
import org.roboquant.strategies.resolve
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.min

/**
 * This is the default policy that will be used if no policy is specified.
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
open class NeverShortPolicy(
    private val minAmount: Double = 5000.0,
    private val maxAmount: Double = 20_000.0,
    private val signalResolve: SignalResolution = SignalResolution.NO_CONFLICTS,
    private val  shorting: Boolean = false,
    private val  increasePosition : Boolean = false,
    private val  oneOrderPerAsset: Boolean = true
) : BasePolicy() {

    private fun createSellOrder(account: Account, signal: Signal, price: Double, buyingPower: Double): SingleOrder? {
        val position = account.portfolio.getPosition(signal.asset)

        // Check if we are going to short
        if (! shorting && ! position.long) return null

        return if (position.long) {
            getOrder(signal, -position.quantity)
        } else {
            if (position.short && ! increasePosition) return null
            val amount = min(buyingPower, maxAmount).absoluteValue
            val volume = floor(calcVolume(amount, signal.asset, price, account))
            if (volume > 0) getOrder(signal, -volume) else null
        }

    }

    private fun createBuyOrder(account: Account, signal: Signal, price: Double, buyingPower: Double): SingleOrder? {
        val position = account.portfolio.getPosition(signal.asset)

        // If we have already a position don't increase it
        if (position.long) return null

        val amount = min(buyingPower, maxAmount)
        val volume = floor(calcVolume(amount, signal.asset, price, account))
        return if (volume > 0) getOrder(signal, volume) else null
    }

    /**
     * Get order. Overwrite this method if you want to create other orders types than the default SingleOrder
     *
     * Other checks have already been performed
     *
     * @param signal
     * @param qty
     */
    open fun getOrder(signal: Signal, qty: Double) : SingleOrder = signal.toMarketOrder(qty)



    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<SingleOrder>()
        var buyingPower = account.buyingPower
        val openOrderAssets = account.orders.open.map { it.asset }

        for (signal in signals.resolve(signalResolve)) {
            val asset = signal.asset

            // If there are open orders, we don't place a new order
            if (oneOrderPerAsset && openOrderAssets.contains(asset)) continue
            val price = event.getPrice(asset)
            if (price !== null) {
                if (signal.rating.isNegative()) {
                    val order = createSellOrder(account, signal, price, buyingPower)
                    orders.addNotNull(order)
                } else if (signal.rating.isPositive() && buyingPower >= minAmount) {
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
        }
        record("policy.signals", signals.size)
        record("policy.orders", orders.size)
        return orders
    }
}
