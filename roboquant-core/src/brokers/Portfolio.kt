package org.roboquant.brokers

import org.roboquant.common.Asset
import org.roboquant.common.Cash
import org.roboquant.common.Summary
import java.text.DecimalFormat

/**
 * Portfolio holds the [Assets][Asset] and their [Position] within an account. An asset can be anything from a stock
 * to an option contract.
 *
 *
 * @constructor Create a new empty Portfolio
 */
class Portfolio : Cloneable {
    
    private val _positions = mutableMapOf<Asset, Position>()

    /**
     * Get the open positions in this portfolio, both short and long
     * positions will be returned.
     */
    val positions
        get() = _positions.filter { it.value.open }

    /**
     * Long positions contained in this portfolio
     */
    val longPositions
        get() = _positions.filter { it.value.long }


    val assets
        get() = positions.keys.toList()

    /**
     * Short positions contained in this portfolio
     */
    val shortPositions
        get() = _positions.filter { it.value.short }

    /**
     * Is this portfolio empty? Or in other words does it not contain positions with a quantity unequal to zero.
     *
     * @return true is empty, false otherwise
     */
    fun isEmpty(): Boolean = positions.isEmpty()


    /**
     * Create a clone of the portfolio and return it. A clone is guaranteed not to be mutated
     * by an ongoing run and so reflects the portfolio at a moment in time.
     *
     * @return the cloned instance
     */
    public override fun clone(): Portfolio {
        val portfolio = Portfolio()
        portfolio._positions.putAll(_positions.map { it.key to it.value.copy() })
        return portfolio
    }

    /**
     * Update a position in the portfolio and return the realized PnL
     *
     * @param position
     */
    fun updatePosition(position: Position): Double {
        val currentPos = _positions[position.asset]
        return if (currentPos == null) {
            _positions[position.asset] = position.copy()
            0.0
        } else {
            currentPos.update(position)
        }
    }

    /**
     * Replace a position in the portfolio with a new position. So this overwrites the previous position
     * This will be used for example with live trading when the broker holds the true state of the positions
     * in a portfolio.
     *
     * @param position
     */
    fun setPosition(position: Position) {
        _positions[position.asset] = position.copy()
    }


    /**
     * Get total value of this portfolio. The total value is calculated based on the
     * sum of the open positions and their last known price.
     *
     * @return the cash value
     */
    fun getValue(): Cash {
        val result = Cash()
        for ((asset, position) in positions) {
            result.deposit(asset.currency, position.value)
        }
        return result
    }

    /**
     * Get total unrealized PNL of this portfolio. The unrealized PNL is calculated based on the open positions and
     * their average cost and last known price.
     *
     * @return the total unrealized PNL as a cash value
     */
    fun unrealizedPNL(): Cash {
        val result = Cash()
        for ((asset, position) in positions) {
            result.deposit(asset.currency, position.pnl)
        }
        return result
    }


    /**
     * Get the position for an asset. If the portfolio doesn't hold the asset, it returns
     * an empty position.
     *
     * @param asset
     * @return
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
     *
     */
    fun summary(): Summary {
        val s = Summary("Portfolio")

        val pf = DecimalFormat("#######")
        val ps = positions
        if (ps.isEmpty()) {
            s.add("EMPTY")
        } else {
            val fmt = "%14s │%6s │%11s │%11s │%11s │%11s │"
            val header = String.format(fmt, "asset", "curr", "qty", "entry", "spot", "p&l")
            s.add(header)

            for ((p, v) in ps) {
                val c = v.asset.currency
                val pos = pf.format(v.quantity)
                val cost = c.format(v.cost) //+ " " + p.currencyCode
                val price = c.format(v.price)
                val pnl = c.format(v.pnl)
                val asset = "${p.type}:${p.symbol}"
                val line = String.format(fmt, asset, p.currencyCode, pos, cost, price, pnl)
                s.add(line)
                //s.add("${p.type}:${p.symbol}", "$pos @ $cost ($price)")
            }
        }

        return s
    }

    /*
    fun asDataFrame(): AnyFrame {
        return positions.values.toDataFrame {
            "asset" { "${asset.type}:${asset.symbol}" }
            "pos" {quantity.toBigDecimal().setScale(1, RoundingMode.HALF_DOWN)}
            "cost" { asset.currency.toBigDecimal(cost)}
            "price" { asset.currency.toBigDecimal(price)}
            "pnl" { asset.currency.toBigDecimal(pnl)}
            "short" { short}
            "total cost" { asset.currency.toBigDecimal(totalCost)}
            "updated" { lastUpdate }
        }
    }
    */

    /**
     * Add the positions of another portfolio to this portfolio.
     *
     * @param portfolio The portfolio to merge
     */
    fun add(portfolio: Portfolio) {
        for (position in portfolio._positions.values) {
            updatePosition(position)
        }
    }

    /**
     * Put the positions of another portfolio into this portfolio, overwriting existing position entries
     *
     * @param portfolio The portfolio to use
     */
    fun put(portfolio: Portfolio) {
        _positions.putAll(portfolio._positions.map { it.key to it.value.copy() })
    }


    /**
     * Calculate the difference between current and a target portfolio. This can be useful when wanting to
     * re-balance a portfolio. The returned map provides for required changes in position per asset. So the following
     * holds true:
     *
     *      Target Portfolio = Current Portfolio + Diff
     *
     * An example how to generate the required orders for the re-balancing of a portfolio:
     *
     *      val diff = portfolio.diff(targetPortfolio)
     *      val orders = diff.map { MarketOrder(it.key, it.value) }
     *
     *
     * @param target The target portfolio to use for comparison
     */
    fun diff(target: Portfolio): Map<Asset, Double> {
        val result = mutableMapOf<Asset, Double>()

        target.positions.forEach { (asset, position) ->
            val value = position.quantity - getPosition(asset).quantity
            if (value != 0.0) result[asset] = value
        }

        positions.forEach { (asset, position) ->
            if (asset !in result) result[asset] = -position.quantity
        }

        return result
    }

}
