/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.optim

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Timeframe


/**
 * Functional interface for defining a scoring function
 */
fun interface Score {

    fun calculate(roboquant: Roboquant, timeframe: Timeframe) : Double

    /**
     * Predefined score functions
     */
    companion object {

        /**
         * The annualized growth as a [Score] function
         */
        fun annualizedEquityGrowth(roboquant: Roboquant, timeframe: Timeframe): Double {
            val broker = roboquant.broker
            require(broker is SimBroker) { "this score function only works with SimBroker"}
            val account = broker.account
            val startDeposit = broker.initialDeposit.convert(account.baseCurrency, timeframe.start).value
            val percentage = (account.equityAmount.value - startDeposit) / startDeposit
            return timeframe.annualize(percentage)
        }
    }

}

/**
 * Use the last value of recorded metric name to get the score.
 */
class MetricScore(private val metricName: String) : Score {

    override fun calculate(roboquant: Roboquant, timeframe: Timeframe): Double {
        val metrics = roboquant.logger.getMetric(metricName)
        return if (metrics.isNotEmpty()) metrics.values.last().values.last() else Double.NaN
    }

}
