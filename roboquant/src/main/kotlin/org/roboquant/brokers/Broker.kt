/*
 * Copyright 2020-2023 Neural Layer
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
 * Interface for any broker implementation, used for both simulated and real brokers. All brokers also implement the
 * [Lifecycle] interface that allows them to manage internal state based on the phase of a run.
 */
interface Broker : Lifecycle {

    /**
     * Return a copy of the trading account that is guaranteed not to change after it has been returned.
     */
    val account: Account

    /**
     * Place a new set of [orders] at this broker. The [event] contains the latest data from the used feed.
     *
     * After processing the [orders], this method returns an instance of the updated [Account]. This returned instance
     * reflects the latest status, is immutable and thread safe. The just placed orders are always included
     * in the returned account object, either as open- or closed-orders.
     */
    fun place(orders: List<Order>, event: Event = Event.empty()): Account

    /**
     * This method will be invoked at each step in a run and provides the broker with the opportunity to
     * log additional information. The default implementation is to return an empty map.
     *
     * The returned map should NOT be mutated after it has been returned.
     */
    fun getMetrics(): MetricResults = emptyMap()

}
