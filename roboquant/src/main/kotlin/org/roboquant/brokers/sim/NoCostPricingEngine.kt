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
package org.roboquant.brokers.sim

import org.roboquant.common.Size
import org.roboquant.feeds.PriceItem
import java.time.Instant

/**
 * Pricing model that uses no additional cost. It calculates the same price for high, low and market prices.
 * It works with any type of PriceItem.
 */
class NoCostPricingEngine(private val priceType: String = "DEFAULT") : PricingEngine {

    private class NoCostPricing(val price: Double) : Pricing {
        override fun marketPrice(size: Size) = price
    }

    override fun getPricing(item: PriceItem, time: Instant): Pricing {
        return NoCostPricing(item.getPrice(priceType))
    }
}
