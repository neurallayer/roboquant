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

import org.apache.commons.math3.stat.descriptive.rank.Percentile
import org.roboquant.common.*
import org.roboquant.orders.OrderState
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue


/**
 * Account represents a unified brokerage trading account and holds the following state:
 *
 * - [cash] balances in the account
 * - the [portfolio] with its assets
 * - The past [trades]
 * - The [orders] and theor state, both open (still in progress) and already closed ones
 *
 * It also supports multi-currency trading through the use of a pluggable currency converter. Without configuring such
 * plug-in it still supports single currency trading, so when all assets and cash balances are denoted in a single
 * currency.
 *
 * This is a immutable class.
 *
 * @property baseCurrency The base currency to use for things like reporting
 * @constructor Create a new Account
 */
class Account(
    val baseCurrency: Currency,
    val lastUpdate: Instant,
    val cash: Wallet,
    val trades: List<Trade>,
    val orders: List<OrderState>,
    val portfolio: List<Position>,
    val buyingPower: Amount
) : Summarizable {

    val cashAmount
        get() = convert(cash)

    val equityAmount
        get() = convert(equity)

    val fee
        get() = trades.sumOf { it.fee }

    val equity
        get() = cash + portfolio.value

    /**
     * Assets in the portfolio
     */
    val assets
        get() = portfolio.map { it.asset }

    /**
     * Gte the associated trades for the provided [orders]. If no orders are provided all orders linked to this account
     * instance are used.
     */
    fun getOrderTrades(orders: Collection<OrderState> = this.orders) =
        orders.associateWith { order -> trades.filter { it.orderId == order.id } }.toMap()

    /**
     * Convert an [amount] to the account base currency using last update of the account as a timestamp
     */
    fun convert(amount: Amount) = amount.convert(baseCurrency, lastUpdate)

    /**
     * Convert a [wallet] to the account base currency using last update of the account as a timestamp
     */
    fun convert(wallet: Wallet) = wallet.convert(baseCurrency, lastUpdate)


    /**
     * Provide a short summary that contains the high level account information, the available cash balances and
     * the open positions in the portfolio.
     *
     * @return The summary
     */
    override fun summary(singleCurrency: Boolean): Summary {

        fun c(w: Wallet): Any = if (singleCurrency) convert(w) else w

        val s = Summary("Account")
        s.add("last update", lastUpdate.truncatedTo(ChronoUnit.SECONDS))
        s.add("base currency", baseCurrency.displayName)
        s.add("cash", c(cash))
        s.add("buying power", buyingPower)
        s.add("equity", c(equity))
        s.add("portfolio", c(portfolio.value))
        s.add("long value", c(portfolio.long.value))
        s.add("short value", c(portfolio.short.value))
        s.add("assets", portfolio.size)
        s.add("realized p&l", c(trades.realizedPNL))
        s.add("trades", trades.size)
        s.add("orders", orders.size)
        s.add("unrealized p&l", c(portfolio.unrealizedPNL))
        return s
    }

    /**
     * Provide a full summary of the account that contains all cahs, orders, trades and open positions. During back
     * testing this can become a long list of items, so look at [summary] for a shorter summary.
     */
    fun fullSummary(): Summary {
        val s = summary()
        s.add(cash.summary())
        s.add(portfolio.summary())
        s.add(orders.summary())
        s.add(trades.summary())
        return s
    }

}


fun Collection<Trade>.outliers(percentage: Double = 0.95): List<Trade> {
    val data = map { it.pnl.value.absoluteValue }.toDoubleArray()
    val p = Percentile()
    val boundary = p.evaluate(data, percentage * 100.0)
    return filter { it.pnl.value.absoluteValue >= boundary }
}

fun Collection<Trade>.inliers(percentage: Double = 0.95): List<Trade> {
    val data = map { it.pnl.value.absoluteValue }.toDoubleArray()
    val p = Percentile()
    val boundary = p.evaluate(data, percentage * 100.0)
    return filter { it.pnl.value.absoluteValue < boundary }
}

val Collection<Trade>.fee
    get() = sumOf { it.fee }

