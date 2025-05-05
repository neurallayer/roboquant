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

import org.roboquant.common.Account
import org.roboquant.common.Event

/**
 * Captures metrics for the open positions per asset, so you can see how these progresses over the
 * duration of the run. For each open position it will record the following attributes:
 *
 * - size; the position size, a negative size for a short position
 * - value; the total position market value, denoted in the currency of the asset
 * - cost; the total position cost, denoted in the currency of the asset
 * - pnl; the total unrealized profit & loss, denoted in the currency of the asset
 *
 * The naming will be: `position.<symbol>.<attribute-name>
 */
class PositionMetric : Metric {

    override fun calculate(account: Account, event: Event): Map<String, Double> {
        val result = mutableMapOf<String, Double>()

        for ((asset,position) in account.positions) {
            val name = "position.${asset.symbol}"
            result["$name.size"] = position.size.toDouble()
            result["$name.value"] = asset.value(position.size, position.mktPrice).value
            result["$name.cost"] = asset.value(position.size, position.avgPrice).value
            result["$name.pnl"] = asset.value(position.size, position.mktPrice - position.avgPrice).value
        }
        return result
    }
}
