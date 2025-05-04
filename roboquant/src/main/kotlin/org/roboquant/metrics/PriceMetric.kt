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
import org.roboquant.feeds.Event

/**
 * This metric logs the prices found in the event. Only a single value is logged, for example, the CLOSE price.
 *
 * @param priceType the type of price to log, default is "DEFAULT"
 */
class PriceMetric(private val priceType: String = "DEFAULT") : Metric {

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event) = buildMap {
        for (action in event.prices.values) {
            val name = "price.$priceType.${action.asset.symbol}".lowercase()
            put(name, action.getPrice(priceType))
        }
    }

}
