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

@file:Suppress("unused")

package org.roboquant.brokers.sim

import org.roboquant.common.Size
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.PriceQuote
import java.time.Instant

/**
 * Interface that any pricing engine needs to implement. Ideally implementations should be able to support any type
 * of [PriceItem], although they can specialize for certain types of price actions, like a PriceBar or OrderBook.
 */
fun interface PricingEngine {

    /**
     * Return a pricing (calculator) for the provided price [item] and [time].
     *
     * Although most often not used, advanced pricing calculators can be dependent on the [time].
     * For example, certain FOREX exchanges might be more volatile during certain timeframes and this can be
     * reflected in the [PricingEngine].
     */
    fun getPricing(item: PriceItem, time: Instant): Pricing

    /**
     * Clear the state of the pricing engine.
     * Most [PricingEngines][PricingEngine] are stateless.
     * But advanced engines might implement some type of ripple-effect pricing behavior where large consecutive orders
     * for the same asset might impact the price.
     * The default implementation is to do nothing.
     */
    fun clear() {
        // default is to do nothing
    }
}


data class OrderExecution(val size: Size, val price: Double) {

    companion object {
        fun zero(price:Double=0.0) = OrderExecution(Size.ZERO, price)
    }

}


interface PriceEngine {

    /**
     * Would the order be triggered
     * @param trigger Double
     * @param priceItem PriceItem
     * @return Boolean
     */
    fun isTriggered(size: Size, trigger: Double, priceItem: PriceItem): Boolean

    /**
     * Simulate an order execution
     * @param size Size
     * @param priceItem PriceItem
     * @param time Instant
     * @param limit optional a limit to take ito account
     * @return OrderExecution
     */
    fun execute(size: Size, priceItem: PriceItem, time: Instant, limit: Double? = null): OrderExecution

}

abstract class BasePriceEngine : PriceEngine {

    fun isTriggered(size: Size, price: Double, trigger: Double?): Boolean {
        if (trigger == null) return true
        if (size.isPositive && price >= trigger) return true
        if (size.isNegative && price <= trigger) return true
        return false
    }

    fun getFill(size: Size, price: Double, limit: Double?): Size {
        if (limit == null) return size
        if (size.isPositive && price <= limit) return size
        if (size.isNegative && price >= limit) return size
        return Size.ZERO
    }
}

class SimplePriceEngine(private val bidPrice: String = "DEFAULT", private val askPrice: String = "DEFAULT") : PriceEngine {

    companion object {

        fun useQuotes(): SimplePriceEngine {
            return SimplePriceEngine("ASK", "BID")
        }
    }

    override fun isTriggered(size: Size, trigger: Double, priceItem: PriceItem): Boolean {
        if (size.isPositive) return priceItem.getPrice(bidPrice) <= trigger
        return priceItem.getPrice(askPrice) >= trigger
    }

    override fun execute(
        size: Size,
        priceItem: PriceItem,
        time: Instant,
        limit: Double?,
    ): OrderExecution {
        if (size.isPositive) {
            val price = priceItem.getPrice(askPrice)
            if (limit == null || limit >=price) return OrderExecution(size, price)
        } else {
            val price = priceItem.getPrice(bidPrice)
            if (limit == null || limit <= price) return OrderExecution(size, price)
        }

        return OrderExecution.zero()
    }

}



