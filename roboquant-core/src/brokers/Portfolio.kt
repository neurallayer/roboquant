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
import org.roboquant.common.Summary
import org.roboquant.common.Wallet
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

/**
 * Portfolio holds the [positions][Position] within an account. For a given asset there can only be one position at
 * a time.
 *
 *
 * @constructor Create a new empty Portfolio
 */
class Portfolio : Cloneable {

    // Use a treemap to store positions in order to make results more reproducable
    private val _positions = TreeMap<Asset, Position>()

    /**
     * Get the open positions in this portfolio, both short and long
     * positions will be returned.
     */
    val positions
        get() = _positions.values

    /**
     * Long positions contained in this portfolio
     */
    val longPositions
        get() = _positions.values.filter { it.long }

    /**
     * The assets hold in this portfolio that currently have an open position
     */
    val assets
        get() = positions.map { it.asset }.sorted()

    /**
     * Short positions contained in this portfolio
     */
    val shortPositions
        get() = _positions.values.filter { it.short }

    /**
     * Is this portfolio empty? Or in other words does it not contain positions with a quantity unequal to zero
     */
    fun isEmpty(): Boolean = _positions.isEmpty()


    /**
     * Create a clone of the portfolio and return it. A clone is guaranteed not to be mutated
     * by an ongoing run and so reflects the portfolio at the moment in time this method is called.
     */
    public override fun clone(): Portfolio {
        val portfolio = Portfolio()
        portfolio._positions.putAll(_positions)
        return portfolio
    }

    /**
     * Update the portfolio with the provided [position] and return the realized PnL.
     */
    fun updatePosition(position: Position): Amount {
        val asset = position.asset
        val currentPos = _positions.getOrDefault(asset, Position.empty(asset))
        val newPosition = currentPos + position
        if (newPosition.closed) _positions.remove(asset) else  _positions[asset] = newPosition
        return currentPos.realizedPNL(position)
    }

    /**
     * Create the trade prices for all positions in the portfolio.
     */
    fun toTradePrices(): List<TradePrice> {
        return positions.map { TradePrice(it.asset, it.spotPrice) }

    }

    /**
     * Update the open positions in the portfolio with the current market prices as found in the [event]
     */
    fun updateMarketPrices(event: Event, priceType: String = "DEFAULT") {
        val prices = event.prices

        for (position in positions) {
            val priceAction = prices[position.asset]
            if (priceAction != null) {
                val price = priceAction.getPrice(priceType)
                val newPosition = position.copy(spotPrice = price, lastUpdate = event.time)
                _positions[position.asset] = newPosition
            }
        }
    }


    /**
     * Replace a position in the portfolio with a new [position]. So this overwrites the previous position
     * This will be used for example with live trading when the broker holds the true state of the positions
     * in a portfolio.
     */
    fun setPosition(position: Position) {
        if (position.closed) {
            _positions.remove(position.asset)
        } else {
            _positions[position.asset] = position
        }
    }


    /**
     * Get total value of this portfolio. The total value is calculated based on the sum of the open positions and
     * their last known price. The result is returned as a [Wallet] object, so no currency conversion is applied.
     */
    val value: Wallet
        get() = positions.value

    val longValue : Wallet
        get() = longPositions.value

    val shortValue : Wallet
        get() = shortPositions.value



    /**
     * Get total exposure of this portfolio. The total value is calculated based on the sum of the open positions and
     * their last known price. The result is returned as a [Wallet] object, so no currency conversion is applied.
     */
    fun getExposure(): Wallet {
        val result = Wallet()
        for (position in positions) {
            result.deposit(position.exposure)
        }
        return result
    }


    /**
     * Get total unrealized PNL of this portfolio. The unrealized PNL is calculated based on the open positions and
     * their average price and last known price. The result is returned as a [Wallet] object, so no currency conversion
     * is applied.
     */
    fun unrealizedPNL(): Wallet {
        val result = Wallet()
        for (position in positions) {
            result.deposit(position.unrealizedPNL)
        }
        return result
    }


    /**
     * Get the position for an [asset]. If the portfolio doesn't hold the asset, it returns an empty position.
     */
    fun getPosition(asset: Asset): Position {
        return _positions.getOrElse(asset) { Position.empty(asset) }
    }

    /**
     * Clear all the positions hold in this portfolio.
     */
    fun clear() = _positions.clear()


    /**
     * Create a [Summary] of this portfolio that contains an overview the open positions.
     */
    fun summary(): Summary {
        val s = Summary("Portfolio")

        val pf = DecimalFormat("#######")
        val ps = positions
        if (ps.isEmpty()) {
            s.add("EMPTY")
        } else {
            val fmt = "│%14s│%6s│%11s│%11s│%11s│%11s│"
            val header = String.format(fmt, "asset", "curr", "qty", "avgPrice", "spot", "p&l")
            s.add(header)

            for (v in ps) {
                val c = v.asset.currency
                val pos = pf.format(v.size)
                val avgPrice = Amount(c, v.avgPrice).formatValue()
                val price =  Amount(c,v.spotPrice).formatValue()
                val pnl =  Amount(c,v.unrealizedPNL.value).formatValue()
                val asset = "${v.asset.type}:${v.asset.symbol}"
                val line = String.format(fmt, asset, v.currency.currencyCode, pos, avgPrice, price, pnl)
                s.add(line)
                //s.add("${p.type}:${p.symbol}", "$pos @ $cost ($price)")
            }
        }

        return s
    }


    /**
     * Add all the positions of an [other] portfolio to this portfolio.
     */
    fun add(other: Portfolio) {
        for (position in other._positions.values) {
            updatePosition(position)
        }
    }

    /**
     * Put the positions of an [other] portfolio to this portfolio, overwriting existing position entries
     */
    fun put(other: Portfolio) {
        _positions.putAll(other._positions.map { it.key to it.value })
    }


    /**
     * Calculate the difference between current and a [target] portfolio. This can be useful when wanting to
     * re-balance a portfolio. The returned map provides the required changes for an asset. So the following
     * rule holds true:
     *
     *      Target Portfolio = Current Portfolio + Diff
     *
     * An example how to generate the market orders for the re-balancing of a portfolio:
     *
     *      val diff = portfolio.diff(targetPortfolio)
     *      val orders = diff.map { MarketOrder(it.key, it.value) }
     *
     */
    fun diff(target: Portfolio): Map<Asset, Double> {
        val result = mutableMapOf<Asset, Double>()

        for (position in target.positions) {
            // Use BigDecimal to avoid inaccuracies
            val targetSize = BigDecimal.valueOf(position.size)
            val sourceSize = BigDecimal.valueOf(getPosition(position.asset).size)
            val value = (targetSize - sourceSize).toDouble()
            if (value != 0.0) result[position.asset] = value
        }

        for (position in positions) {
            if (position.asset !in result) result[position.asset] = -position.size
        }

        return result
    }

}
