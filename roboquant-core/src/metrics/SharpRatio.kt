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
import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant
import kotlin.math.pow
import kotlin.math.sqrt

private const val eps = 0.0000000001

/**
 * Calculate the sharp ratio for the portfolio. The ratio is the average return earned in excess of the risk-free rate
 * per unit of volatility or total risk. Volatility is a measure of the price fluctuations of the portfolio.
 *
 * @TODO validate formula for time compensation
 *
 * @property riskFreeRate The risk-free rate to use. For example 0.04 equals 4%
 * @property minSteps The minimal number of steps before calculating the sharp ratio
 * @constructor
 *
 */
class SharpRatio(
    private val riskFreeRate: Double = 0.0,
    private val minSteps: Int = 60,
) : SimpleMetric() {

    private var stats = DescriptiveStatistics()
    private var start: Instant = Instant.MIN
    private var lastValue: Double = 0.0


    override fun calc(account: Account, event: Event): MetricResults {
        val values = account.getValue()
        val value = account.convertToCurrency(values, now = event.now)

        if (start == Instant.MIN) {
            start = event.now
            lastValue = value
            return mapOf()
        } else {
            val portfolioReturn = (value - lastValue) / lastValue
            stats.addValue(portfolioReturn)
            lastValue = value

            return if (stats.n >= minSteps) {
                val avgSeconds = (event.now.epochSecond - start.epochSecond) / stats.n
                val avgYears = avgSeconds / (365.0 * 24 * 3600)
                val adjRiskFreeRate = (1.0 + riskFreeRate).pow(avgYears) - 1.0
                val mean = stats.mean
                val std = stats.standardDeviation + eps
                val sharpRatio = (mean - adjRiskFreeRate) / std * sqrt(1.0 / avgYears)
                mapOf(
                    "portfolio.mean" to mean,
                    "portfolio.std" to std,
                    "portfolio.sharpRatio" to sharpRatio
                )
            } else {
                mapOf()
            }
        }
    }

    override fun start(phase: Phase) {
        stats.clear()
        start = Instant.MIN
        lastValue = 0.0
    }
}