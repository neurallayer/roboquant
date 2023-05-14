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
import org.roboquant.brokers.realizedPNL
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.feeds.Event

/**
 * Metric that calculates the realized and unrealized Profit and Loss. The unrealized PNL is calculated based
 * on the open positions and their last known market prices. The realized PNL is based on actual trades made
 * and the profit they generated. All amounts are converted to the base currency of the account.
 *
 * This metric can slow down back-tests with many trades, since at each step in a run this metric iterates over
 * all available trades to calculate the realized PNL.
 *
 * Metric names used:
 * - pnl.realized
 * - pnl.unrealized
 * - pnl.total (= realized + unrealized)
 *
 * @constructor Create a new instance of the PNLMetric
 */
class PNLMetric : Metric {

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event): Map<String, Double>  {
        val pnl = account.trades.realizedPNL
        val realizedPNL = pnl.convert(account.baseCurrency, event.time).value

        val pnl2 = account.positions.unrealizedPNL
        val unrealizedPNL = pnl2.convert(account.baseCurrency, event.time).value

        return mapOf(
            "pnl.realized" to realizedPNL,
            "pnl.unrealized" to unrealizedPNL,
            "pnl.total" to realizedPNL + unrealizedPNL
        )
    }

}