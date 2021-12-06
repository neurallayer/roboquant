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

package org.roboquant.brokers

import org.roboquant.common.Component
import org.roboquant.feeds.Event
import org.roboquant.orders.Order

/**
 * Interface for any broker, both simulated and real live brokers.
 *
 */
interface Broker : Component {

    /**
     * The client account
     */
    val account: Account

    /**
     * Place new orders at this broker. After processing them, this method returns an instance of the
     * updated account. It is important that the placed orders are indeed included in the account, as either open or
     * closed.
     *
     * See also [Order]
     *
     * @param orders list of orders to be placed at the broker
     * @return the updated account that reflects the latest state
     */
    fun place(orders: List<Order>, event: Event): Account


}
