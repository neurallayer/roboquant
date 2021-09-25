package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.marketdata.historical.bar.enums.BarAdjustment
import net.jacobpeterson.alpaca.model.endpoint.marketdata.historical.bar.enums.BarTimePeriod
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.logging.Logger


typealias AlpacaPeriod =  BarTimePeriod

/**
 * Get historic data feed from Alpaca
 */
class AlpacaHistoricFeed(apiKey: String? = null, apiSecret: String? = null,  accountType :AccountType = AccountType.PAPER, dataType:DataType = DataType.IEX)  : HistoricFeed {

    private val alpacaAPI: AlpacaAPI
    private val logger: Logger = Logger.getLogger(this.javaClass.simpleName)
    private val events = TreeMap<Instant, MutableList<PriceAction>>()
    private val zoneId: ZoneId = ZoneId.of("America/New_York")

    override val timeline: List<Instant>
        get() = events.keys.toList()

    override val assets
        get() = events.values.map { priceBars -> priceBars.map { it.asset }.distinct() }.flatten().distinct()

    lateinit var availableAssets: List<Asset>


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


    fun retrieve(vararg symbols: String, timeFrame:TimeFrame, period: AlpacaPeriod = AlpacaPeriod.DAY) {
        for (symbol in symbols) {
            val resp = alpacaAPI.marketData().getBars(
                symbol,
                ZonedDateTime.ofInstant(timeFrame.start, zoneId),
                ZonedDateTime.ofInstant(timeFrame.end, zoneId),
                null,
                null,
                1,
                period,
                BarAdjustment.ALL
            )

            val asset = Asset(symbol)
            for (bar in resp.bars) {
                val action = PriceBar(asset, bar.o, bar.h, bar.l, bar.c, bar.v)
                val now = bar.t.toInstant()
                val list = events.getOrPut(now) { mutableListOf() }
                list.add(action)
            }
            logger.fine { "Retrieved asset $asset for $timeFrame"}
        }
    }


    init {
        val connection = AlpacaConnection(apiKey, apiSecret, accountType, dataType)
        alpacaAPI = connection.getAPI()

    }
}

