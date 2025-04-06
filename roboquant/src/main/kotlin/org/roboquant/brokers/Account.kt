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
import org.roboquant.orders.Order
import java.time.Instant

/**
 * An account represents a brokerage trading account and is unified across all broker implementations.
 * It holds the following state:
 *
 * - The buying power
 * - The base currency
 * - All the [cash] balances in the account
 * - All the open [positions] with its assets
 * - The [orders]
 *
 * Some convenience methods convert a multi-currency Wallet to a single-currency Amount.
 * For this to work, you'll need to have the appropriate exchange rates defined
 * at [Config.exchangeRates].
 *
 * @property baseCurrency the base currency of the account
 * @property lastUpdate time that the account was last updated
 * @property cash cash balances
 * @property orders List of [Order] of all open orders
 * @property positions List of all open [Position], with maximum one entry per asset
 * @property buyingPower amount of buying power available for trading
 * @constructor Create a new Account
 */
interface Account {

    val baseCurrency: Currency
    val lastUpdate: Instant
    val cash: Wallet
    val orders: List<Order>
    val positions: Map<Asset, Position>
    val buyingPower: Amount

    /**
     * Cash balances converted to a single amount denoted in the [baseCurrency] of the account. If you want to know
     * how much cash is available for trading, please use [buyingPower] instead.
     */
    val cashAmount: Amount
        get() = convert(cash)

    /**
     * [equity] converted to a single amount denoted in the [baseCurrency] of the account.
     */
    fun equityAmount(): Amount = convert(equity())

    /**
     * Calculate the total equity hold in the account and return the result as a [Wallet]
     * Equity is defined as the sum of [cash] balances and the market value of the open [positions].
     */
    fun equity(): Wallet = cash + marketValue()

    /**
     * The unique set of assets hold in the open [positions]
     */
    val assets: Set<Asset>
        get() = positions.keys

    /**
     * Return the market value of the open positions, optionally filter by one or more asset.
     */
    fun marketValue(vararg assets: Asset): Wallet {
        val v = positions.filterKeys { assets.isEmpty() || it in assets }
        val result = Wallet()
        for ((asset, position) in v) {
            val positionValue = asset.value(position.size, position.mktPrice)
            result.deposit(positionValue)
        }
        return result
    }

    /**
     * Return the position size for the provided asset within this account. If there is no open position,
     * a size of ZERO is returned.
     */
    fun positionSize(asset: Asset) : Size = positions[asset]?.size ?: Size.ZERO

    /**
     * Return the unrealized PNL of the open positions, optionally filter by one or more asset. If there is
     * not match, an empty [Wallet] will be returned.
     */
    fun unrealizedPNL(vararg assets: Asset): Wallet {
        val v = positions.filterKeys { assets.isEmpty() || it in assets }
        val result = Wallet()
        for ((asset, position) in v) {
            val positionValue = asset.value(position.size, position.mktPrice - position.avgPrice)
            result.deposit(positionValue)
        }
        return result
    }

    /**
     * Convert an [amount] to the account [baseCurrency] using last update of the account as a timestamp
     */
    fun convert(amount: Amount): Amount = amount.convert(baseCurrency, lastUpdate)

    /**
     * Convert a [wallet] to the account [baseCurrency] using last update of the account as a timestamp
     */
    fun convert(wallet: Wallet): Amount = wallet.convert(baseCurrency, lastUpdate)

}
