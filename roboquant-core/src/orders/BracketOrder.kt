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

package org.roboquant.orders

import org.roboquant.common.Asset
import java.time.Instant

/**
 * Bracket orders are designed to help limit the loss and lock in a profit by "bracketing" an
 * order with two opposite-side orders.
 *
 * - a BUY order is bracketed by a high-side sell limit order and a low-side sell stop(-limit) order.
 * - a SELL order is bracketed by a high-side buy stop(-limit) order and a low side buy limit order.
 *
 * @property main The primary order
 * @property profit The take profit order
 * @property loss The limit loss order
 *
 * @constructor Create a new bracket order
 *
 */
open class BracketOrder(
    val main: SingleOrder,
    val profit: SingleOrder,
    val loss: SingleOrder,
) : CombinedOrder(main, profit, loss) {

    companion object {

        /**
         * Create a bracket order with a Stop Loss Order and Limit Order to limit the loss and protect the profits.
         * the boundaries are based on a percentage offset from the current price. 1% offset = 0.01.
         */
        fun fromPercentage(asset: Asset, qty: Double, price: Double, profit: Double, loss: Double): BracketOrder {
            require(profit > 0.0 && loss > 0.0) { "Profit and Loss are values bigger than 0.0, for example 0.05 for 5%" }
            val mainOrder = MarketOrder(asset, qty)
            val direction = mainOrder.direction
            val stopLoss = StopOrder(asset, -qty, (1.0 - loss * direction) * price)
            val takeProfit = LimitOrder(asset, -qty, (1.0 + profit * direction) * price)
            return BracketOrder(mainOrder, takeProfit, stopLoss)
        }

    }

    init {
        // Some basic sanity checks
        require(main.quantity == -profit.quantity && profit.quantity == loss.quantity) { "Profit and loss order quantities need to be the same and opposite to the main order quantity" }
    }

    override fun clone(): BracketOrder {
        return BracketOrder(main.clone(), profit.clone(), loss.clone())
    }

    override val remaining: Double
        get() = if (main.status.open) main.remaining else profit.remaining


    override fun execute(price: Double, time: Instant): Double {

        var qty = 0.0

        if (main.status.open) {
            qty += main.execute(price, time)
            if (main.status.aborted) {
                status = main.status
            }
        }

        // We only trigger profit and loss orders if main order is completed.
        if (main.status == OrderStatus.COMPLETED) {

            if (profit.remaining != 0.0) {
                qty += profit.execute(price, time)
                loss.quantity = -main.quantity - profit.fill
            }

            if (loss.remaining != 0.0) {
                qty += loss.execute(price, time)
                profit.quantity = -main.quantity - loss.fill
            }

            if (profit.status.closed)
                status = profit.status
            else if (loss.status.closed)
                status = loss.status

            if (loss.fill + profit.fill == -main.quantity) {
                status = OrderStatus.COMPLETED
            }
        }

        return qty
    }
}