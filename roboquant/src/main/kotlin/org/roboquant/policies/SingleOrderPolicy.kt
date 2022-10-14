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
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal

/**
 * Allow only a single open order per asset. Signals generated when there is an open order for the same asset, will be
 * removed before handing it over to the wrapped policy
 *
 * @property policy The policy to wrap
 */
private class SingleOrderPolicy(private val policy: Policy) : Policy by policy {

    private val logger = Logging.getLogger(this::class)

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = policy.act(signals, account, event)
        val openOrderAssets = account.openOrders.assets
        val newOrders = orders.filter { ! openOrderAssets.contains(it.asset) }
        logger.debug { "orders in=${orders.size} out=${orders.size}" }
        return newOrders
    }
}


/**
 * Ensure there is only a single open order per asset at any given time.
 */
fun Policy.singleOrder() : Policy = SingleOrderPolicy(this)
