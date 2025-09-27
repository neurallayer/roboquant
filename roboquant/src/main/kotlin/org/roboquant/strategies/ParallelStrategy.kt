/*
 * Copyright 2020-2025 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.strategies

import kotlinx.coroutines.*
import org.roboquant.common.Event
import org.roboquant.common.Signal

/**
 * This Strategy that runs a number of other strategies in parallel and combines the result. The strategies will each run
 * in their own coroutine, and the results are aggregated after they are finished. The order of aggregation is the same
 * as the order of strategies that is provided in the constructor.
 *
 * This will typically improve performance for strategies that are CPU or IO intensive and take some time to complete.
 * For fast strategies, this might not speed up the overall performance. Only the [createSignals] method is run in
 * parallel, other method invocations like reset and getMetrics are run sequential.
 *
 * There is no logic included to resolve conflicting signals, for example, one strategy generates a BUY signal,
 * and another strategy generates a SELL signal for the same asset. This is left to the signalConverter to resolve.
 *
 * @property strategies The strategies to process in parallel
 * @constructor Create a new parallel strategy
 */
class ParallelStrategy(val strategies: Collection<Strategy>, private val signalResolver: SignalResolver? = null) :
    Strategy {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    /**
     * Create a new parallel strategy using the provided [strategies]
     */
    constructor(vararg strategies: Strategy, signalResolver: SignalResolver? = null) : this(
        strategies.toList(),
        signalResolver
    )

    /**
     * @see Strategy.createSignals
     */
    override fun createSignals(event: Event): List<Signal> {
        val signals: List<Signal> = runBlocking {
            strategies.map { strategy ->
                scope.async {
                    strategy.createSignals(event)
                }
            }.awaitAll().flatten()
        }
        return signalResolver?.let { signals.it() }
            ?: signals
    }

}
