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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Metric that calculates the realized and unrealized Profit and Loss.
 *
 * - The unrealized PNL is calculated based on the open positions and their last known market prices.
 * - The total PNL is based on growth on equity
 * - The realized PNL is based on the difference between the above two
 * - The market PNL is based on the returns of individual assets, so expressed as a percentage
 *
 * Metric names used:
 * ```
 * - pnl.realized
 * - pnl.unrealized
 * - pnl.total (= realized + unrealized)
 * - pnl.mkt
 * ```
 *
 * @param priceType the ype of price to use for market data to determine market PNL, default is "DEFAULT"
 * @constructor Create a new instance of the PNLMetric
 *
 */
class PNLMetric(private val priceType: String = "DEFAULT") : Metric {

    private class AssetReturn(
        val start: Instant,
        val first: Double,
        var end: Instant = start,
        var last: Double = first
    ) {

        val duration
            get() = Timeframe(start, end, true).duration.toMillis()

        fun calcReturn() = last / first - 1.0

        fun update(time: Instant, value: Double) {
            end = time
            last = value
        }

    }


    private var equity = Double.NaN
    private val assetReturns = mutableMapOf<Asset, AssetReturn>()

    /**
     * Market return of all assets, each weight by how long they are present in the run
     */
    private fun marketReturn(): Double {
        var sum = 0.0
        var totalWeights = 0.0
        for (r in assetReturns.values) {
            val weight = r.duration
            sum += r.calcReturn() * weight
            totalWeights += weight
        }
        return if (totalWeights > 0.0) sum / totalWeights else 0.0
    }

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event): Map<String, Double> {
        if (equity.isNaN()) equity = account.equityAmount().value
        val pnl = account.equityAmount().value - equity

        val pnl2 = account.unrealizedPNL()
        val unrealizedPNL = pnl2.convert(account.baseCurrency, event.time).value

        event.prices.values.forEach {
            val r = assetReturns.getOrPut(it.asset) { AssetReturn(event.time, it.getPrice(priceType)) }
            r.update(event.time, it.getPrice())
        }

        return mapOf(
            "pnl.realized" to pnl - unrealizedPNL,
            "pnl.unrealized" to unrealizedPNL,
            "pnl.total" to pnl,
            "pnl.mkt" to marketReturn()
        )
    }

    override fun reset() {
        equity = Double.NaN
        assetReturns.clear()
    }

}
