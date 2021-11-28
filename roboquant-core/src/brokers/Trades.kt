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

import org.roboquant.common.*
import java.time.Instant
import java.time.Period
import java.util.*


/**
 * List of trades. This is part of the [Account] and contains all the trades that occurred.
 *
 * @constructor Create new empty list of Trades
 */
class Trades : MutableList<Trade>, LinkedList<Trade>() {

    // Which are the assets referred to in these trades.
    val assets: List<Asset>
        get() = map { it.asset }.distinct()

    // What is the combined timeline for the trades
    val timeline: List<Instant>
        get() = map { it.time }.distinct().sorted()

    /**
     * Calculate the realized PnL for a certain asset and optionally a timeframe
     *
     * @param asset The asset
     * @param timeFrame An optional timeframe to restrict the calculation to
     * @return The P&L as a double value
     */
    fun realizedPnL(asset: Asset, timeFrame: TimeFrame? = null): Double {
        var filteredResults = asSequence().filter { it.asset == asset }
        if (timeFrame != null)
            filteredResults = filteredResults.filter { timeFrame.contains(it.time) }

        return filteredResults.fold(0.0) { result, trade -> result + trade.pnl }
    }

    /**
     * Realized PnL for an asset identified by its symbol name. If multiple assets are found with the same symbol
     * name, the first one will be used.
     *
     * @param symbol
     * @param timeFrame
     * @return
     */
    fun realizedPnL(symbol: String, timeFrame: TimeFrame? = null): Double {
        val asset = assets.first { it.symbol == symbol }
        return realizedPnL(asset, timeFrame)
    }

    /**
     * Calculate the total realized PnL for all assets and optionally a timeframe
     *
     * @param timeFrame An optional timeframe to restrict the PnL calculation to
     * @return The P&L as a cash value
     */
    fun realizedPnL(timeFrame: TimeFrame? = null): Cash {
        val result = Cash()
        var filteredResults = asSequence()
        if (timeFrame != null)
            filteredResults = filteredResults.filter { timeFrame.contains(it.time) }

        filteredResults.forEach {
            result.deposit(it.asset.currency, it.pnl)
        }
        return result
    }


    fun split(period: Period) = timeline.split(period)

    /**
     * Total fee, optionally limited to a specific timeframe. Fees are denoted in the
     * currency of the asset, so the returned Cash can hold different currencies.
     *
     * @param timeFrame
     * @return
     */
    fun totalFee(timeFrame: TimeFrame? = null): Cash {
        val result = Cash()
        val filteredResults = asSequence().filter(timeFrame)

        filteredResults.forEach {
            result.deposit(it.asset.currency, it.fee)
        }
        return result
    }

    private fun Sequence<Trade>.filter(timeFrame: TimeFrame?) : Sequence<Trade> {
        return if (timeFrame == null)
           this
        else
            filter { timeFrame.contains(it.time) }
    }

    /**
     * Total fee for a single asset denoted in the currency of the asset
     *
     * @param timeFrame
     * @return
     */
    fun totalFee(asset: Asset, timeFrame: TimeFrame? = null): Double {
        var filteredResults = asSequence().filter { it.asset == asset }
        filteredResults = filteredResults.filter(timeFrame)
        return filteredResults.fold(0.0) { result, order -> result + order.fee }
    }


    private fun Collection<Trade>.asTrades(): Trades {
        val result = Trades()
        result.addAll(this)
        return result
    }

    /**
     * Get a subset of these trades and return a new Trades instance.
     *
     * # Example how to print the first 10 trades
     *
     *      val t = account.trades[0..9]
     *      t.summary().print()
     *
     * @param range
     * @return
     */
    operator fun get(range: IntRange): Trades {
        return subList(Integer.max(0, range.first), Integer.min(this.size, range.last)).asTrades()
    }

    /**
     * Create a summary of the trades
     */
    fun summary(): Summary {
        val s = Summary("Trades")
        if (isEmpty()) {
            s.add("EMPTY")
        } else {
            val fmt = "%24s │%10s │%11s │%11s │%11s │%11s │%11s │"
            val header = String.format(fmt, "time", "asset", "qty", "amount", "fee", "p&l", "price")
            s.add(header)
            forEach {
                with(it) {
                    val amount = asset.currency.format(totalAmount)
                    val fee = asset.currency.format(fee)
                    val pnl = asset.currency.format(pnl)
                    val price = asset.currency.format(price)
                    val line = String.format(fmt, time, asset.symbol, quantity, amount, fee, pnl, price)
                    s.add(line)
                }
            }
        }
        return s
    }

}