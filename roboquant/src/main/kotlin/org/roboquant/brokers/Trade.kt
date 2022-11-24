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

package org.roboquant.brokers

import org.roboquant.common.*
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Trade is created once an order has been (partially) filled and records various aspects of a trade like its [size],
 * [price] and [feeValue]. A single order can result in multiple trades, for example if an order is filled in batches.
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
    val orderId: Int,
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

}


/**
 * Get the total fee for a collection of trades
 */
val Collection<Trade>.fee : Wallet
    get() = sumOf { it.fee }

/**
 * Get the timeline for a collection of trades
 */
val Collection<Trade>.timeline
    get() = map { it.time }.distinct().sorted()

/**
 * Return the total realized PNL for a collection of trades
 */
val Collection<Trade>.realizedPNL : Wallet
    get() = sumOf { it.pnl }

/**
 * Return the timeframe for a collection of trades
 */
val Collection<Trade>.timeframe : Timeframe
    get() = timeline.timeframe

/**
* Create a summary of the trades
*/
@JvmName("summaryTrades")
fun Collection<Trade>.summary(name: String = "trades"): Summary {
    val s = Summary(name)
    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val lines = mutableListOf<List<Any>>()
        lines.add(listOf("time", "symbol", "ccy", "size", "cost", "fee", "rlzd p&l", "price"))
        forEach {
            with(it) {
                val currency = asset.currency
                val cost = totalCost.formatValue()
                val fee = fee.formatValue()
                val pnl = pnl.formatValue()
                val price = Amount(currency, price).formatValue()
                val t = time.truncatedTo(ChronoUnit.SECONDS)
                lines.add(listOf(t, asset.symbol, currency.currencyCode, size, cost, fee, pnl, price))
            }
        }
        return lines.summary(name)
    }
    return s
}

