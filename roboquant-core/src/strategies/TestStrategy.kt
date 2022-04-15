/*
 * Copyright 2021 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.feeds.Event

/**
 * Test strategy that has deterministic behavior that can help during testing to detect where things go wrong.
 *
 * It will generate an alternating BUY/SELL signal every n-th step. So for example it can generate a BUY signal for
 * Apple at 10th trading day and a SELL signal at 20th trading day.
 *
 *
 * @constructor Create new Test strategy
 */
class TestStrategy(private val skip: Int = 0) : Strategy {

    private val nSignals = mutableMapOf<Asset, Pair<Int, Boolean>>()

    init {
        require(skip >= 0) { "skip should be >= 0, found $skip instead" }
    }

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()

        for (asset in event.prices.keys) {
            var (nSignal, buy) = nSignals.getOrPut(asset) { Pair(0, true) }
            if ((nSignal % (skip + 1)) == 0) {
                val rating = if (buy) Rating.BUY else Rating.SELL
                val signal = Signal(asset, rating)
                signals.add(signal)
                buy = !buy
            }
            nSignals[asset] = Pair(nSignal + 1, buy)
        }
        return signals
    }

    override fun reset() {
        nSignals.clear()
    }
}