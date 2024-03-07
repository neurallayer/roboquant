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

import org.roboquant.feeds.PriceItem
import java.time.Instant

/**
 * Interface that any pricing engine needs to implement. Ideally implementations should be able to support any type
 * of [PriceItem], although they can specialize for certain types of price actions, like a PriceBar or OrderBook.
 */
fun interface PricingEngine {

    /**
     * Return a pricing (calculator) for the provided price [action] and [time].
     *
     * Although most often not used, advanced pricing calculators can be dependent on the [time].
     * For example, certain FOREX exchanges might be more volatile during certain timeframes and this can be
     * reflected in the [PricingEngine].
     */
    fun getPricing(action: PriceItem, time: Instant): Pricing

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

