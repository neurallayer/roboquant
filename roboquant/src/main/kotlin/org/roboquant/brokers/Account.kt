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
import org.roboquant.orders.CreateOrder
import java.time.Instant

/**
 * An account represents a brokerage trading account and is unified across all broker implementations.
 * This is an immutable class, and it holds the following state:
 *
 * - The buying power
 * - The base currency
 * - All the [cash] balances in the account
 * - All the open [positions] with its assets
 * - The past [trades]
 * - The [openOrders] and [closedOrders] state
 *
 * Some convenience methods convert a multi-currency Wallet to a single-currency Amount.
 * For this to work, you'll
 * need to have the appropriate exchange rates defined at [Config.exchangeRates].
 *
 * @property baseCurrency the base currency of the account
 * @property lastUpdate time that the account was last updated
 * @property cash cash balances
 * @property trades List of all executed trades
 * @property openOrders List of [Order] of all open orders
 * @property closedOrders List of [Order] of all closed orders
 * @property positions List of all open [Position], with maximum one entry per asset
 * @property buyingPower amount of buying power available for trading
 * @constructor Create a new Account
 */
class Account(
    val baseCurrency: Currency,
    val lastUpdate: Instant,
    val cash: Wallet,
    val trades: List<Trade>,
    val openOrders: List<CreateOrder>,
    val closedOrders: List<CreateOrder>,
    val positions: List<Position>,
    val buyingPower: Amount
) {

    init {
        require(buyingPower.currency == baseCurrency) {
            "Buying power $buyingPower needs to be expressed in the baseCurrency $baseCurrency"
        }
    }

    /**
     * Cash balances converted to a single amount denoted in the [baseCurrency] of the account. If you want to know
     * how much cash is available for trading, please use [buyingPower] instead.
     */
    val cashAmount: Amount
        get() = convert(cash)

    /**
     * [equity] converted to a single amount denoted in the [baseCurrency] of the account.
     */
    val equityAmount: Amount
        get() = convert(equity)

    /**
     * Total equity hold in the account.
     * Equity is defined as the sum of [cash] balances and the market value of the open [positions].
     */
    val equity: Wallet
        get() = cash + positions.marketValue

    /**
     * Unique set of assets hold in the open [positions]
     */
    val assets: Set<Asset>
        get() = positions.map { it.asset }.toSet()



    /**
     * Convert an [amount] to the account [baseCurrency] using last update of the account as a timestamp
     */
    fun convert(amount: Amount, time: Instant = lastUpdate): Amount = amount.convert(baseCurrency, time)

    /**
     * Convert a [wallet] to the account [baseCurrency] using last update of the account as a timestamp
     */
    fun convert(wallet: Wallet, time: Instant = lastUpdate): Amount = wallet.convert(baseCurrency, time)


    override fun toString(): String {

        val positionString  = positions.joinToString(limit = 20) { "${it.size}@${it.asset.symbol}" }

        return """
            last update  : $lastUpdate
            base currency: $baseCurrency
            cash         : $cash
            buying Power : $buyingPower
            equity       : $equity
            positions    : ${positionString.ifEmpty { "-" }}
            open orders  : ${openOrders.size}
            closed orders: ${closedOrders.size}
            trades       : ${trades.size}
        """.trimIndent()

    }


}
