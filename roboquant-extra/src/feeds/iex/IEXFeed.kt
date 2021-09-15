package org.roboquant.feeds.iex

import org.roboquant.common.Asset
import org.roboquant.common.Exchange
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import pl.zankowski.iextrading4j.api.stocks.Chart
import pl.zankowski.iextrading4j.api.stocks.ChartRange
import pl.zankowski.iextrading4j.api.stocks.v1.Intraday
import pl.zankowski.iextrading4j.client.IEXCloudClient
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion
import pl.zankowski.iextrading4j.client.IEXTradingClient
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRequestBuilder
import pl.zankowski.iextrading4j.client.rest.request.stocks.v1.IntradayRequestBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Feed of historic price data using IEX Cloud as the source.
 *
 * @constructor
 *
 * @param publishableToken
 * @param secretToken
 */
class IEXFeed(
    publishableToken: String,
    secretToken: String? = null,
    sandbox: Boolean = true,
    private val exchange: Exchange = Exchange.DEFAULT
) : HistoricFeed {

    private val events = TreeMap<Instant, MutableList<PriceAction>>()
    private val logger = Logging.getLogger("IEXFeed")
    private val cloudClient: IEXCloudClient


    override val timeline: List<Instant>
        get() = events.keys.toList()

    override val assets
        get() = events.values.map { pricebars -> pricebars.map { it.asset }.distinct() }.flatten().distinct()


    init {
        var tokenBuilder = IEXCloudTokenBuilder().withPublishableToken(publishableToken)
        if (secretToken != null)
            tokenBuilder = tokenBuilder.withSecretToken(secretToken)

        val apiVersion = if (sandbox) IEXTradingApiVersion.IEX_CLOUD_V1_SANDBOX else IEXTradingApiVersion.IEX_CLOUD_V1
        cloudClient = IEXTradingClient.create(apiVersion, tokenBuilder.build())
    }

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
     * Retrieve historic intraday price bars for one or more assets
     *
     * @param assets
     */
    fun retrieveIntraday(vararg assets: Asset) {
        assets.forEach {
            val quote = cloudClient.executeRequest(
                IntradayRequestBuilder()
                    .withSymbol(it.symbol)
                    .build()
            )
            handleIntraday(it, quote)
        }
    }


    /**
     * Retrieve historic end of day price bars for one or more symbols
     *
     * @param symbols
     */
    fun retrievePriceBar(vararg symbols: String, range: String = "5y")  {
        // ChartRange.valueOf(range)
        val value = try {
            ChartRange.getValueFromCode(range)
        } catch (e:Exception) {
            throw Exception("Unsupported range value, possible values are: ${ChartRange.values().map { it.code }}")
        }
        val assets = symbols.map { Asset(it) }
        retrievePriceBar(*assets.toTypedArray(), range = value)
    }

    /**
     * Retrieve historic end of day price bars for one or more assets
     *
     * @param assets
     */
    fun retrievePriceBar(vararg assets: Asset, range: ChartRange = ChartRange.FIVE_YEARS) {

        assets.forEach {
            val chart = cloudClient.executeRequest(
                ChartRequestBuilder()
                    .withChartRange(range)
                    .withSymbol(it.symbol)
                    .build()
            )
            handleChart(it, chart)
        }
    }


    private fun getInstant(date: String, minute: String?): Instant {
        return if (minute !== null) {
            val dt = LocalDateTime.parse("${date}T$minute")
            exchange.getInstant(dt)
        } else {
            val d = LocalDate.parse(date)
            exchange.getClosingTime(d)
        }
    }

    private fun handleChart(asset: Asset, chart: List<Chart>) {
        chart.filter { it.open !== null }.forEach {
            val action = PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)
            val now = getInstant(it.date, it.minute)
            val list = events.getOrPut(now) { mutableListOf() }
            list.add(action)
        }
        logger.info { "Received data for $asset" }
        logger.info { "Total ${events.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }


    private fun handleIntraday(asset: Asset, quotes: List<Intraday>) {
        quotes.filter { it.open !== null }.forEach {
            val action = PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)
            val now = getInstant(it.date, it.minute)
            val list = events.getOrPut(now) { mutableListOf() }
            list.add(action)
        }
        logger.info { "Received data for $asset" }
        logger.info { "Total ${events.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }

}
