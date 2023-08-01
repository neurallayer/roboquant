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

package org.roboquant.backtest

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.TimeSeries
import org.roboquant.common.Timeframe


/**
 * Functional interface for defining a scoring function that is used during (hyper-)parameter optimizations.
 */
fun interface Score {

    fun calculate(roboquant: Roboquant, run: String, timeframe: Timeframe): Double

    /**
     * Predefined score functions
     */
    companion object {

        /**
         * The annualized equity growth as a [Score] function.
         *
         * This function works best for longer timeframe runs (over 3 months). Otherwise, calculated values can have
         * large fluctuations.
         */
        fun annualizedEquityGrowth(roboquant: Roboquant, timeframe: Timeframe): Double {
            val broker = roboquant.broker as SimBroker
            val account = broker.account
            val startDeposit = broker.initialDeposit.convert(account.baseCurrency, timeframe.start).value
            val percentage = account.equityAmount.value/startDeposit - 1.0
            return timeframe.annualize(percentage)
        }
    }

}

/**
 * Use a recorded metric as the basis of the score.
 *
 * Use this with a metrics logger that supports the retrieval of metrics.
 *
 * @param metricName the metric name to use
 * @param reduce the function to use to go from a [TimeSeries] to a [Double], default being last entry,
 */
class MetricScore(private val metricName: String, private val reduce: (TimeSeries) -> Double = Companion::last) :
    Score {

    /**
     * Some typical reduce functions to derive a [Double] value out of a [TimeSeries]
     */
    companion object {

        fun last(ts: TimeSeries) = ts.values.last()
        fun mean(ts: TimeSeries) = ts.values.average()
        fun max(ts: TimeSeries) = ts.values.max()
        fun min(ts: TimeSeries) = ts.values.max()
        fun annualized(ts: TimeSeries): Double {
            if (ts.size < 2) return Double.NaN
            val perc = ts.values.last()/ts.values.first() - 1.0
            return ts.timeframe.annualize(perc)
        }

    }


    override fun calculate(roboquant: Roboquant, run: String, timeframe: Timeframe): Double {
        val metrics = roboquant.logger.getMetric(metricName, run)
        return if (metrics.isNotEmpty()) reduce(metrics) else Double.NaN
    }

}
