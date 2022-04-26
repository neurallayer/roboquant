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

package org.roboquant.metrics

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.roboquant.brokers.Account
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import java.time.Instant

private const val EPS = 0.0000000001

/**
 * Calculate the sharp ratio for the returns made. The ratio is the average return earned in excess of the risk-free
 * rate per unit of volatility or total risk. Volatility is a measure of the price fluctuations of the portfolio.
 *
 * The following metrics will be calculated:
 *
 * - mean
 * - standard deviation
 * - SharpRatio
 *
 * @TODO validate formula
 *
 * @property riskFreeRate The risk-free rate to use. For example 0.04 equals 4%
 * @property minSteps The minimal number of observations before calculating the sharp ratio, default is 60
 * @constructor
 *
 */
class SharpRatio(
    private val riskFreeRate: Double = 0.0,
    private val minSteps: Int = 60,
) : SimpleMetric() {

    private var stats = DescriptiveStatistics()
    private var lastValue: Double = Double.NaN
    private var lastTime: Instant = Instant.MIN


    override fun calc(account: Account, event: Event): MetricResults {
        val value = account.equity.convert(time = event.time).value

        if (lastTime == Instant.MIN) {
            lastValue = value
            lastTime = event.time
            return mapOf()
        } else {
            val returns = (value - lastValue) / lastValue
            val tf = Timeframe(lastTime, event.time)
            val annualReturns = tf.annualize(returns)
            stats.addValue(annualReturns)
            lastValue = value
            lastTime = event.time

            return if (stats.n >= minSteps) {

                // Calculate the three metrics
                val mean = stats.mean
                val std = stats.standardDeviation
                val sharpRatio = (mean - riskFreeRate) / (std + EPS)

                mapOf(
                    "portfolio.mean" to mean,
                    "portfolio.std" to std,
                    "portfolio.sharpratio" to sharpRatio
                )
            } else {
                mapOf()
            }
        }
    }

    override fun reset() {
        stats.clear()
        lastTime = Instant.MIN
        lastValue = Double.NaN
    }
}
