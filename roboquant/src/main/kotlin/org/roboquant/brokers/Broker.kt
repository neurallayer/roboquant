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

package org.roboquant.brokers

import org.roboquant.common.Lifecycle
import org.roboquant.feeds.Event
import org.roboquant.orders.Order

/**
 * Interface for any broker implementation, used for both simulated and real brokers.
 * Brokers can also implement the [Lifecycle] interface that allows them to manage internal state.
 */
interface Broker : Lifecycle {

    /**
     * Sync the state of the roboquant with the broker.
     *
     * Typically, this method will invoke the underlying broker API to obtain the latest state of positions, orders,
     * trades, cash and buying power.
     *
     * Optionally an [event] can be provided, although normally only the SimBroker requires this to simulate
     * trade executions. If no event is provided, an empty event will be used instead.
     *
     * A sync will result in a new instance of the account object.
     */
    fun sync(event: Event = Event.empty()) : Account

    /**
     * Place new [orders] at this broker.
     */
    fun place(orders: List<Order>)

    /**
     * This method will be invoked at each step in a run and provides the broker with the opportunity to
     * provide additional metrics. The default implementation returns an empty map.
     */
    fun getMetrics(): Map<String, Double> = emptyMap()

}
