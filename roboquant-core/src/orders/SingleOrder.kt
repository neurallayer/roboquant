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
 * SingleOrder types are plain non-combined orders with a pre-defined quantity and Time in Force policy. Many well-known
 * order types fall under this category, like Market-, Limit- and Trail-orders.
 */
interface SingleOrder : TradeOrder {

    val quantity: Double
    val tif: TimeInForce

    val sell
        get() = quantity < 0.0

    val buy
        get() = quantity > 0.0

    val direction
        get() = if (quantity > 0.0) 1 else if (quantity < 0.0) -1 else 0

}


data class MarketOrder(
    override val asset: Asset,
    override val quantity: Double,
    override val tif: TimeInForce = GTC(),
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : SingleOrder


data class LimitOrder(
    override val asset: Asset,
    override val quantity: Double,
    val limit: Double,
    override val tif: TimeInForce = GTC(),
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : SingleOrder



data class StopLimitOrder(
    override val asset: Asset,
    override val quantity: Double,
    val stop: Double,
    val limit: Double,
    override val tif: TimeInForce = GTC(),
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : SingleOrder


data class StopOrder(
    override val asset: Asset,
    override val quantity: Double,
    val stop: Double,
    override val tif: TimeInForce = GTC(),
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : SingleOrder


data class TrailOrder(
    override val asset: Asset,
    override val quantity: Double,
    val trailPercentage: Double,
    override val tif: TimeInForce = GTC(),
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : SingleOrder


data class TrailLimitOrder(
    override val asset: Asset,
    override val quantity: Double,
    val trailPercentage: Double,
    val limitOffset: Double,
    override val tif: TimeInForce = GTC(),
    override val id: String = Order.nextId(),
    override val state: OrderState = OrderState()
) : SingleOrder

