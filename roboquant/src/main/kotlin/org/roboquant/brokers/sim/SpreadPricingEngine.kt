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

package org.roboquant.brokers.sim

import org.roboquant.common.Size
import org.roboquant.common.bips
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Pricing model that uses a constant [spread] to calculate the trading price.
 *
 * If, for example, the spread is `10.bips` (default), the BUY price will increase by 0.05 percent, and the SELL price
 * decreases by 0.05 percent so that the spread between the two is 0.1% (aka 10 Bips).
 *
 * Depending on the market, spreads can vary a lot.
 * The USD/EUR typically has a very low spread (below 5 bips), while some penny stock might have a spread of well
 * above 10_000 bips.
 *
 * This engine uses the same price for high, low and market prices. It works with any type of [PriceAction].
 */
class SpreadPricingEngine(private val spread: Double = 10.bips, private val priceType: String = "DEFAULT") :
    PricingEngine {

    init {
        require(spread in 0.0..1.0)
    }

    private class SpreadPricing(private val price: Double, private val percentage: Double) : Pricing {

        override fun marketPrice(size: Size): Double {
            // If BUY -> market price is higher
            // if SELL -> market the price is lower
            val correction = 1.0 + (size.sign * percentage)
            return price * correction
        }
    }

    override fun getPricing(action: PriceAction, time: Instant): Pricing {
        val spreadPercentage = spread / 2.0
        return SpreadPricing(action.getPrice(priceType), spreadPercentage)
    }
}