val Collection<Trade>.timeline
    get() = map { it.time }.distinct().sorted()

val Collection<Position>.assets
    get() = map { it.asset }.distinct()

val Collection<Position>.long
    get() = filter { it.long }

val Collection<Position>.unrealizedPNL
    get() = sumOf { it.unrealizedPNL }

val Collection<Trade>.realizedPNL
    get() = sumOf { it.pnl }

val Collection<Trade>.timeframe
    get() = timeline.timeframe

val Collection<Position>.short
    get() = filter { it.short }

fun Collection<Position>.getPosition(asset: Asset) = find { it.asset == asset } ?: Position.empty(asset)

fun Collection<Position>.diff(target: Collection<Position>): Map<Asset, Double> {
    val result = mutableMapOf<Asset, Double>()

    for (position in target) {
        // Use BigDecimal to avoid inaccuracies
        val targetSize = BigDecimal.valueOf(position.size)
        val sourceSize = BigDecimal.valueOf(getPosition(position.asset).size)
        val value = (targetSize - sourceSize).toDouble()
        if (value != 0.0) result[position.asset] = value
    }

    for (position in this) {
        if (position.asset !in result) result[position.asset] = -position.size
    }

    return result
}


/**
 * Provide a summary for the orders
 */
@JvmName("summaryOrders")
fun Collection<OrderState>.summary(): Summary {
    val s = Summary("Orders")
    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val fmt = "%15s │%10s │%15s │%10s │%24s │%24s │%10s │ %-50s"
        val header = String.format(fmt, "type", "asset", "status", "id", "opened at", "closed at", "currency", "details")
        s.add(header)
        forEach {
            with(it) {
                val t1 = openedAt.truncatedTo(ChronoUnit.SECONDS)
                val t2 = closedAt.truncatedTo(ChronoUnit.SECONDS)
                val infoString = order.info().toString().removeSuffix("}").removePrefix("{")
                val line = String.format(fmt, order.type, asset.symbol, status, order.id, t1, t2, asset.currencyCode, infoString)
                s.add(line)
            }
        }
    }
    return s
}


/**
 * Create a summary of the trades
 */
@JvmName("summaryTrades")
fun Collection<Trade>.summary(): Summary {

    val s = Summary("Trades")
    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val fmt = "%24s│%10s│%11s│%14s│%14s│%14s│%12s│"
        val header = String.format(fmt, "time", "asset", "qty", "cost", "fee", "p&l", "price")
        s.add(header)
        forEach {
            with(it) {
                val cost = totalCost.formatValue()
                val fee = fee.formatValue()
                val pnl = pnl.formatValue()
                val price = Amount(asset.currency, price).formatValue()
                val t = time.truncatedTo(ChronoUnit.SECONDS)
                val line = String.format(fmt, t, asset.symbol, quantity, cost, fee, pnl, price)
                s.add(line)
            }
        }
    }
    return s
}


/**
 * Create a [Summary] of this portfolio that contains an overview of the open positions.
 */
@JvmName("summaryPositions")
fun Collection<Position>.summary(): Summary {
    val s = Summary("Portfolio")

    val pf = DecimalFormat("############")
    pf.minimumFractionDigits = 0
    pf.maximumFractionDigits = 4

    if (isEmpty()) {
        s.add("EMPTY")
    } else {
        val fmt = "%14s│%10s│%14s│%14s│%14s│%14s│%14s│"
        val header = String.format(
            fmt,
            "asset",
            "currency",
            "quantity",
            "entry price",
            "market price",
            "market value",
            "unrlzd p&l"
        )
        s.add(header)

        for (v in this) {
            val c = v.asset.currency
            val pos = pf.format(v.size)
            val avgPrice = Amount(c, v.avgPrice).formatValue()
            val price = Amount(c, v.spotPrice).formatValue()
            val value = v.marketValue.formatValue()
            val pnl = Amount(c, v.unrealizedPNL.value).formatValue()
            val asset = "${v.asset.type}:${v.asset.symbol}"
            val line = String.format(fmt, asset, v.currency.currencyCode, pos, avgPrice, price, value, pnl)
            s.add(line)
        }
    }

    return s
}
