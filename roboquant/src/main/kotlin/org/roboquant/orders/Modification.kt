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
 * Update an existing open order. It is up to the broker implementation to translate the [update] order to the
 * correct message, so it can be processed. Only an order that is a [Order] can be updated.
 *
 * The SimBroker, like real brokers, can only modify open orders. In live trading, only certain parts of an open order
 * can be updated, like the limit price of a limit order. For many other types of changes, an order needs to be
 * cancelled first and then a new order needs to be created.
 *
 * @param orderId the id of the order you want to update
 * @property update the updated order, of the same type and asset as the original order
 * @constructor Create new Modification
 */
class Modification(
    val orderId: String,
    val update: Order,
    val tag: String = ""
) : Instruction()
