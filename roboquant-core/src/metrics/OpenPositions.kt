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
import org.roboquant.feeds.Event


/**
 * Captures metrics for all open positions within the portfolio, so you can see how these progresses over the
 * duration of the run. Per asset, it will record quantity, value, cost and unrealized P&L for that position.
 *
 */
class OpenPositions : SimpleMetric() {

    override fun calc(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Number>()
        val positions = account.portfolio.positions.values
        for (position in positions) {
            val asset = position.asset
            val name = "position.${asset.symbol}"
            result["$name.quantity"] = position.quantity
            result["$name.value"] = position.value
            result["$name.cost"] = position.totalCost
            result["$name.pnl"] = position.pnl
        }
        return result
    }
}