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
import org.roboquant.brokers.assets
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.CancelOrder
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

/**
 * Allow only a single open order per asset. Orders generated when there is an open order for the same asset, will be
 * removed or old orders will be cancelled.
 */
private class SingleOrderPolicy(private val policy: Policy, val action: String) : Policy by policy {

    private val logger = Logging.getLogger(this::class)

    init {
        require(action in setOf("remove", "cancel"))
    }

    private fun removeNew(orders: List<Order>, account: Account): List<Order> {
        val openOrderAssets = account.openOrders.assets
        return orders.filter { it.asset !in openOrderAssets }
    }

    private fun cancelOld(orders: List<Order>, account: Account): List<Order> {
        val newOrderAssets = orders.map { it.asset }.toSet()
        val duplicates = account.openOrders.filter { it.asset in newOrderAssets }
        return duplicates.map { CancelOrder(it) } + orders
    }

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = policy.act(signals, account, event)
        val newOrders = when {
            orders.isEmpty() -> orders
            action == "remove" -> removeNew(orders, account)
            else -> cancelOld(orders, account)
        }

        logger.debug { "orders in=${orders.size} out=${newOrders.size}" }
        return newOrders
    }
}


/**
 * Ensure there is only one open order per asset at any given time. There are two possible actions, either "remove"
 * new orders or "cancel" old orders (although that last [action] is not a guarantee since the cancellation might fail)
 *
 * Default is to remove new orders.
 */
fun Policy.singleOrder(action: String = "remove") : Policy = SingleOrderPolicy(this, action)
