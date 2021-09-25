package org.roboquant.alphavantage

import com.crazzyghost.alphavantage.AlphaVantage
import com.crazzyghost.alphavantage.Config
import org.roboquant.common.Config as RConfig
import com.crazzyghost.alphavantage.parameters.Interval as AlphaInterval
import com.crazzyghost.alphavantage.parameters.OutputSize
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

typealias Interval = AlphaInterval

/**
 * Alpha vantage feed, current just a PoC to validate we can retrieve data.
 *
 * TODO add support for different types of data and split between live feed and historic feeds.
 *
 * @property compensateTimeZone compensate for timezone differences
 * @property generateSinglePrice generate a single price event (using the close price) or a price bar (OHLCV) event
 * @constructor
 *
 * @param apiKey
 */
class AlphaVantageFeed(
    apiKey: String? = null,
    val compensateTimeZone: Boolean = true,
    private val generateSinglePrice: Boolean = true
) : HistoricFeed {

    private val subscriptions = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger("AlphaVantageFeed")
    private val events = mutableListOf<Event>()

    override val assets
        get() = subscriptions.values.toSet()

    override val timeline: List<Instant>
        get() = events.map { it.now }

    init {
        val key = apiKey ?: RConfig.getProperty("ALPHA_VANTAGE_API_KEY")
        require(key != null) { "No api key provided or set as environment variable ALPHA_VANTAGE_API_KEY" }

        val cfg = Config.builder()
            .key(key)
            .timeOut(10)
            .build()
        AlphaVantage.api().init(cfg)
        logger.info("Connected Alpha Vantage")
    }


    /**
     * (Re)play the events of this feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        events.forEach {
            channel.send(it)
        }
    }

    /**
     * Retrieve historic intraday price data for the provided asset
     *
     * @param asset
     */
    fun subscribe(asset: Asset, interval: Interval = Interval.FIVE_MIN) {
        val symbol = asset.symbol
        subscriptions[symbol] = asset
        val result = AlphaVantage.api()
            .timeSeries()
            .intraday()
            .forSymbol(symbol)
            .interval(interval)
            .outputSize(OutputSize.FULL)
            .fetchSync()

        if (result.errorMessage != null)
            logger.warning(result.errorMessage)
        else
            handleSuccess(result)
    }


    private fun getParser(timezone: String): DateTimeFormatter {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val zoneId = if (compensateTimeZone) ZoneId.of(timezone) else ZoneId.of("UTC")
        return DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    }

    private fun handleSuccess(response: TimeSeriesResponse) {
        try {
            val symbol = response.metaData.symbol
            logger.info { "Received time series response for $symbol" }
            val asset = subscriptions[symbol]!!
            val tz = response.metaData.timeZone
            val dtf = getParser(tz)
            response.stockUnits.forEach {

                val action = if (generateSinglePrice) TradePrice(asset, it.close) else
                    PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)

                val now = ZonedDateTime.parse(it.date, dtf).toInstant()
                val event = Event(listOf(action), now)
                this.events.add(event)
            }
            events.sort()
            logger.info { "Received ${events.size} prices for $symbol from ${events.firstOrNull()?.now} to ${events.lastOrNull()?.now}" }
        } catch (e: Exception) {
            logger.severe { e.toString() }
        }
    }


}