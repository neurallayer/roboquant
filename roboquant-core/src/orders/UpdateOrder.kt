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

import org.roboquant.brokers.sim.Execution
import java.time.Instant


/**
 * Instruction to update an SimpleOrder. It is up to the broker implementation to translate the updated order to the correct
 * message, so it can be processed.
 *
 * Typically, only a part of an open order can be updated, like the limit price of a limit order. For many other
 * types of changes, an order needs to be cancelled and a new one needs to be created.
 **
 * @property originalOrder
 * @property updateOrder
 * @constructor Create empty Order update
 */
class UpdateOrder<T : SingleOrder>(val originalOrder: T, val updateOrder: T) : Order(originalOrder.asset) {

    init {
        require(originalOrder.asset == updateOrder.asset)
    }

    override fun execute(price: Double, time: Instant): List<Execution> {
        when {
            originalOrder.status.closed -> status = OrderStatus.REJECTED
            updateOrder.quantity < originalOrder.fill -> status = OrderStatus.REJECTED
            else -> {
                originalOrder.quantity = updateOrder.quantity
            }
        }
        return listOf()
    }

    override fun getValue(price: Double): Double {
        return updateOrder.getValue(price)
    }

}