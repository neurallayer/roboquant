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

package org.roboquant.orders

import org.roboquant.common.Asset



/**
 * Order type that only modifies (update or cancel) other orders, it doesn't generate trades.
 */
interface ModifyOrder : Order


/**
 * Update an existing SimpleOrder. It is up to the broker implementation to translate the updated order to the correct
 * message, so it can be processed.
 *
 * Typically, only a part of an open order can be updated, like the limit price of a limit order. For many other
 * types of changes, an order needs to be cancelled and a new one needs to be created.
 **
 * @property original
 * @property update
 * @constructor Create empty Order update
 */

data class UpdateOrder<T : SingleOrder>(
    val original: T,
    val update: T,
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : ModifyOrder {

    init {
        require(original.status.open)
        require(original.status == OrderStatus.INITIAL)
        require(original.asset == update.asset)
    }

    override val asset: Asset
        get() = original.asset

}


data class CancellationOrder(
    val order: Order,
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : ModifyOrder {

    init {
        require(order.status.open)
    }

    override val asset: Asset
        get() = order.asset

}
