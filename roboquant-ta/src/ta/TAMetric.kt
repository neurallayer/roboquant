package org.roboquant.ta

import org.roboquant.RunPhase
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.AssetFilter
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.metrics.MetricResults
import org.roboquant.metrics.SimpleMetric
import org.roboquant.strategies.utils.PriceBarSeries

/**
 * Add a technical indicator as a metric, for example a moving avarage.
 *
 * @property name the name of the metric
 * @property history
 * @property assetFilter determines which assets to process, default is [AssetFilter.noFilter]
 * @property block
 * @constructor Create new metric
 */
class TAMetric(
    private val name: String,
    private val history: Int = 15,
    private val assetFilter: AssetFilter = AssetFilter.noFilter(),
    private var block: TA.(series: PriceBarSeries) -> Double
) : SimpleMetric() {

    private val buffers = mutableMapOf<Asset, PriceBarSeries>()
    private val ta = TA()
    // private val logger: Logger = Logging.getLogger(TALibMetric::class)

    override fun calc(account: Account, event: Event): MetricResults {
        val metrics = mutableMapOf<String, Number>()
        val actions = event.prices.values.filterIsInstance<PriceBar>().filter { assetFilter.filter(it.asset) }
        for (priceAction in actions) {
            val asset = priceAction.asset
            val buffer = buffers.getOrPut(asset) { PriceBarSeries(asset, history, usePercentage = false) }
            buffer.add(priceAction)
            if (buffer.isAvailable()) {
                val metric = block.invoke(ta, buffer)
                val name = "$name.${asset.symbol.lowercase()}"
                metrics[name] = metric
            }
        }
        return metrics
    }

    override fun start(runPhase: RunPhase) {
       reset()
    }

    override fun reset() {
        super.reset()
        buffers.clear()
    }

}