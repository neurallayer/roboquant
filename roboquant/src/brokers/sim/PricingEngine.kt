package org.roboquant.brokers.sim

import org.roboquant.common.Size
import org.roboquant.feeds.PriceAction
import java.time.Instant

/**
 * Interface for any pricing engine to implement. Ideally implementations should be able to support any type of price
 * actions, although they can specialize for certain types of price actions, like a PriceBar.
 */
fun interface PricingEngine {

    /**
     * Return a pricing (calculator) for the provided price [action]. Although most often not used, advanced pricing
     * calculators can be dependent on the [time]. For example certain FOREX exchanges might be more volatile during
     * certain timeframes and this can be reflected in the Pricing.
     */
    fun getPricing(action: PriceAction, time: Instant): Pricing

    /**
     * Clear any state of the pricing engine. General speaking a PricingEngine is stateless, but advanced engines might
     * implement some type of ripple-effect pricing strategy. Default is to do nothing.
     */
    fun clear() {}
}

/**
 * Pricing calclulator is provided as an argument [TradeOrderHandler.execute] so it can determine the price to use
 * when executing an order.
 */
interface Pricing {

    /**
     * Get the lowest price for the provided [size]. Default is the [marketPrice]
     * Typivally this is used to evaluate if a limit or stop has been triggered.
     */
    fun lowPrice(size: Size): Double = marketPrice(size)

    /**
     * Get the highest price for the provided [size]. Default is the [marketPrice]
     * Typivally this is used to evaluate if a limit or stop has been triggered.
     */
    fun highPrice(size: Size): Double = marketPrice(size)

    /**
     * Get the market price for the provided [size]. There is no default.
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

