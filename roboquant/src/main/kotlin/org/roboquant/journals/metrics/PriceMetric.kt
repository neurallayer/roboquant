/*
 * Copyright 2020-2026 Neural Layer
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

import org.roboquant.common.Account
import org.roboquant.common.Event
import org.roboquant.common.Order
import org.roboquant.common.Signal

/**
 * This metric logs the prices found in the event. Only a single value is logged, for example, the CLOSE price.
 *
 * @param priceType the type of price to log, default is "DEFAULT"
 */
class PriceMetric(private val priceType: String = "DEFAULT") : Metric {

    /**
     * @see Metric.calculate
     */
    override fun calculate(event: Event, account: Account, signals: List<Signal>, orders: List<Order>) = buildMap {
        for (item in event.prices.values) {
            val name = "price.$priceType.${item.asset.symbol}".lowercase()
            put(name, item.getPrice(priceType))
        }
    }

}
