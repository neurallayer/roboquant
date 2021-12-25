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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.SingleOrder
import org.roboquant.strategies.Signal
import kotlin.math.absoluteValue
import kotlin.math.floor

/**
 * Policy will try to maintain a certain balanced between assets based on predefined weights for
 * these assets. Assets that don't have a weight assigned to them, will not be traded.
 *
 * The policy will only BUY or SELL an asset if it receives a signal, so it won't perform automatic re-balancing
 * of the portfolio.
 *
 * @constructor
 *
 * @param weight
 */
class WeightedPolicy(vararg weight: Pair<Asset, Double>) : Policy {

    private val weights = mutableMapOf<Asset, Double>()

    init {
        val total = weight.map { it.second }.reduce { acc, d -> acc + d }
        for ((k, v) in weight) {
            weights[k] = v / total
        }
    }

    private fun getSizing(asset: Asset, pos: Position, totalValue: Double, price: Double): Double {
        val prefValue = totalValue * weights[asset]!!
        val prefSize = prefValue / (price * asset.multiplier)
        val diffSize = floor(pos.quantity - prefSize)
        if (diffSize.absoluteValue > 1.0) {
            return diffSize * price * asset.multiplier
        }
        return 0.0
    }


    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = mutableListOf<SingleOrder>()
        val value = account.getMarketValue()
        val totalValue = account.convertToCurrency(value, now = event.now)
        for (signal in signals) {
            val asset = signal.asset
            if (weights.containsKey(asset)) {
                val pos = account.portfolio.getPosition(asset)
                val price = event.getPrice(asset)
                if (price !== null) {
                    val size = getSizing(asset, pos, totalValue, price)
                    if (signal.rating.isPositive && size > 0) {
                        orders.add(MarketOrder(asset, size))
                    } else if (signal.rating.isNegative && size < 0) {
                        orders.add(MarketOrder(asset, size))
                    }
                }
            }
        }
        return orders
    }
}