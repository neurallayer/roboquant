package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Base class for strategies that are only interested in a single asset.
 *
 * @property asset The asset for which to apply this strategy
 * @constructor Create new single asset strategy
 */
abstract class SingleAssetStrategy(
    protected val asset: Asset,
    prefix: String = "strategy.${asset.symbol}."
) : RecordingStrategy(prefix = prefix) {

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        val priceAction = event.prices[asset]
        if (priceAction != null) {
            val signal = generate(priceAction, event.now)
            result.addNotNull(signal)
        }
        return result
    }

    /**
     * Subclasses need to be implemented this method. It will only be invoked if there is
     * a price action for the asset.
     *
     * @param priceAction
     * @param now
     * @return
     */
    abstract fun generate(priceAction: PriceAction, now: Instant): Signal?

}