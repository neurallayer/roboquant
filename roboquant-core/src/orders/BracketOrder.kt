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

import org.roboquant.brokers.sim.Execution
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
class BracketOrder(
    val main: SingleOrder,
    val profit: SingleOrder,
    val loss: SingleOrder,
) : Order(main.asset) {


    init {
        // Some basic sanity checks
        require(main.asset == profit.asset && profit.asset == loss.asset) { "Assets need to be identical" }
        require(main.quantity == -profit.quantity && profit.quantity == loss.quantity) { "Quantities need to match" }
    }

    override fun clone(): BracketOrder {
        return BracketOrder(main.clone(), profit.clone(), loss.clone())
    }

    override fun getValue(price: Double): Double {
        return main.getValue(price)
    }

    override fun execute(price: Double, time: Instant): List<Execution> {
        val result = mutableListOf<Execution>()
        place(price, time)

        if (!main.status.closed) {
            val e = main.execute(price, time)
            result.addAll(e)
            if (main.status.aborted) {
                status = main.status
                return result
            }
        }

        if (main.status == OrderStatus.COMPLETED) {

            if (profit.remaining != 0.0) {
                val e = profit.execute(price, time)
                result.addAll(e)
                loss.quantity = main.quantity - profit.fill
            }

            if (loss.remaining != 0.0) {
                val e = loss.execute(price, time)
                result.addAll(e)
                profit.quantity = main.quantity - loss.fill
            }

            if (profit.status.closed)
                status = profit.status
            else if (loss.status.closed)
                status = loss.status

            if (loss.fill + profit.fill == main.quantity) {
                status = OrderStatus.COMPLETED
            }
        }

        return result
    }
}