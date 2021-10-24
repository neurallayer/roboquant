/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.strategies

import kotlin.math.sign

/**
 * Rating is the key component of a [Signal] that is a measure of the expected performance of an asset. It is an
 * enumeration and is closely modelled after how traditional analyst rate, using a 5 point score:
 *
 * - Buy: Also known as strong buy and "on the recommended list". Buy is a recommendation to purchase a specific
 *   security.
 * - Sell: Also known as strong sell, it's a recommendation to sell a security or to liquidate an asset.
 * - Hold: In general terms, a company with a hold recommendation is expected to perform at the same pace as comparable
 *   companies or in-line with the market.
 * - Underperform: A recommendation that means a stock is expected to do slightly worse than the overall stock market
 *   return. Underperform can also be expressed as "moderate sell," "weak hold" and "underweight."
 * - Outperform: Also known as "moderate buy," "accumulate" and "overweight." Outperform is an analyst recommendation
 *   meaning a stock is expected to do slightly better than the market return.
 *
 *   The [value] of a rating goes from 2 (BUY) to -2 (SELL)
 *
 */
enum class Rating(val value: Int) {
    BUY(2),
    OUTPEFORM(1),
    HOLD(0),
    UNDERPERFORM(-1),
    SELL(-2);

    /**
     * Is this a positive rating, so a BUY or an OUTPERFORM
     */
    val isPositive : Boolean get() = this === BUY || this === OUTPEFORM



    /**
     * Is this a negative rating, so a SELL or UNDERPERFORM
     */
    val isNegative: Boolean get() = this === SELL || this === UNDERPERFORM

    /**
     * The inverse of this rating
     *
     * @return
     */
    fun inverse(): Rating {
        return when (this) {
            BUY -> SELL
            OUTPEFORM -> UNDERPERFORM
            HOLD -> HOLD
            UNDERPERFORM -> OUTPEFORM
            SELL -> BUY
        }
    }

    /**
     * Does this rating conflict with an [other] rating
     *
     * @param other
     */
    fun conflicts(other: Rating) = value.sign != other.value.sign
}