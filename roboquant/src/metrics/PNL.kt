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

import org.roboquant.brokers.Account
import org.roboquant.brokers.realizedPNL
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.feeds.Event

/**
 * Metric that calculates both the realized and unrealized Profit and Loss. The unrealized PNL is calculated based
 * on the assets in the portfolio and the last known market price. The realized PNL is based on actual trades made
 * and the profit they generated. All amounts are converted to the base currency of the account.
 *
 * This can be a metric can slow down back tests with very many trades, since at each step it iterates over all
 * available trades to calculate the realized PNL
 *
 * Metric names used:
 * - pnl.realized
 * - pnl.unrealized
 * - pnl.total (= realized + unrealized)
 *
 * @constructor Create new PNL metric
 */
class PNL : Metric {

    /**
     * Calculate any metrics given the event of information. This will be called at the
     * end of each step in a run. The result is returned using the base currency of
     * the account. It contains the following three metrics
     *
     * @param account
     * @return
     */
    override fun calculate(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Double>()

        val pnl = account.trades.realizedPNL
        val realizedPNL = pnl.convert(time = event.time)
        result["pnl.realized"] = realizedPNL.value

        val pnl2 = account.positions.unrealizedPNL
        val unrealizedPNL = pnl2.convert(time = event.time)
        result["pnl.unrealized"] = unrealizedPNL.value

        result["pnl.total"] = realizedPNL.value + unrealizedPNL.value
        return result
    }



}