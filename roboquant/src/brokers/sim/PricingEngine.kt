/*
 * Copyright 2022 Neural Layer
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
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Interface that any pricing engine needs to implement. Ideally implementations should be able to support any type
 * of price actions, although they can specialize for certain types of price actions, like a PriceBar.
 */
fun interface PricingEngine {

    /**
     * Return a pricing (calculator) for the provided price [action] and [time]. Although most often not used, advanced
     * pricing calculators can be dependent on the [time]. For example certain FOREX exchanges might be more
     * volatile during certain timeframes and this can be reflected in the [PricingEngine].
     */
    fun getPricing(action: PriceAction, time: Instant): Pricing

    /**
     * Clear any state of the pricing engine. Often a [PricingEngine] is stateless, but advanced engines might
     * implement some type of ripple-effect pricing behavior. The default implementation is to do nothing.
     */
    fun clear() {}
}

/**
 * Pricing is provided as an argument [TradeOrderHandler.execute] so it can determine the price to use
 * when executing an order. The [PricingEngine] is the factory that creates these pricing.
 */
interface Pricing {

    /**
     * Get the lowest price for the provided [size]. Default is the [marketPrice]
     * Typically this is used to evaluate if a limit or stop has been triggered.
     */
    fun lowPrice(size: Size): Double = marketPrice(size)

    /**
     * Get the highest price for the provided [size]. Default is the [marketPrice]
     * Typically this is used to evaluate if a limit or stop has been triggered.
     */
    fun highPrice(size: Size): Double = marketPrice(size)

    /**
     * Get the market price for the provided [size]. There is no default implementation and a needs to be provided
     * by the implementation.
     */
    fun marketPrice(size: Size): Double

}

/**
 * Pricing model that uses a constant [spreadInBips] in BIPS to determine final trading price. It uses the same
 * price for high, low and market prices. It works with any type of PriceAction.
 */
class SpreadPricingEngine(private val spreadInBips: Int = 10, private val priceType: String = "DEFAULT") :
    PricingEngine {

    private class SpreadPricing(val price: Double, val slippagePercentage: Double) : Pricing {

        override fun marketPrice(size: Size): Double {
            val correction = if (size > 0) 1.0 + slippagePercentage else 1.0 - slippagePercentage
            return price * correction
        }
    }

    override fun getPricing(action: PriceAction, time: Instant): Pricing {
        val slippagePercentage = spreadInBips / 10_000.0
        return SpreadPricing(action.getPrice(priceType), slippagePercentage)
    }
}

/**
 * Pricing model that uses no additional cost. It calculates the same price for high, low and market prices.
 * It works with any type of PriceAction.
 */
class NoCostPricingEngine(private val priceType: String = "DEFAULT") : PricingEngine {

    private class NoCostPricing(val price: Double) : Pricing {

        override fun marketPrice(size: Size) = price
    }

    override fun getPricing(action: PriceAction, time: Instant): Pricing {
        return NoCostPricing(action.getPrice(priceType))
    }
}

