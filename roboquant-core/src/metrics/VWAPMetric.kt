package org.roboquant.metrics

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.utils.VWAPDaily

/**
 * Calculate the daily VWAP (Volume Weighted Average Price) for all the assets in a feed
 *
 * @property minSize minimum number of events before calculating the first VWAP
 * @constructor Create new VWAP metric
 */
class VWAPMetric(val minSize: Int = 2) : SimpleMetric() {

    private val calculators = mutableMapOf<Asset, VWAPDaily>()

    /**
     * Calculate the metric given the account and step. This method will be invoked at the
     * end of each step in a run. The result is [Map] with keys being a unique metric name and the value
     * the calculated metric value. If no metrics are calculated, an empty map should be returned instead.
     *
     * Please note VWAP is a single-day indicator, so it only makes sense in the context of intra-day events.
     *
     * @param account
     * @return
     */
    override fun calc(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Double>()
        event.prices.values.filterIsInstance<PriceBar>().forEach {
            val calc = calculators.getOrPut(it.asset) { VWAPDaily(minSize) }
            calc.add(it, event.now)
            if (calc.isReady()) {
                result["vwap.${it.asset.symbol}"] = calc.calc()
            }
        }
        return result
    }

    override fun start(phase: Phase) {
        calculators.clear()
    }
}