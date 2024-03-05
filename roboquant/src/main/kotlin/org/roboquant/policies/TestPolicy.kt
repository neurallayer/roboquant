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
import org.roboquant.common.Size
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.SingleOrder
import org.roboquant.strategies.Signal

/**
 * Policy that buys or sells a fixed [size] of an asset based on the signal. It will always generate [MarketOrder]s
 *
 * Because of this deterministic behavior, this policy is useful during testing/debugging of a strategy. But it
 * should not be used in live trading or realistic back-tests.
 */
class TestPolicy(private val size: Size = Size.ONE) : BasePolicy() {

    /**
     * Create a buys or sells [MarketOrder] for an asset based on the received [signals]. It ignores the [account]
     * and [event] parameters.
     *
     * @see Policy.act
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<SingleOrder>()
        for (signal in signals) {
            val order: MarketOrder? = when  {
                signal.isPositive  -> MarketOrder(signal.asset, size)
                signal.isNegative -> MarketOrder(signal.asset, -size)
                else -> null
            }
            orders.addNotNull(order)
        }
        return orders
    }
}
