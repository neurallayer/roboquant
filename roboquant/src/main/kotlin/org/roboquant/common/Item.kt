/*
 * Copyright 2020-2026 Neural Layer
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

import kotlin.collections.sumOf
import kotlin.math.absoluteValue

/**
 * An item is the lowest level of information contained in an [Event] and can be anything from a market price for an
 * asset to an annual report or a Twitter tweet.
 * An item doesn't have to be linked to a particular asset, although [PriceItem]s are.
 *
 * The content of the item is determined by the class implementing this interface. Strategies are expected to filter
 * on those types of items they are interested in.
 *
 * # Example
 * ```
 * event.items.filterIsInstance<PriceBar>(). ...
 * ```
 */
interface Item

/**
 * PriceItem represents an [Item] that contains pricing information for a single [Asset].
 */
interface PriceItem : Item {

    /**
     * The underlying asset of the price item
     */
    val asset: Asset

    /**
     * Get the price for this PriceItem. If more than one price is available, optionally the
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
     * Retrieves the volume associated with this PriceItem.
     * The volume may represent trade volume or total order-book volume, depending on the
     * PriceItem implementation, and may vary based on the specified [type].
     * If the [type] is not recognized or not applicable, a default volume is returned.
     * If the volume is not supported, the result is [Double.NaN].
     *
     * @param type The type of volume to retrieve. Commonly used types are uppercase strings,
     *             such as "TRADE" or "ORDER_BOOK". Defaults to "DEFAULT".
     * @return The volume as a double, or [Double.NaN] if not supported.
     */
    fun getVolume(type: String = "DEFAULT"): Double

}

/**
 * Different types of PriceItems supported out of the box
 * - BAR: PriceBar
 * - QUOTE: PriceQuote
 * - TRADE: TradePrice
 * - BOOK: OrderBook
 */
enum class PriceItemType {
    BAR, QUOTE, TRADE, BOOK
}


/**
 * Provides open, high, low, and close prices and volume for a single asset. If the volume is not available, it
 * will return Double.NaN instead. Often this type of price item is also referred to as a candlestick.
 *
 * To optimize memory and reduce GC overhead, internally the values are stored in a DoubleArray.
 *
 * @property asset the asset of the price-bar
 * @property ohlcv contains the values of this price-bar as doubles
 * @property timeSpan the timeSpan of the price-bar, default is null
 */
class PriceBar(
    override val asset: Asset,
    val ohlcv: DoubleArray,
    val timeSpan: TimeSpan? = null
) : PriceItem {

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
     * Returns the volume, if available. Double.NaN is returned if the volume is not available.
     *
     * ## Example
     * ```
     * val volume = action.volume
     * ```
     *
     * @see [getVolume]
     */
    val volume
        get() = ohlcv[4]


    override fun getVolume(type: String): Double {
        return ohlcv[4].absoluteValue
    }

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
     * Get the price for this price bar.
     *
     * The supported types are: CLOSE, OPEN, LOW, HIGH, TYPICAL, with the default type being "CLOSE".
     *
     * Example:
     * ```
     * val price = action.getPrice("OPEN")
     * ```
     */
    override fun getPrice(type: String): Double {
        return when (type) {
            "DEFAULT" -> ohlcv[3]
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
data class TradePrice(override val asset: Asset, val price: Double, val volume: Double = Double.NaN) :
    PriceItem {

    /**
     * Returns the underlying [price].
     * Since this item only holds a single price, the [type] parameter is ignored.
     */
    override fun getPrice(type: String): Double {
        return price
    }

    /**
     * Return the volume
     */
    override fun getVolume(type: String): Double {
        return volume
    }

}

/**
 * Price Quote for an asset. Common use case is that this holds the National Best Bid and Offer and their sizes.
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
) : PriceItem {

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


    override fun getVolume(type: String): Double {
        return when(type) {
            "ASK" -> askSize
            "BID" -> bidSize
            else -> (askSize.absoluteValue + bidSize.absoluteValue) / 2
        }
    }


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
) : PriceItem {

    /**
     * Returns the total number of entries (asks + bids) in this order book
     */
    val entries
        get() = asks.size + bids.size

    /**
     * Instruction book will by default return the unweighted **MIDPOINT** price. Other [types][type] that are supported are:
     * - lowest **ASK** price
     * - highest **BID** price
     * - **WEIGHTED** midpoint price
     *
     * @param type
     * @return
     */
    override fun getPrice(type: String): Double {
        return when (type) {
            "ASK" -> bestOffer
            "BID" -> bestBid
            "WEIGHTED" -> {
                val askVolume = asks.volume()
                val bidVolume = bids.volume()
                (asks.min() * askVolume + bids.max() * bidVolume) / (askVolume + bidVolume)
            }

            else -> (bestBid + bestOffer) / 2.0
        }
    }

    /**
     * Return the best (maximum) bid-price available in the order book
     */
    val bestBid: Double
        get() = bids.max()

    /**
     * Return the best (minimum) offer-price (aka ask-price) available in the order book
     */
    val bestOffer: Double
        get() = asks.min()

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
    override fun getVolume(type: String): Double {
        return asks.volume() + bids.volume()
    }

    private fun List<OrderBookEntry>.volume() = this.sumOf { it.size.absoluteValue }
    private fun List<OrderBookEntry>.max() = this.maxOf { it.limit }
    private fun List<OrderBookEntry>.min() = this.minOf { it.limit }

    /**
     * Instruction book entry contains the [size] and [limit] price
     *
     * @property size
     * @property limit
     * @constructor Create new Instruction book entry
     */
    data class OrderBookEntry(val size: Double, val limit: Double)
}

/**
 * Corporate actions like dividends and splits.
 *
 * It is important to note that this is not an action as defined by roboquant, since a corporate action is not something
 * a signalConverter can create. Rather, it is an action originating from a market and could be included in a feed.
 *
 * @property asset
 * @property type
 * @property value
 * @constructor Create empty Corporate action
 */
class CorporateItem(val asset: Asset, val type: String, val value: Double) : Item

/**
 * Can contain news items from market news sources or social media like Twitter and Reddit
 * Useful when you want to integrate sentiment analysis as part of the trading strategy.
 *
 * @property items list of news items
 * @constructor Create new News items
 */
data class NewsItems(val items: List<NewsItem>) : Item {

    /**
     * News item contains a single news item (text) with optionally extra metadata like
     * the author or symbol.
     *
     * @property id unique identifier of the news item
     * @property assets list of assets related to this news item
     * @property content the actual content of the news item
     * @property headline optional headline of the news item
     * @property url optional url pointing to the original news item
     * @property meta optional map of additional metadata
     * @constructor Create new News item
     */
    data class NewsItem(
        val id: String,
        val assets: List<Asset>? = null,
        val content: String,
        val headline: String? = null,
        val url: String? = null,
        val meta: Map<String, Any>? = null
    )
}
