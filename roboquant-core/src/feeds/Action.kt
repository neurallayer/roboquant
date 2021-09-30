package org.roboquant.feeds

import org.roboquant.common.Asset

/**
 * An action is the lowest level of information contained in an Event and can be anything from a price action for an asset
 * to an annual report to a Twitter tweet. An action doesn't have to be linked to a particular asset, although many are.
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
abstract class PriceAction(val asset: Asset) : Action {


    /**
     * Get the price from this PriceEvent. If more than one price is available, optionally the
     * [type] of price can be passed as a parameter. For example "CLOSE" in case of a candlestick.
     *
     * Any implementation is expected to return a default price if the type is not recognised. This way strategies
     * can work on a wide variety of feeds. It is convention to use uppercase strings for different types.
     *
     * @param type
     * @return
     */
    abstract fun getPrice(type: String = "DEFAULT"): Double
}

/**
 * Provides [open], [high], [low], and [close] prices and volume for a single asset. If the volume is not available, it will
 * return NaN instead. Sometimes this is also referred to as a candlestick.
 *
 * Internally it stores the values in as floats for efficiency reasons and only transfers them to a double when requesting
 * the price.
 *
 */
class PriceBar(
    asset: Asset,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float = Float.NaN
) : PriceAction(asset) {

    constructor(
        asset: Asset,
        open: Number,
        high: Number,
        low: Number,
        close: Number,
        volume: Number = Float.NaN
    ) : this(asset, open.toFloat(), high.toFloat(), low.toFloat(), close.toFloat(), volume.toFloat())

    constructor(asset: Asset, values: List<Float>) : this(asset, values[0], values[1], values[2], values[3], values[4])

    companion object {

        /**
         * Create a new PriceBar and compensate all prices and volume for the [adjustedClose]. This result in all prices
         * being corrected with by a factor [adjustedClose]/[close] and the volume by a factor [close]/[adjustedClose]
         */
        fun fromAdjustedClose(
            asset: Asset,
            open: Number,
            high: Number,
            low: Number,
            close: Number,
            adjustedClose: Number,
            volume: Number = Float.NaN
        ): PriceBar {
            val adj = adjustedClose.toFloat() / close.toFloat()
            return PriceBar(
                asset,
                open.toFloat() * adj,
                high.toFloat() * adj,
                low.toFloat() * adj,
                close.toFloat() * adj,
                volume.toFloat() / adj
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
        val result = when (type) {
            "CLOSE" -> close
            "OPEN" -> open
            "LOW" -> low
            "HIGH" -> high
            "TYPICAL" -> (high + low + close) / 3.0
            else -> close
        }
        return result.toDouble()
    }

    /**
     * return the contained values (OHLCV) as a float array
     */
    fun values() = floatArrayOf(open, high, low, close, volume)

    override fun toString(): String {
        return "price-bar ${asset.symbol} $open $high $low $close $volume"
    }

}


/**
 * Holds a single price for an asset and optional the volume. Often this reflects an actual trade, but it can
 * also be used in other circumstances.
 *
 * @property asset
 * @property price
 * @property volume
 * @constructor Create empty Single price
 */
class TradePrice(asset: Asset, private val price: Double, val volume: Double = Double.NaN) : PriceAction(asset) {

    /**
     * Return the underlying price. Since this event only holds a single price, the aspect
     * parameter is not used.
     *
     * @param type
     * @return
     */
    override fun getPrice(type: String): Double {
        return price
    }

    fun toList() = listOf(price, volume)
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
class PriceQuote(
    asset: Asset,
    val askPrice: Double,
    val askSize: Double,
    val bidPrice: Double,
    val bidSize: Double
) : PriceAction(asset) {

    /**
     * Return the underlying price. The available types are:
     *
     * - WEIGHTED
     * - ASK
     * - BID
     * - MEAN (also the default)
     *
     * @param type
     * @return
     */
    override fun getPrice(type: String): Double {
        return when (type) {
            "WEIGHTED" -> (askPrice * askSize + bidPrice * bidSize) / (askSize + bidSize)
            "ASK" -> askPrice
            "BID" -> bidPrice
            else -> (askPrice + bidPrice) / 2
        }
    }

    fun toList() = listOf(askPrice, askSize, bidPrice, bidSize)
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
class OrderBook(
    asset: Asset,
    val asks: List<OrderBookEntry>,
    val bids: List<OrderBookEntry>
) : PriceAction(asset) {

    /**
     * Order book will by default return the unweighted MIDPOINT price. Other types that are supported are:
     * - lowest "ASK" price
     * - highest "BID" price
     * - "WEIGHTED" midpoint price
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

    private fun List<OrderBookEntry>.volume() = this.sumOf { it.quantity }
    private fun List<OrderBookEntry>.max() = this.maxOf { it.limit }
    private fun List<OrderBookEntry>.min() = this.minOf { it.limit }

    /**
     * Order book entry contains the limit price and quantity
     *
     * @property quantity
     * @property limit
     * @constructor Create empty Order book entry
     */
    class OrderBookEntry(val quantity: Double, val limit: Double)
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
     * the originating source.
     *
     * @property content
     * @property meta
     * @constructor Create empty News item
     */
    class NewsItem(val content: String, val meta: Map<String, Any>)
}

