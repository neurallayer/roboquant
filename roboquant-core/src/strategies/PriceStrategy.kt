package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Base class that can be extended by strategies that are only interested in
 * a single price for an asset and not other type of actions.
 *
 */
abstract class PriceStrategy(private val aspect: String = "DEFAULT", prefix: String = "strategy.") :
    RecordingStrategy(prefix) {

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for ((asset, priceAction) in event.prices) {
            val price = priceAction.getPrice(aspect)
            val signal = generate(asset, price, event.now)
            if (signal != null) signals.add(signal)
        }
        return signals
    }

    /**
     * Subclasses need to implement this method and return optional a signal.
     *
     * @param asset
     * @param price
     * @param now
     * @return
     */
    abstract fun generate(asset: Asset, price: Double, now: Instant): Signal?
}

