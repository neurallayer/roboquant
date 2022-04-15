package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.toUTC
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Rule
import org.ta4j.core.rules.BooleanRule

/**
 * Strategy that allows to use indicators and rules from the TA4J library to define a custom strategy.
 *
 * TA4J is a flexible framework written in Java that allows both simple and complex rules. Compared to TALib it is
 * however a fair bit slower.
 *
 * @property buyingRule function that returns a buying rule based on the provided BarSaries
 * @property sellingRule function that returns a selling rule based on the provided BarSaries
 * @property maxBarCount maximum number of pricebars to track
 * @constructor Create new TA4J strategy
 */
class TA4JStrategy(
    private val maxBarCount:Int = -1,
) : Strategy {

    private var buyingRule: (BarSeries) -> Rule = { BooleanRule.FALSE }
    private var sellingRule: (BarSeries) -> Rule = { BooleanRule.FALSE }
    private val data = mutableMapOf<Asset, Triple<Rule, Rule, BarSeries>>()

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        val time = event.time.toUTC()
        for ((asset, price) in event.prices) {
            if (price is PriceBar) {
                val (buyingRule, sellingRule, series) = data.getOrPut(asset) {
                    val series = BaseBarSeriesBuilder().withName(asset.symbol).build()
                    if (maxBarCount >= 0) series.maximumBarCount = maxBarCount
                    val rule1 = buyingRule(series)
                    val rule2 = sellingRule(series)
                    Triple(rule1, rule2, series)
                }

                series.addBar(time, price.open, price.high, price.low, price.close, price.volume)
                if (buyingRule.isSatisfied(series.endIndex)) result.add(Signal(asset, Rating.BUY))
                if (sellingRule.isSatisfied(series.endIndex)) result.add(Signal(asset, Rating.SELL))
            }
        }
        return result
    }

    /**
     * Define the buy condition, return true if you want to generate a BUY signal, false otherwise
     *
     * # Example
     *
     *       strategy.buy { price ->
     *          ema(price.close, shortTerm) > ema(price.close, longTerm) && cdlMorningStar(price)
     *       }
     *
     */
    fun buy(block: (series: BarSeries) -> Rule) {
        buyingRule = block
    }

    /**
     * Define the sell conditions, return true if you want to generate a SELL signal, false otherwise
     *
     * # Example
     *
     *      strategy.sell { price ->
     *          cdl3BlackCrows(price) || cdl2Crows(price)
     *      }
     *
     */
    fun sell(block: (series: BarSeries) -> Rule) {
        sellingRule = block
    }

    override fun reset() {
        super.reset()
        data.clear()
    }

}