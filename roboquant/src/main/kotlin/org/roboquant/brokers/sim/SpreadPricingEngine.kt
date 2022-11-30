package org.roboquant.brokers.sim

import org.roboquant.common.Size
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Pricing model that uses a constant [spreadInBips] to calculate the trading price. It uses the same
 * price for high, low and market prices. It works with any type of PriceAction.
 */
class SpreadPricingEngine(private val spreadInBips: Int = 10, private val priceType: String = "DEFAULT") :
    PricingEngine {

    private class SpreadPricing(val price: Double, val spreadPercentage: Double) : Pricing {

        override fun marketPrice(size: Size): Double {
            // If BUY -> market price will be higher
            // if SELL -> market the price will be lower
            val correction = if (size > 0) 1.0 + spreadPercentage else 1.0 - spreadPercentage
            return price * correction
        }
    }

    override fun getPricing(action: PriceAction, time: Instant): Pricing {
        val spreadPercentage = spreadInBips / 10_000.0
        return SpreadPricing(action.getPrice(priceType), spreadPercentage)
    }
}