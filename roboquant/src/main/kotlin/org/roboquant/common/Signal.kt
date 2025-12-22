/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.common

import org.roboquant.common.SignalType.*

/**
 * This enum class represents the type of signal: [ENTRY], [EXIT] or [BOTH] and can be used by more advanced
 * strategies that provide different signals for entering and exiting a position.
 *
 * Please note it is up to the [org.roboquant.traders.Trader] to use this additional information and
 * simple implementations might ignore it.
 */
enum class SignalType {

    /**
     * Used to signal to open or increase a position size.
     */
    ENTRY,

    /**
     * Used to signal to close or decrease a position size.
     */
    EXIT,

    /**
     * Can be used for opening or closing of a position.
     */
    BOTH
}

/**
 * Default value for a BUY rating.
 */
const val BUY: Double = 1.0


/**
 * Default value for a SELL rating
 */
const val SELL: Double = -1.0

/**
 * Signal provides a rating for an [Asset] and is typically created by a strategy.
 *
 * It depends on the signalConverter how these signals are translated into actual orders, but possible scenarios are:
 *
 *  - A BUY rating results in going long for that asset. A SELL rating results in going short
 *  - The target price will become an additional take profit order
 *  - The probability determines the stake (volume) of the order. The more confident, the larger the stake
 *
 * @property asset The asset for which this rating applies
 * @property rating The rating for the asset, typically between -1.0 and 1.0
 * @property type The type of signal, entry, exit or both. [SignalType.BOTH] is the default.
 * @property tag Optional extra tag, for example, the strategy name that generated the signal, default is empty String
 * @constructor Create a new Signal
 */
class Signal(
    val asset: Asset,
    val rating: Double,
    val type: SignalType = BOTH,
    val tag: String = ""
) {

    /**
     * Companion object to create common signals like BUY and SELL
     */
    companion object {

        /**
         * Create a BUY signal.
         */
        fun buy(asset: Asset, type: SignalType = BOTH, tag: String = "") : Signal {
            return Signal(asset, BUY, type, tag)
        }

        /**
         * Creae a sell signal.
         */
        fun sell(asset: Asset, type: SignalType = BOTH, tag: String = "") : Signal {
            return Signal(asset, SELL, type, tag)
        }

    }

    /**
     * Is this signal (also) an exit signal, so to close or decrease a position?
     */
    val exit: Boolean
        get() = type === EXIT || type === BOTH

    /**
     * Returns true if this signal can function as an entry signal, false otherwise
     */
    val entry: Boolean
        get() = type === ENTRY || type === BOTH

    /**
     * Does this signal conflict with an [other] signal. Two signals conflict if they contain the same asset but
     * opposite ratings. So one signal has a positive outlook and the other one is negative.
     */
    fun conflicts(other: Signal): Boolean = asset == other.asset && direction != other.direction

    /**
     * Return the direction of the rating, -1 for negative ratings, 1 for positive ratings and 0 otherwise (HOLD rating)
     */
    val direction: Int
        get() = when {
            isBuy -> 1
            isSell -> -1
            else -> 0
        }

    /**
     * Is this a positive rating, so a BUY or an OUTPERFORM rating
     */
    val isBuy: Boolean
        get() = rating > 0.0

    /**
     * Is this a negative rating, so a SELL or UNDERPERFORM rating
     */
    val isSell: Boolean
        get() = rating < 0.0

}
