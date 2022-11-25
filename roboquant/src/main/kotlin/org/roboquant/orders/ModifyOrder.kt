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

package org.roboquant.orders

/**
 * Update an existing open order. It is up to the broker implementation to translate the [update] order to the correct
 * message, so it can be processed. Currently, only an order that is a [SingleOrder] can be updated.
 *
 * In real life, only certain parts of an open order can be updated, like the limit price of a limit order. For many
 * other types of changes, an order needs to be cancelled first and then a new order needs to be created.
 *
 * Currently, there are no checks in the SimBroker if the updates would be possible in real life. So it is up to the
 * user to only create "sensible" update orders.
 *
 * @property original the order you want to update
 * @property update the updated order
 * @constructor Create new UpdateOrder
 */
class UpdateOrder(
    val original: OrderState,
    val update: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : Order(original.order.asset, id, tag) {

    init {
        require(original.order::class == update::class) { "update orders cannot change order type" }
        require(original.status.open) { "Only open orders can be updated" }
    }

    override fun info() = update.info()
}

/**
 * Cancel an open order, will throw an exception if the order is not open anymore.
 *
 * @property order The order to cancel
 * @param id
 */
class CancelOrder(
    val order: OrderState,
    id: Int = nextId(),
    tag: String = ""
) : Order(order.order.asset, id, tag) {

    init {
        require(order.status.open) { "Only open orders can be cancelled" }
    }

    override fun info() = order.order.info()
}