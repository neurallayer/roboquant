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
 * One Triggers Other order, if the primary order it executed, the secondary order will automatically be activated. Both
 * orders require the same asset.
 *
 * @property primary the primary order
 * @property secondary the secondary order
 * @param tag an optional tag
 * @constructor create a new instance of a OTOOrder
 */
class OTOOrder(
    val primary: SingleOrder,
    val secondary: SingleOrder,
    tag: String = ""
) : CreateOrder(primary.asset, tag) {

    init {
        require(primary.asset == secondary.asset) { "OTO orders can only contain orders for the same asset" }
    }

    override fun info() = sortedMapOf("first" to primary, "second" to secondary)

}