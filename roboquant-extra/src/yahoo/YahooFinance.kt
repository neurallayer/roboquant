package org.roboquant.yahoo

import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import org.roboquant.common.toUTC
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.PriceBar
import yahoofinance.YahooFinance
import yahoofinance.histquotes.HistoricalQuote
import yahoofinance.histquotes.Interval
import java.time.Instant
import java.util.*

/**
 * This feed uses historic data from Yahoo Finance API. Be aware this is not the most stable API and
 * might at any time stop working correctly.
 *
 * @property adjClose
 * @constructor Create new Yahoo finance feed
 */
class YahooFinanceFeed(private val adjClose: Boolean = true) : HistoricFeed {

    private val events = TreeMap<Instant, MutableList<PriceBar>>()
    private val logger = Logging.getLogger("YahooFinanceFeed")

    override val timeline: List<Instant>
        get() = events.keys.toList()

    override val assets = TreeSet<Asset>()

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        events.forEach {
            val event = Event(it.value, it.key)
            channel.send(event)
        }
    }

    /**
     * Retrieve historic [PriceBar] data from Yahoo Finance
     *
     * @param assets
     * @param timeFrame
     * @param interval
     */
    fun retrieve(vararg assets: Asset, timeFrame: TimeFrame, interval: Interval = Interval.DAILY) {
        val c1 = GregorianCalendar.from(timeFrame.start.toUTC())
        val c2 = GregorianCalendar.from(timeFrame.end.toUTC())
        assets.forEach {
            val quotes = YahooFinance.get(it.symbol)
            val history = quotes.getHistory(c1, c2, interval)
            handle(it, history)
        }
        this.assets.addAll(assets)
    }

    // TODO validate time offset
    private fun handle(asset: Asset, quotes: List<HistoricalQuote>) {

        quotes.forEach {
            val action = if (adjClose)
                PriceBar.fromAdjustedClose(asset, it.open, it.high, it.low, it.close, it.adjClose, it.volume)
            else
                PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)

            val now = it.date.toInstant()
            val list = events.getOrPut(now) { mutableListOf() }
            list.add(action)
        }

        logger.info { "Received data for $asset" }
        logger.info { "Total ${events.size} events from ${timeline.first()} to ${timeline.last()}" }
    }

}
