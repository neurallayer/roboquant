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