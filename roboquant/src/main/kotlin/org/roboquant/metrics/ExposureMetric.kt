/*
 * Copyright 2020-2024 Neural Layer
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
import org.roboquant.common.Wallet
import org.roboquant.feeds.Event
import kotlin.math.absoluteValue

/**
 * Calculate the net and gross exposure of the open positions relative to the total account value.
 *
 * - Net exposure is the difference between the long positions and the short positions and is expressed
 *   as a percentage of total account value
 *
 * - Gross exposure refers to the absolute level of exposure. It takes into account the absolute value
 * of both the long positions and short positions and is expressed as a percentage of total account value.
 *
 * @constructor Create new ExposureMetric
 */
class ExposureMetric : Metric {

    /**
     * Calculate any metrics given the event of information. This will be called at the
     * end of each step in a run.
     *
     * If no values are calculated, an empty map should be returned.
     *
     * @param account
     * @return
     */
    override fun calculate(account: Account, event: Event): Map<String, Double> {
        val longExposure = Wallet()
        val shortExposure = Wallet()
        val now = event.time

        for ((asset, position) in account.positions) {
            val v = asset.value(position.size, position.mktPrice)
            if (position.long)
                longExposure.deposit(v)
            else
                shortExposure.deposit(v)
        }

        val currency = account.baseCurrency
        val longExposureValue = longExposure.convert(currency, now).value
        val shortExposureValue = shortExposure.convert(currency, now).value
        val netExposureValue = longExposureValue + shortExposureValue
        val grossExposureValue = longExposureValue.absoluteValue + shortExposureValue.absoluteValue

        val total = account.cash.convert(currency, now).value + netExposureValue

        return mapOf(
            "exposure.net" to netExposureValue / total,
            "exposure.gross" to grossExposureValue / total,
            "exposure.long" to longExposureValue,
            "exposure.short" to shortExposureValue,
        )

    }
}
