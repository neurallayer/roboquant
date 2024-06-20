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


data class OrderExecution(val triggered: Boolean, val size: Size, val price: Double) {

    companion object {
        fun zero() = OrderExecution(true, Size.ZERO, 0.0)

        fun notTriggered() = OrderExecution(false, Size.ZERO, 0.0)
    }

}


interface PriceEngine {

    fun execute(size: Size, priceItem: PriceItem, time: Instant, limit: Double? = null, trigger: Double? = null)
            : OrderExecution

}

abstract class BasePriceEngine : PriceEngine {
    fun triggered(size: Size, price: Double, trigger: Double?): Boolean {
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

class SimplePriceEngine(val priceType: String = "DEFAULT") : BasePriceEngine() {
    override fun execute(
        size: Size,
        priceItem: PriceItem,
        time: Instant,
        limit: Double?,
        trigger: Double?
    ): OrderExecution {
        val price = priceItem.getPrice(priceType)
        val triggered = triggered(size, price, trigger)
        return if (triggered) OrderExecution(true, getFill(size, price, limit), price) else OrderExecution.notTriggered()
    }

}


class QuotePriceEngine : BasePriceEngine() {
    override fun execute(
        size: Size,
        priceItem: PriceItem,
        time: Instant,
        limit: Double?,
        trigger: Double?
    ): OrderExecution {
        if (priceItem !is PriceQuote) return OrderExecution.notTriggered()
        val price = if (size.isPositive) priceItem.askPrice else priceItem.bidPrice
        val triggered = triggered(size, price, trigger)
        return if (triggered) OrderExecution(true, getFill(size, price, limit), price) else OrderExecution.notTriggered()
    }

}



@Suppress("unused")
class BarPriceEngine : BasePriceEngine() {

    private fun handleOpen(bar: PriceBar, size: Size, trigger: Double?, limit: Double?) : OrderExecution {
        val openPrice = bar.open
        val openTriggered = triggered(size, openPrice, trigger)
        if (openTriggered) {
            var fill = getFill(size, openPrice, limit)
            if (fill.nonzero) return OrderExecution(true, fill, openPrice)
            val bestPrice = if (size.isNegative) bar.high else bar.low
            fill = getFill(size, bestPrice, limit)
            return OrderExecution(true, fill, limit!!)
        }
        return OrderExecution.notTriggered()
    }

    private fun handleIntra(bar: PriceBar, size: Size, trigger: Double?, limit: Double?) : OrderExecution {
        val price = if (size.isNegative) bar.high else bar.low
        val triggered = triggered(size, price, trigger)
        if (triggered) {
            val fill = getFill(size, price, limit)
            return OrderExecution(true, fill, limit!!)
        }
        return OrderExecution.notTriggered()
    }


    override fun execute(
        size: Size,
        priceItem: PriceItem,
        time: Instant,
        limit: Double?,
        trigger: Double?
    ): OrderExecution {
        if (priceItem !is PriceBar) return OrderExecution.notTriggered()
        val result = handleOpen(priceItem, size, trigger, limit)
        if (result.triggered) return result

        return handleIntra(priceItem, size, trigger, limit)
    }

}
