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

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Asset
import java.time.Instant

/**
 * Trade is created once an order has been (partially) filled and records various aspects of a trade like quantity,
 * price and fee. A single order can result in multiple trades, for example is an order is filled in batches.
 *
 * All the monetary amounts are denoted in the currency of the underlying asset. One important metric that can be
 * derived from trades is the realized PnL.
 *
 * @property time The time of this trade
 * @property asset The underlying asset of this trade
 * @property quantity The quantity or volume of this trade, negative for selling assets
 * @property price The average price paid denoted in the currency of the asset excluding fee
 * @property fee Any brokerage fees or commission charged as part of this trade denoted in the currency of the asset
 * @property pnl The realized profit & loss made by this trade denoted in the currency of the asset
 * @constructor Create a new trade
 */
data class Trade(
    val time: Instant,
    val asset: Asset,
    val quantity: Double,
    val price: Double,
    val feeValue: Double,
    val pnlValue: Double,
    val orderId: String,
) {

    /**
     * total cost of this trade, including the fee. If the trade generated revenue (for example by selling assets) this
     * will return a negative value.
     */
    val totalCost
        get() = Amount(asset.currency, price * quantity * asset.multiplier + feeValue)

    val fee
        get() = Amount(asset.currency, feeValue)

    val pnl
        get() = Amount(asset.currency, pnlValue)

    val priceAmount
        get() = Amount(asset.currency, price)

}

