/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.backtest

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed


/**
 * Run back tests without parameter optimizations.
 * The back tests in this class will run sequentially.
 *
 * These types of back tests are especially useful to get insights into the performance over different timeframes.
 *
 * @property feed The feed to use during the back tests
 * @property roboquant The roboquant instance to use during the back tests
 */
class Backtest(val feed: Feed, val roboquant: Roboquant) {


    init {
        require(roboquant.broker is SimBroker) { "Only a SimBroker can be used for back testing" }
    }

    /**
     * Perform a single run over the provided [timeframe].
     */
    fun singleRun(timeframe: Timeframe = feed.timeframe) {
        roboquant.run(feed, timeframe)
    }


    /**
     * Run a walk forward using the provided [period] and [overlap].
     * The [overlap] is optional and will always directly precede the provided [period].
     */
    fun walkForward(
        period: TimeSpan,
        overlap: TimeSpan = TimeSpan.ZERO,
        anchored: Boolean = false,
        timeframe: Timeframe = feed.timeframe
    ) {
        require(timeframe.isFinite()) { "needs a finite timeframe" }
        if (anchored) require(overlap.isZero) { "Cannot have overlap if anchored"}
        timeframe.split(period, overlap).forEach {
            val tf = if (anchored) it.copy(start = timeframe.start) else it
            roboquant.run(feed, tf, "wf-$tf")
        }
    }

    /**
     * Run a Monte Carlo simulation of a number of [samples] to find out how metrics of interest are distributed
     * given the provided [period].
     */
    fun monteCarlo(
        period: TimeSpan,
        samples: Int,
        timeframe: Timeframe = feed.timeframe
    ) {
        require(timeframe.isFinite()) { "needs a finite timeframe" }
        timeframe.sample(period, samples).forEach {
            roboquant.run(feed, it, "mc-$it")
        }
    }


}
