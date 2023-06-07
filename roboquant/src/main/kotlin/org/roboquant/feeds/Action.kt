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

package org.roboquant.feeds

import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.TimeSpan
import org.roboquant.feeds.OrderBook.OrderBookEntry
import kotlin.math.absoluteValue

/**
 * An action is the lowest level of information contained in an [Event] and can be anything from a price action for an
 * asset to an annual report or a Twitter tweet. An action doesn't have to be linked to a particular asset, price
 * actions are.
 *
 * The content of the action is determined by the class implementing this interface. Strategies are expected to filter
 * on those types of actions they are interested in.
 *
 * # Example
 *      event.actions.filterByType<PriceBar>(). ...
 *
 */
interface Action

/**
 * PriceAction represents an [Action] that contains pricing information for a single asset.
 *
 * @constructor Create empty Price action
 */
interface PriceAction : Action {

    /**
     * The underlying asset of the price action
     */
    val asset: Asset

    /**
     * Get the price for this PriceAction. If more than one price is available, optionally the
     * [type] of price can be passed as a parameter. For example, "CLOSE" in case of a candlestick.
     *
     * Any implementation is expected to return a default price if the type is not recognized. This way strategies
     * can work on a wide variety of feeds. It is convention to use uppercase strings for different types.
     */
    fun getPrice(type: String = "DEFAULT"): Double

    /**
     * Same as [getPrice] but returns an [Amount], so this includes the currency of the asset.
     */
    fun getPriceAmount(type: String = "DEFAULT") = Amount(asset.currency, getPrice(type))

    /**
     * Volume for the price action. If not implemented, it should return [Double.NaN]
     *
     * Volume in the context of a PriceAction can mean different things. For example, is can be trade volume but also
     * the total order-book volume, depending on the type of PriceAction.
     */
    val volume: Double

}

/**
 * Provides open, high, low, and close prices and volume for a single asset. If the volume is not available, it
 * will return Double.NaN instead. Often this type of price action is also referred to as a candlestick.
 *
 * In order to optimize memory and reduce GC overhead, internally the values are stored in a DoubleArray.
 *
 * @property asset the asset of the price-bar
 * @property ohlcv contains the values of this price-bar as doubles
 * @property timeSpan the timeSpan of the price-bar, default is null
 */
class PriceBar(
    override val asset: Asset,
    val ohlcv: DoubleArray,
    val timeSpan: TimeSpan? = null
) : PriceAction {

    /**
     * Convenience constructor to allow the instantiation with any type of number.
     */
    constructor(
        asset: Asset,
        open: Number,
        high: Number,
        low: Number,
        close: Number,
        volume: Number = Double.NaN,
        timeSpan: TimeSpan? = null
    ) : this(
        asset,
        doubleArrayOf(open.toDouble(), high.toDouble(), low.toDouble(), close.toDouble(), volume.toDouble()),
        timeSpan
    )

    /**
     * Returns the opening price
     */
    val open
        get() = ohlcv[0]

    /**
     * Returns the highest price
     */
    val high
        get() = ohlcv[1]

    /**
     * Returns the lowest price
     */
    val low
        get() = ohlcv[2]

    /**
     * Returns the closing price
     */
    val close
        get() = ohlcv[3]

    /**
     * Returns the volume
     */
    override val volume
        get() = ohlcv[4]

    /**
     * String representation of this price-bar
     */
    override fun toString(): String =
        "symbol=${asset.symbol} ohlcv=${ohlcv.toList()} timeSpan=${timeSpan?.toString() ?: "unknown"}"


    /**
     * Adjust this PriceBar conform the provided adjusted close [price]
     */
    fun adjustClose(price: Number) {
        val adj = price.toDouble() / close
        ohlcv[0] *= adj
        ohlcv[1] *= adj
        ohlcv[2] *= adj
        ohlcv[3] *= adj
        ohlcv[4] /= adj
    }


    /**
     * Get the price for this price bar, default is the closing price. Supported types:
     *
     *  CLOSE, OPEN, LOW, HIGH, TYPICAL
     *
     * Example:
     *      val price = action.getPrice("OPEN")
     *
     */
    override fun getPrice(type: String): Double {
        return when (type) {
            "CLOSE" -> ohlcv[3]
            "OPEN" -> ohlcv[0]
            "LOW" -> ohlcv[2]
            "HIGH" -> ohlcv[1]
            "TYPICAL" -> (ohlcv[1] + ohlcv[2] + ohlcv[3]) / 3.0
            else -> ohlcv[3]
        }
    }


}

/**
 * Holds a single price for an asset and optional the volume. Often this reflects an actual trade, but it can
 * also be used in a different scenario.
 *
 * @property asset the asset of the trade
 * @property price the price of the trade
 * @property volume the volume of the trade, default is Double.NaN
 * @constructor Create a new instance of Trade Price
 */
