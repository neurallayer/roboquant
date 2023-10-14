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

package org.roboquant.orders

/**
 * Update an existing open order. It is up to the broker implementation to translate the [update] order to the
 * correct message, so it can be processed. Only an order that is a [CreateOrder] can be updated.
 *
 * The SimBroker, like real brokers, can only modify open orders. In live trading, only certain parts of an open order
 * can be updated, like the limit price of a limit order. For many other types of changes, an order needs to be
 * cancelled first and then a new order needs to be created.
 *
 * @param order the order you want to update
 * @property update the updated order, of the same type and asset as the original order
 * @param tag an optional tag to link to this order
 * @constructor Create new UpdateOrder
 */
class UpdateOrder(
    order: CreateOrder,
    val update: CreateOrder,
    tag: String = ""
) : ModifyOrder(order, tag) {

    /**
     * Create instance of UpdateOrder based on the [OrderState] of an open order. This will throw an exception if
     * the order is not open any more or if the passed state doesn't contain a create-order.
     */
    constructor(state: OrderState, update: CreateOrder, tag: String = "") : this(
        state.order as CreateOrder,
        update,
        tag
    ) {
        require(state.open) { "only open orders can be updated" }
    }

    init {
        require(order::class == update::class) { "cannot update order type old=${order::class} new=${update::class}" }
        require(order.asset == update.asset) { "cannot update the asset old=${order.asset} new=${update.asset}" }
        update.id = order.id
    }

    override fun info() = update.info() + mapOf("modified-id" to order.id)
}
