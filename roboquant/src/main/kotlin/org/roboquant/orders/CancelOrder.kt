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

package org.roboquant.orders

/**
 * Cancel an open create-order.
 *
 * @param order the order to cancel
 * @param tag an optional tag
 */
class CancelOrder(
    order: CreateOrder,
    tag: String = ""
) : ModifyOrder(order, tag) {

    /**
     * Create instance of CancelOrder based on the [OrderState] of an open order. This will throw an exception if
     * the order is not open any more or if the passed state doesn't contain a create-order.
     */
    constructor(state: OrderState, tag: String = "") : this(state.order as CreateOrder, tag) {
        require(state.open) { "only open orders can be cancelled" }
    }

    override fun info() = mapOf("modified-id" to order.id)
}
