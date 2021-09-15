package org.roboquant.strategies

import kotlin.math.sign

/**
 * Rating is the key component of a [Signal] that is a measure of the expected performance of an asset. It is an
 * enumeration  and is closely modelled on how traditional analyst ratings using a 5 point rating score:
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
 * @constructor Create a new rating
 */
enum class Rating(val value: Int) {
    BUY(2),
    OUTPEFORM(1),
    HOLD(0),
    UNDERPERFORM(-1),
    SELL(-2);

    /**
     * Is this a positive rating, so a BUY or an OUTPERFORM
     *
     */
    fun isPositive() = this === BUY || this === OUTPEFORM

    /**
     * Is this a negative rating, so a SELL or UNDERPERFORM
     *
     */
    fun isNegative() = this === SELL || this === UNDERPERFORM

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
     * Does this rating conflict with another rating
     *
     * @param other
     */
    fun conflicts(other: Rating) = value.sign != other.value.sign
}