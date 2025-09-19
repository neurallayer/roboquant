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

package org.roboquant.journals.metrics

import org.hipparchus.stat.descriptive.DescriptiveStatistics
import org.roboquant.common.Account
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.common.plus
import org.roboquant.common.years
import org.roboquant.common.Event
import org.roboquant.common.Order
import org.roboquant.common.Signal
import java.time.Instant

private const val EPS = 0.0000000001

/**
 * Calculate several metrics for the returns.
 *
 * Please note that this metric calculates the returns based on the total equity of the account.
 *
 * The following metrics will be calculated:
 * ```
 * - returns.mean
 * - returns.std
 * - returns.sharperatio
 * - returns.min
 * - returns.max
 * - returns.last
 *```
 *
 * @property riskFreeRate The annualized risk-free rate used in the Sharpe Ratio, for example, 4% would be 0.04.
 * Default is 0.0
 * @property minPeriods The minimal number of periods before calculating the metrics, default is 2
 * @property period The duration of a single period, default is 1 year.
 * @property annualize Should the returns be annualized, default is true
 * @constructor create a new instance of the ReturnsMetric
 */
class ReturnsMetric(
    private val riskFreeRate: Double = 0.0,
    private val minPeriods: Int = 2,
    private val period: TimeSpan = 1.years,
    private val annualize: Boolean = true
) : Metric {

    private var stats = DescriptiveStatistics()
    private var lastValue: Double = Double.NaN
    private var lastTime: Instant = Instant.MIN
    private var nextTime: Instant = Instant.MIN

    /**
     * @see Metric.calculate
     */
    override fun calculate(event: Event, account: Account, signals: List<Signal>, orders: List<Order>): Map<String, Double> {
        val time = event.time

        // Initialize first time
        @Suppress("ReplaceCallWithBinaryOperator")
        if (nextTime.equals(Instant.MIN)) {
            nextTime = time + period
            lastTime = time
            lastValue = account.equity().convert(account.baseCurrency, event.time).value
        }

        if (time >= nextTime) {

            val value = account.equity().convert(account.baseCurrency, event.time).value
            val periodReturn = (value - lastValue) / lastValue
            val tf = Timeframe(lastTime, time)
            val returns = if (annualize) tf.annualize(periodReturn) else periodReturn
            stats.addValue(returns)
            nextTime = time + period
            lastTime = time
            lastValue = value

            if (stats.n >= minPeriods) {

                // Calculate the three metrics
                val mean = stats.mean
                val std = stats.standardDeviation
                val sharpeRatio = (mean - riskFreeRate) / (std + EPS)

                return mapOf(
                    "returns.mean" to mean,
                    "returns.std" to std,
                    "returns.sharperatio" to sharpeRatio,
                    "returns.min" to stats.min,
                    "returns.max" to stats.max,
                    "returns.last" to returns
                )
            }
        }
        return emptyMap()
    }

    override fun reset() {
        stats.clear()
        lastTime = Instant.MIN
        nextTime = Instant.MIN
        lastValue = Double.NaN
    }
}
