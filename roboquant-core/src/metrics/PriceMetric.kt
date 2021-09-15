package org.roboquant.metrics

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Capture price actions for a particular asset. This can then be used to display it in a graph or perform
 * other post-run analysis. This metric also implements the Feed API.
 *
 * This metric is different from how most metrics work. It stores the result internally and does not
 * hand them over to a logger. However, just like other metrics it will reset its state at the beginning of a
 * phase.
 *
 * @property asset
 * @constructor Create new Asset price metric
 */
class PriceMetric(val asset: Asset) : Metric, Feed {

    private val _prices = mutableListOf<Pair<Instant, PriceAction>>()

    val prices
        get() = _prices.toList()

    override fun calculate(account: Account, event: Event) {
        val price = event.prices[asset]
        if (price != null) _prices.add(Pair(event.now, price))
    }

    override fun start(phase: Phase) {
        super.start(phase)
        _prices.clear()
    }

    override suspend fun play(channel: EventChannel) {
        for ((now, price) in _prices) {
            val event = Event(listOf(price), now)
            channel.send(event)
        }
    }
}