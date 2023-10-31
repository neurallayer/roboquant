/*
 * Copyright 2020-2023 Neural Layer
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

import org.roboquant.brokers.Account
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.feeds.Event

/**
 * Metric that calculates the realized and unrealized Profit and Loss.
 *
 * - The unrealized PNL is calculated based on the open positions and their last known market prices.
 * - The total PNL is based on growth on equity
 * - The realized PNL is based on the difference between the above two
 *
 * Metric names used:
 * ```
 * - pnl.realized
 * - pnl.unrealized
 * - pnl.total (= realized + unrealized)
 * ```
 * @constructor Create a new instance of the PNLMetric
 */
class PNLMetric : Metric {

    private var equity = Double.NaN

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event): Map<String, Double> {
        if (equity.isNaN()) equity = account.equityAmount.value
        val pnl = account.equityAmount.value - equity

        val pnl2 = account.positions.unrealizedPNL
        val unrealizedPNL = pnl2.convert(account.baseCurrency, event.time).value

        return mapOf(
            "pnl.realized" to pnl - unrealizedPNL,
            "pnl.unrealized" to unrealizedPNL,
            "pnl.total" to pnl
        )
    }

    override fun reset() {
        equity = Double.NaN
    }

}
