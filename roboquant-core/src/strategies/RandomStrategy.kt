package org.roboquant.strategies

import org.roboquant.common.Config
import org.roboquant.feeds.Event
import kotlin.random.Random


/**
 * Strategy that randomly generates a BUY or SELL signal for the assets in the feed.
 * The configured probability is the likelihood to generate a signal. And after that,
 * there is a 50/50 change it will generate either a SELL or BUY signal.
 *
 *
 * @property probability the likelihood to generate a signal, a value between 0.0 and 1.0
 * @constructor Create new Random strategy
 */
class RandomStrategy(private val probability: Double = 0.05, seed: Long = Config.seed) : Strategy {

    private val random = Random(seed)

    init {
        require(probability in 0.0..1.0) { "probability should be a value between 0.0 and 1.0, found $probability instead" }
    }

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for (asset in event.prices.keys) {
            if (random.nextDouble() < probability) {
                val rating = if (random.nextDouble() < 0.5) Rating.SELL else Rating.BUY
                signals.add(Signal(asset, rating))
            }
        }
        return signals
    }

}