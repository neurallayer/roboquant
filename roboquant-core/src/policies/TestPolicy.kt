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
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.SingleOrder
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal

/**
 * Policy that buys or sells a fixed [quantity] of an asset, generating [MarketOrder]s
 *
 * Because of the deterministic behavior, this policy useful during testing/debugging a strategy. But it should not be
 * used in live trading of realistic back-tests.
 */
class TestPolicy(private val quantity: Double = 1.0) : BasePolicy() {

    /**
     * Create a buys or sells [MarketOrder] for an asset based on the received [signals]. It ignores the [account]
     * and [event] parameters.
     *
     * @see Policy.act
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<SingleOrder>()
        for (signal in signals.resolve()) {
            val order: MarketOrder? = when (signal.rating) {
                Rating.BUY, Rating.OUTPEFORM -> MarketOrder(signal.asset, quantity)
                Rating.SELL, Rating.UNDERPERFORM -> MarketOrder(signal.asset, -quantity)
                Rating.HOLD -> null
            }
            orders.addNotNull(order)
        }
        return orders
    }
}