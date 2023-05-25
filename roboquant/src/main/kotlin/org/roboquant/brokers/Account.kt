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

package org.roboquant.brokers

import org.roboquant.common.*
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import org.roboquant.orders.summary
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * The Account represents a brokerage trading account and is unified across all broker implementations.
 * This is an immutable class, and it holds the following state:
 *
 * - buying power
 * - base currency
 * - [cash] balances in the account
 * - the [positions] with its assets
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
 * @property openOrders List of [OrderState] of all open orders
 * @property closedOrders List of [OrderState] of all closed orders
 * @property positions List of all open [Position], with maximum one entry per asset
 * @property buyingPower amount of buying power available for trading
 * @constructor Create a new Account
 */
class Account(
    val baseCurrency: Currency,
    val lastUpdate: Instant,
    val cash: Wallet,
    val trades: List<Trade>,
    val openOrders: List<OrderState>,
    val closedOrders: List<OrderState>,
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
     * [equity] converted to a single amount denoted in the [baseCurrency] of the account
     */
    val equityAmount: Amount
        get() = convert(equity)

    /**
     * Total equity hold in the account. Equity is defined as sum of [cash] balances and the [positions] market value
     */
    val equity: Wallet
        get() = cash + positions.marketValue

    /**
     * Unique set of assets hold in the open [positions]
     */
    val assets: Set<Asset>
        get() = positions.map { it.asset }.toSet()

    /**
     * Get the associated trades for the provided [orders]. If no orders are provided all [closedOrders] linked to this
     * account instance are used.
     */
    fun getOrderTrades(orders: Collection<OrderState> = this.closedOrders): Map<OrderState, List<Trade>> =
        orders.associateWith { order -> trades.filter { it.orderId == order.orderId } }.toMap()

    /**
     * Convert an [amount] to the account [baseCurrency] using last update of the account as a timestamp
     */
    fun convert(amount: Amount, time: Instant = lastUpdate): Amount = amount.convert(baseCurrency, time)

    /**
     * Convert a [wallet] to the account [baseCurrency] using last update of the account as a timestamp
     */
    fun convert(wallet: Wallet, time: Instant = lastUpdate): Amount = wallet.convert(baseCurrency, time)

    /**
     * Returns a summary that contains the high-level account information, the available cash balances and
     * the open positions. Optionally the summary can convert wallets to a [singleCurrency], default is not.
     */
    fun summary(singleCurrency: Boolean = false): Summary {

        fun c(w: Wallet): Any {
            if (w.isEmpty()) return Amount(baseCurrency, 0.0)
            return if (singleCurrency) convert(w) else w
        }

        val s = Summary("account")
        val p = Summary("position summary")
        val t = Summary("trade summary")
        val o = Summary("order summary")

        s.add("last update", lastUpdate.truncatedTo(ChronoUnit.SECONDS))
        s.add("base currency", baseCurrency.displayName)
        s.add("cash", c(cash))
        s.add("buying power", buyingPower)
        s.add("equity", c(equity))

        s.add(p)
        p.add("open", positions.size)
        p.add("long", positions.long.size)
        p.add("short", positions.short.size)
        p.add("total value", c(positions.marketValue))
        p.add("long value", c(positions.long.marketValue))
        p.add("short value", c(positions.short.marketValue))
        p.add("unrealized p&l", c(positions.unrealizedPNL))

        s.add(t)
        t.add("total", trades.size)
        t.add("realized p&l", c(trades.realizedPNL))
        t.add("fee", trades.sumOf { it.fee })
        t.add("first", trades.firstOrNull()?.time ?: "")
        t.add("last", trades.lastOrNull()?.time ?: "")
        t.add("winners", trades.count { it.pnl > 0 })
        t.add("losers", trades.count { it.pnl < 0 })

        s.add(o)
        o.add("open", openOrders.size)
        o.add("closed", closedOrders.size)
        o.add("completed", closedOrders.filter { it.status == OrderStatus.COMPLETED }.size)
        o.add("cancelled", closedOrders.filter { it.status == OrderStatus.CANCELLED }.size)
        o.add("expired", closedOrders.filter { it.status == OrderStatus.EXPIRED }.size)
        o.add("rejected", closedOrders.filter { it.status == OrderStatus.REJECTED }.size)
        return s
    }

    /**
     * Provide a full summary of the account that contains all cash, orders, trades and open positions. During back
     * testing this can become a long list of items, so look at [summary] for a shorter summary.
     */
    fun fullSummary(singleCurrency: Boolean = false): Summary {
        val s = summary(singleCurrency)
        s.add(cash.summary())
        s.add(positions.summary())
        s.add(openOrders.summary("open orders"))
        s.add(closedOrders.summary("closed orders"))
        s.add(trades.summary())
        return s
    }


}
