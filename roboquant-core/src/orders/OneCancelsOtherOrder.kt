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

import java.time.Instant


/**
 * Creates an OCO (One Cancels Other) order. If one order is (partially) filled, the other order will be cancelled. So
 * at most only one of the two orders will be executed.
 *
 * @constructor
 *
 */
class OneCancelsOtherOrder(
    val first: SingleOrder,
    val second: SingleOrder,
) : CombinedOrder(first, second) {


    override fun clone(): OneCancelsOtherOrder {
        return OneCancelsOtherOrder(first.clone(), second.clone())
    }

     fun execute(price: Double, time: Instant): Double {
        var qty = 0.0

        if (first.status.open) {
            qty += first.execute(price, time)
            status = first.status
            if (first.executed) second.status = OrderStatus.CANCELLED
        }

        if (second.status.open) {
            qty += second.execute(price, time)
            status = second.status
            if (second.executed) first.status = OrderStatus.CANCELLED
        }

        return qty
    }
}


