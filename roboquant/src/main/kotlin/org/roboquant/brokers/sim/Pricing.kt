package org.roboquant.brokers.sim

import org.roboquant.common.Size

/**
 * Pricing is used to determine the price to use when executing an order.
 * The [PricingEngine] is the factory that creates these pricing.
 */
interface Pricing {

    /**
     * Get the lowest price available for the provided [size]. Default is the [marketPrice]
     * Typically this is used to evaluate if a limit or stop has been triggered.
     */
    fun lowPrice(size: Size): Double = marketPrice(size)

    /**
     * Get the highest price available  for the provided [size]. Default is the [marketPrice]
     * Typically this is used to evaluate if a limit or stop has been triggered.
     */
    fun highPrice(size: Size): Double = marketPrice(size)

    /**
     * Get the market price for the provided [size] that should be used for a trade. There is no default implementation
     * this needs to be implemented by the class.
     */
    fun marketPrice(size: Size): Double

}