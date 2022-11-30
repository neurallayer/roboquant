package org.roboquant.brokers.sim

import org.roboquant.common.Size
import org.roboquant.feeds.PriceAction
import java.time.Instant

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