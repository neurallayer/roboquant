package org.roboquant.strategies

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Background
import org.roboquant.feeds.Event

/**
 * Strategy that runs a number of other strategies in parallel and combines the result. The strategies each run
 * in their own coroutine and the results are aggregated after they are finished. The order of aggregation is the same
 * as the order of strategies that is provided in the constructor.
 *
 * This will typically improve performance for strategies that are CPU or IO intensive and take some time to complete.
 * For very fast strategies this might not speed up the overall performance.
 *
 * There is no additional logic to remove conflicting signals, for example one strategy generates a BUY signal
 * and another strategy generates a SELL signal for the same asset.
 *
 * @property strategies The strategies to process in parallel
 * @constructor Create a new parallel strategy
 */
class ParallelStrategy(vararg strategies: Strategy) : CombinedStrategy(*strategies) {

    /**
     * @see Strategy.generate
     */
    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        runBlocking {
            val deferredList = mutableListOf<Deferred<List<Signal>>>()
            for (strategy in strategies) {
                val deferred = Background.async {
                    strategy.generate(event)
                }
                deferredList.add(deferred)
            }
            deferredList.forEach { signals.addAll(it.await()) }
        }
        return signals
    }


}