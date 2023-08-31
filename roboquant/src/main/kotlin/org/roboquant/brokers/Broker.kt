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
import org.roboquant.orders.Order
import java.time.Instant

/**
 * Interface for any broker implementation, used for both simulated and real brokers.
 * Brokers can also implement the [Lifecycle] interface that allows them to manage internal state.
 */
interface Broker : Lifecycle {

    /**
     * The state of the trading account since the last [sync]. This returns an immutable object.
     * Only invoking [sync] will result in creating a new instance of this object.
     */
    val account: Account

    /**
     * Sync the state of the roboquant with the broker.
     *
     * Typically, this method will invoke the underlying broker API to obtain the latest state of positions, orders,
     * trades, cash and buying power.
     *
     * Optionally an [event] can be provided, although normally only the SimBroker requires this to simulate
     * trade executions. If no event is provided, an empty event will be used instead.
     *
     * A sync will result in a new instance of the [account] object.
     */
    fun sync(event: Event = Event.empty())

    /**
     * Place new [orders] at this broker.
     *
     * Optional provide a [time] that can be used by the broker as a safety check to determine you are not submitting
     * back test orders to a real broker.
     */
    fun place(orders: List<Order>, time: Instant = Instant.now())

    /**
     * This method will be invoked at each step in a run and provides the broker with the opportunity to
     * provide additional metrics. The default implementation returns an empty map.
     */
    fun getMetrics(): Map<String, Double> = emptyMap()

}
