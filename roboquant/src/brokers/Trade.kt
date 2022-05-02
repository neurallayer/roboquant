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
import org.roboquant.common.Size
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
 * @property size The size or volume of this trade, negative for selling assets
 * @property price The average price paid denoted in the currency of the asset excluding fee
 * @property feeValue total fee or commission charged as part of this trade denoted in the currency of the asset
 * @property pnlValue The realized profit & loss made by this trade denoted in the currency of the asset
 * @property orderId The corresponding order id
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
     * Returns total cost amount of this trade, including the fee.
     */
    val totalCost
        get() = asset.value(size, price) + feeValue

    /**
     * Returns the fee amount
     */
    val fee
        get() = Amount(asset.currency, feeValue)

    /**
     * Returns the profit & loss amount
     */
    val pnl
        get() = Amount(asset.currency, pnlValue)

}

