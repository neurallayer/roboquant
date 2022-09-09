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

package org.roboquant.brokers

import org.roboquant.common.Lifecycle
import org.roboquant.feeds.Event
import org.roboquant.metrics.MetricResults
import org.roboquant.orders.Order

/**
 * Interface for any broker implementation, used for both simulated and real brokers.
 */
interface Broker : Lifecycle {

    /**
     * Return a snapshot of the trading account that is guaranteed not to change after it has been returned.
     */
    val account: Account

    /**
     * Place new [orders] at this broker. The [event] is useful for simulated brokers, but can also function
     * as a safeguard for other brokers (like not allowing to place orders when the current price is unknown).
     *
     * After processing the [orders], this method returns an instance of the updated account. The returned instance of
     * [Account] should reflect the latest status and be considered thread safe. It is important that the placed orders
     * are already included in the account.
     */
    fun place(orders: List<Order>, event: Event): Account

    /**
     * This will be invoked at each step in a run and provides the implementation with the opportunity to log additional
     * information. The default implementation is to return an empty map.
     *
     * This map should NOT be mutated after it has been returned by this method.
     */
    fun getMetrics(): MetricResults = emptyMap()

}
