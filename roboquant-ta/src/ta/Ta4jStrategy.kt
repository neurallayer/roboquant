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
 * Technical analysis based [Strategy] that allows to use indicators and rules from the ta4j library to define a
 * custom strategy. Ta4j is a flexible framework written in Java that allows to combine indicators and rules and comes
 * with over 130 indicators.
 *
 * @property buyingRule function that returns a buying rule based on the provided BarSaries, default is always FALSE
 * @property sellingRule function that returns a selling rule based on the provided BarSaries, default is always FALSE
 * @property maxBarCount maximum number of pricebars to track, default is -1 meaning track all bars
 * @constructor Create a new ta4j strategy
 */
class Ta4jStrategy(
    private val maxBarCount:Int = -1,
) : Strategy {

    private var buyingRule: (BarSeries) -> Rule = { BooleanRule.FALSE }
    private var sellingRule: (BarSeries) -> Rule = { BooleanRule.FALSE }
    private val rules = mutableMapOf<Asset, Triple<Rule, Rule, BarSeries>>()

    private fun getRules(asset: Asset): Triple<Rule, Rule, BarSeries> {
        return rules.getOrPut(asset) {
            val series = BaseBarSeriesBuilder().withName(asset.symbol).build()
            if (maxBarCount >= 0) series.maximumBarCount = maxBarCount
            val rule1 = buyingRule(series)
            val rule2 = sellingRule(series)
            Triple(rule1, rule2, series)
        }
    }

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        val time = event.time.toUTC()
        for ((asset, price) in event.prices) {
            if (price is PriceBar) {
                val (buyingRule, sellingRule, series) = getRules(asset)
                series.addBar(time, price.open, price.high, price.low, price.close, price.volume)
                if (buyingRule.isSatisfied(series.endIndex)) result.add(Signal(asset, Rating.BUY))
                if (sellingRule.isSatisfied(series.endIndex)) result.add(Signal(asset, Rating.SELL))
            }
        }
        return result
    }

    /**
     * Define the buy [rule]
     */
    fun buy(rule: (series: BarSeries) -> Rule) {
        buyingRule = rule
    }

    /**
     * Define the sell [rule]
     */
    fun sell(rule: (series: BarSeries) -> Rule) {
        sellingRule = rule
    }

    /**
     * Reset this strategy and any series data captured so far.
     */
    override fun reset() {
        super.reset()
        rules.clear()
    }

}