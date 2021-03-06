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

package org.roboquant.feeds

import org.roboquant.common.Amount
import org.roboquant.common.Asset
import kotlin.math.absoluteValue

/**
 * An action is the lowest level of information contained in an Event and can be anything from a price action for an
 * asset to an annual report or a Twitter tweet. An action doesn't have to be linked to a particular asset, although
 * many are.
 *
 * The content of the action is determined by the class implementing this interface. Strategies are expected to filter
 * on those types of actions they are interested in.
 *
 * Actions are bundled in a [Event], that contain all the actions that happen at a certain moment in time.
 *
 * # Example
 *      event.actions.filterByType<PriceAction>(). ...
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
     * Returns the asset of the action
     */
    val asset: Asset

    /**
     * Get the price for this PriceAction. If more than one price is available, optionally the
     * [type] of price can be passed as a parameter. For example "CLOSE" in case of a candlestick.
     *
     * Any implementation is expected to return a default price if the type is not recognised. This way strategies
     * can work on a wide variety of feeds. It is convention to use uppercase strings for different types.
     *
     * @return
     */
    fun getPrice(type: String = "DEFAULT"): Double

    /**
     * Same as [getPrice] but returns an [Amount], so this incudes the currency. The default implementation should be
     * sufficient for most cases.
     */
    fun getPriceAmount(type: String = "DEFAULT") = Amount(asset.currency, getPrice(type))

    /**
     * Volume for the price action. If not implemented, it should return [Double.NaN]
     *
     * Volume in the context of a PriceAction can mean different things. For example is can be trade volume but also
     * order-book volume, depending on the type of PriceAction.
     */
    val volume: Double

    /**
     * Return the prices and volume as a list of doubles. This is used to serialize the price action
     */
    val values: List<Double>
}

/**
 * Provides open, high, low, and close prices and volume for a single asset. If the volume is not available, it
 * will return Double.NaN instead. Often this type of price action is also referred to as a candlestick.
 *
 * In order to optimize memory and reduce GC overhead, internally the values are stored in a DoubleArray.
 *
 */
class PriceBar(
    override val asset: Asset,
    val ohlcv: DoubleArray
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
        volume: Number = Double.NaN
    ) : this(
        asset,
        doubleArrayOf(open.toDouble(), high.toDouble(), low.toDouble(), close.toDouble(), volume.toDouble())
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


    override fun toString(): String = "asset=${asset.symbol} OHLCV=${ohlcv.toList()}"

    companion object {

        fun fromValues(asset: Asset, values: List<Double>) = PriceBar(asset, values.toDoubleArray())

        /**
         * Create a new PriceBar and compensate all prices and volume for the [adjustedClose]. This result in all prices
         * being corrected with by a factor [adjustedClose]/[close] and the volume by a factor [close]/[adjustedClose]
         */
        @Suppress("LongParameterList")
        fun fromAdjustedClose(
            asset: Asset,
            open: Number,
            high: Number,
            low: Number,
            close: Number,
            adjustedClose: Number,
            volume: Number = Double.NaN
        ): PriceBar {
            val adj = adjustedClose.toDouble() / close.toDouble()
            return PriceBar(
                asset,
                doubleArrayOf(
                    open.toDouble() * adj,
                    high.toDouble() * adj,
                    low.toDouble() * adj,
                    close.toDouble() * adj,
                    volume.toDouble() / adj
                )
            )
        }

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

    /**
     * return the contained values (OHLCV) as a list of Doubles
     */
    override val values
        get() = ohlcv.toList()

}

/**
 * Holds a single price for an asset and optional the volume. Often this reflects an actual trade, but it can
 * also be used in other scenario's.
 *
 * @property asset
 * @property price
 * @property volume
 * @constructor Create empty Single price
 */
data class TradePrice(override val asset: Asset, private val price: Double, override val volume: Double = Double.NaN) :
    PriceAction {

    override val values
        get() = listOf(price, volume)

    operator fun times(n: Number) = copy(price = price * n.toDouble())
    operator fun div(n: Number) = copy(price = price / n.toDouble())

    companion object {
        fun fromValues(asset: Asset, values: List<Double>) = TradePrice(
            asset,
            values[0],
            values[1],
        )
    }

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
 * Price Quote for an asset. Most common use case is that this holds the National Best Bid and Offer and their volumes.
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

    override val values
        get() = listOf(askPrice, askSize, bidPrice, bidSize)

    companion object {
        fun fromValues(asset: Asset, values: List<Double>) = PriceQuote(
            asset,
            values[0],
            values[1],
            values[2],
            values[3]
        )
    }

    /**
     * Return the underlying price. The available [types][type] are:
     *
     * - WEIGHTED
     * - ASK
     * - BID
     * - MEAN (also the default)
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

}

/**
 * Contains an order book for an asset at a certain moment in time. The entries in the order book, both
 * ask and bid, are modelled as a list of [OrderBookEntry]
 *
 * @property asset
 * @property asks
 * @property bids
 * @constructor Create empty Order book
 */
data class OrderBook(
    override val asset: Asset,
    val asks: List<OrderBookEntry>,
    val bids: List<OrderBookEntry>
) : PriceAction {

    companion object {

        fun fromValues(asset: Asset, values: List<Double>): OrderBook {
            val asks = mutableListOf<OrderBookEntry>()
            val bids = mutableListOf<OrderBookEntry>()
            val endAsks = 1 + 2 * values[0].toInt()
            for (i in 1 until endAsks step 2) {
                val entry = OrderBookEntry(values[i], values[i + 1])
                asks.add(entry)
            }

            for (i in endAsks until values.lastIndex step 2) {
                val entry = OrderBookEntry(values[i], values[i + 1])
                bids.add(entry)
            }
            return OrderBook(asset, asks, bids)
        }
    }

    /**
     * Returns the total amount of entries (asks + bids)
     */
    val entries
        get() = asks.size + bids.size

    override val values
        get() = listOf(asks.size.toDouble()) +
                asks.map { listOf(it.quantity, it.limit) }.flatten() +
                bids.map { listOf(it.quantity, it.limit) }.flatten()

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
     * Returns the total outstanding volume of order book (bid + ask volumes combined)
     */
    override val volume: Double
        get() = asks.volume() + bids.volume()

    private fun List<OrderBookEntry>.volume() = this.sumOf { it.quantity.absoluteValue }
    private fun List<OrderBookEntry>.max() = this.maxOf { it.limit }
    private fun List<OrderBookEntry>.min() = this.minOf { it.limit }

    // override fun toString(): String = "$asset bids:${bids.size} asks:${asks.size}"

    /**
     * Order book entry contains the [quantity] and [limit] price
     *
     * @property quantity
     * @property limit
     * @constructor Create new Order book entry
     */
    data class OrderBookEntry(val quantity: Double, val limit: Double)
}

/**
 * Corporate actions like dividends and splits.
 *
 * It is important to note that this is not an action as defined by roboquant, since a corporate action is not something
 * a policy can create. Rather it is an action originating from a market and could be included in a feed.
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
     * the author
     *
     * @property content
     * @property meta
     * @constructor Create new News item
     */
    class NewsItem(val content: String, val meta: Map<String, Any>)
}

