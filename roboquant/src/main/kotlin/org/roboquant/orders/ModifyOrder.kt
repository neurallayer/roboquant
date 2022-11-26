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
 * message, so it can be processed. Only an order that is a [CreateOrder] can be updated.
 *
 * In real life, only certain parts of an open order can be updated, like the limit price of a limit order. For many
 * other types of changes, an order needs to be cancelled first and then a new order needs to be created.
 *
 * @property original the order you want to update
 * @property update the updated order
 * @constructor Create new UpdateOrder
 */
class UpdateOrder(
    val original: OrderState,
    val update: CreateOrder,
    tag: String = ""
) : ModifyOrder(original.order.asset, tag) {

    init {
        require(original.order::class == update::class) { "cannot update order type" }
        require(original.order.asset == update.asset) { "cannot update asset" }
        require(original.status.open) { "only open orders can be updated" }
    }
    override fun info() = update.info() + mapOf("modified-id" to original.orderId)
}

/**
 * Cancel an open order, will throw an exception if the order is not open anymore.
 *
 * @property state The order to cancel
 */
class CancelOrder(
    val state: OrderState,
    tag: String = ""
) : ModifyOrder(state.order.asset, tag) {

    init {
        require(state.status.open) { "only open orders can be cancelled" }
        require(state.order is CreateOrder) { "only create orders can be cancelled" }
    }

    override fun info() = mapOf("modified-id" to state.orderId)
}
