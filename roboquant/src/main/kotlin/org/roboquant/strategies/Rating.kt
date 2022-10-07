/*
 * Copyright 2020-2022 Neural Layer
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
 * enumeration and is closely modelled after how traditional analyst rate assets, using a 5 point score:
 *
 * - [BUY]: Also known as strong buy and "on the recommended list". Buy is a recommendation to purchase a specific
 *   asset.
 * - [SELL]: Also known as strong sell, it's a recommendation to sell an asset or to liquidate an asset.
 * - [HOLD]: In general terms, an asset with a hold recommendation is expected to perform at the same pace as comparable
 *   assets or in-line with the market.
 * - [UNDERPERFORM]: A recommendation that means an asset is expected to do slightly worse than the overall market
 *   return. Underperform can also be expressed as "moderate sell," "weak hold" and "underweight."
 * - [OUTPERFORM]: Also known as "moderate buy," "accumulate" and "overweight." Outperform is a recommendation
 *   meaning an asset is expected to do slightly better than the market return.
 *
 *   @property value representation of a rating goes from 2 (BUY) to -2 (SELL)
 */
enum class Rating(val value: Int) {

    /**
     * Buy rating, is a recommendation to purchase a specific asset.
     */
    BUY(2),

    /**
     * Outperform rating, is a recommendation meaning an asset is expected to do slightly better than the market return.
     */
    OUTPERFORM(1),

    /**
     * Hold rating, a recommendation that means an asset is expected to perform at the same pace as comparable assets
     * or in-line with the market.
     */
    HOLD(0),

    /**
     * Underperform rating, a recommendation that means an asset is expected to do slightly worse than the overall
     * market return.
     */
    UNDERPERFORM(-1),

    /**
     * Sell rating. a recommendation to sell an asset or to liquidate an asset.
     */
    SELL(-2);

    /**
     * Is this a positive rating, so a BUY or an OUTPERFORM
     */
    val isPositive: Boolean get() = this === BUY || this === OUTPERFORM

    /**
     * Is this a negative rating, so a SELL or UNDERPERFORM
     */
    val isNegative: Boolean get() = this === SELL || this === UNDERPERFORM

    /**
     * Return the inverse of this rating
     */
    fun inverse(): Rating {
        return when (this) {
            BUY -> SELL
            OUTPERFORM -> UNDERPERFORM
            HOLD -> HOLD
            UNDERPERFORM -> OUTPERFORM
            SELL -> BUY
        }
    }

    /**
     * Does this rating conflict with an [other] rating. Ratings only conflict if the direction is different. So for
     * example, a [BUY] and [OUTPERFORM] don't conflict.
     */
    fun conflicts(other: Rating) = value.sign != other.value.sign
}