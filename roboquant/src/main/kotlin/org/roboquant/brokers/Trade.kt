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

package org.roboquant.brokers

import org.roboquant.common.*
import java.time.Instant
import kotlin.math.absoluteValue

/**
 * Trade is created once an order has been (partially) filled and records various aspects of a trade like its [size],
 * [price] and [feeValue]. A single order can result in multiple trades, for example, if the order is filled in batches.
 *
 * All the monetary amounts are denoted in the currency of the underlying asset. One important metric that can be
 * derived from trades is the realized profit and loss [pnl].
 *
 * @property time The time of this trade
 * @property asset The underlying asset of this trade
 * @property size The size or volume of this trade, negative for selling assets
 * @property price The average price paid denoted in the currency of the asset excluding fee
 * @property feeValue total fee or commission charged as part of this trade denoted in the currency of the asset
 * @property pnlValue The realized profit & loss made by this trade denoted in the currency of the asset
 * @property orderId The id of the corresponding order
 * @constructor Create a new trade
 */
data class Trade(
    val time: Instant,
    val asset: Asset,
    val size: Size,
    val price: Double,
    val feeValue: Double,
    val pnlValue: Double,
    val orderId: String,
) {

    /**
     * Returns total cost amount of this trade, including any fees.
     */
    val totalCost: Amount
        get() = asset.value(size, price) + feeValue

    /**
     * Returns the fee amount
     */
    val fee: Amount
        get() = Amount(asset.currency, feeValue)

    /**
     * Returns the realized profit & loss amount of this trade
     */
    val pnl: Amount
        get() = Amount(asset.currency, pnlValue)

    /**
     * Returns the PNL as a percentage of the total trade
     */
    val pnlPercentage: Double
        get() = pnlValue / totalCost.value.absoluteValue
}

/**
 * Get the total fee for a collection of trades
 */
val Collection<Trade>.fee: Wallet
    get() = sumOf { it.fee }

/**
 * Get the timeline for a collection of trades
 */
val Collection<Trade>.timeline
    get() = map { it.time }.distinct().sorted()

/**
 * Return the total realized PNL for a collection of trades
 */
val Collection<Trade>.realizedPNL: Wallet
    get() = sumOf { it.pnl }

/**
 * Return the timeframe for a collection of trades
 */
val Collection<Trade>.timeframe: Timeframe
    get() = timeline.timeframe

/**
 * Convert the collection of trades to PNL percentage time-series
 */
fun Collection<Trade>.toPNLPercentageMetrics(): TimeSeries {
    return toTimeSeries { Pair(it.time, it.pnlPercentage) }
}

