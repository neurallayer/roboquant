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

import org.roboquant.common.Asset
import java.time.Instant

/**
 * Trade is created once an order has been (partially) filled and records various aspects of a trade
 * like quantity, price and fee. Trades are not updated once created, and all its properties are
 * immutable.
 *
 * A single order can result in multiple trades, for example is an order is filled in batches.
 *
 * All the monetary amounts are denoted in the currency of the underlying asset. One important metric that can be
 * derived from trades is the realized PnL. If a broker implementation doesn't provide a certain attribute, it is
 * expected to use Double.NaN instead.
 *
 * @property time The time of this trade
 * @property asset The underlying asset of this trade
 * @property quantity The quantity or volume of this trade
 * @property price The effective price paid denoted in the currency of the asset
 * @property totalAmount The total monetary amount of this trade denoted in the currency of the asset
 * @property fee Any brokerage fees or commission charged as part of this trade denoted in the currency of the asset
 * @property pnl The realized profit & loss made by this trade denoted in the currency of the asset
 * @constructor Create a new trade
 */
class Trade(
    val time: Instant,
    val asset: Asset,
    val quantity: Double,
    val price: Double,
    val totalAmount: Double,
    val fee: Double,
    val pnl: Double,
    val orderId: String,
)