data class TradePrice(override val asset: Asset, val price: Double, override val volume: Double = Double.NaN) :
    PriceAction {


    /**
     * Return the underlying price. Since this event only holds a single price, the [type] parameter is not used.
     *
     * @param type
     * @return
     */
    override fun getPrice(type: String): Double {
        return price
    }

}

/**
 * Price Quote for an asset. Common use case is that this holds the National Best Bid and Offer and their volumes.
 *
 * @property asset
 * @property askPrice
 * @property askSize
 * @property bidPrice
 * @property bidSize
 * @constructor Create empty Price quote
 */
data class PriceQuote(
    override val asset: Asset,
    val askPrice: Double,
    val askSize: Double,
    val bidPrice: Double,
    val bidSize: Double
) : PriceAction {


    /**
     * Return the underlying price. The available [types][type] are:
     *
     * - WEIGHTED
     * - ASK
     * - BID
     * - MEAN (also known as MIDPOINT and the default)
     *
     * @return
     */
    override fun getPrice(type: String): Double {
        val result = when (type) {
            "WEIGHTED" -> (askPrice * askSize + bidPrice * bidSize) / (askSize + bidSize)
            "ASK" -> askPrice
            "BID" -> bidPrice
            else -> (askPrice + bidPrice) / 2
        }
        return result
    }

    /**
     * Returns the volume. The volume is defined as total of [askSize] and [bidSize]
     */
    override val volume: Double
        get() = askSize + bidSize


    /**
     * Returns the spread percentage. The used formula is
     * ```
     * spread = (lowest_aks - highest_bid) / lowest_ask
     * ```
     */
    val spread: Double
        get() = (askPrice - bidPrice) / askPrice


}

/**
 * Contains an order book for an asset at a certain moment in time. The entries in the order book, both
 * ask and bid, are modeled as a list of [OrderBookEntry]
 *
 * @property asset
 * @property asks
 * @property bids
 * @constructor Create a new order book
 */
data class OrderBook(
    override val asset: Asset,
    val asks: List<OrderBookEntry>,
    val bids: List<OrderBookEntry>
) : PriceAction {

    /**
     * Returns the total number of entries (asks + bids) in this order book
     */
    val entries
        get() = asks.size + bids.size

    /**
     * Order book will by default return the unweighted **MIDPOINT** price. Other [types][type] that are supported are:
     * - lowest **ASK** price
     * - highest **BID** price
     * - **WEIGHTED** midpoint price
     *
     * @param type
     * @return
     */
    override fun getPrice(type: String): Double {
        return when (type) {
            "ASK" -> asks.min()
            "BID" -> bids.max()
            "WEIGHTED" -> {
                val askVolume = asks.volume()
                val bidVolume = bids.volume()
                (asks.min() * askVolume + bids.max() * bidVolume) / (askVolume + bidVolume)
            }

            else -> (asks.min() + bids.max()) / 2.0
        }
    }

    /**
     * Returns the spread percentage. The used formula is:
     * ```
     * spread = (lowest_aks - highest_bid) / lowest_ask
     * ```
     */
    val spread: Double
        get() {
            val ask = getPrice("ASK")
            val bid = getPrice("BID")
            return (ask - bid) / ask
        }


    /**
     * Returns the total outstanding volume of the order book (bid + ask volumes combined)
     */
    override val volume: Double
        get() = asks.volume() + bids.volume()

    private fun List<OrderBookEntry>.volume() = this.sumOf { it.size.absoluteValue }
    private fun List<OrderBookEntry>.max() = this.maxOf { it.limit }
    private fun List<OrderBookEntry>.min() = this.minOf { it.limit }

    // override fun toString(): String = "$asset bids:${bids.size} asks:${asks.size}"

    /**
     * Order book entry contains the [size] and [limit] price
     *
     * @property size
     * @property limit
     * @constructor Create new Order book entry
     */
    data class OrderBookEntry(val size: Double, val limit: Double)
}

/**
 * Corporate actions like dividends and splits.
 *
 * It is important to note that this is not an action as defined by roboquant, since a corporate action is not something
 * a policy can create. Rather, it is an action originating from a market and could be included in a feed.
 *
 * @property asset
 * @property type
 * @property value
 * @constructor Create empty Corporate action
 */
class CorporateAction(val asset: Asset, val type: String, val value: Double) : Action

/**
 * Can contain news items from market news sources or social media like Twitter and Reddit
 * Useful when you want to integrate sentiment analysis as part of the trading strategy.
 *
 * @property items list of news items
 * @constructor Create new News action
 */
class NewsAction(val items: List<NewsItem>) : Action {

    /**
     * News item contains a single news item (text) with optionally extra metadata like
     * the author or symbol.
     *
     * @property content
     * @property meta
     * @constructor Create new News item
     */
    class NewsItem(val content: String, val meta: Map<String, Any>)
}

