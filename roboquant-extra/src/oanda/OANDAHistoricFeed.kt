package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.instrument.CandlestickGranularity
import com.oanda.v20.instrument.InstrumentCandlesRequest
import com.oanda.v20.primitives.InstrumentName
import oanda.OANDAConnection
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Retrieve historic FOREX data from OANDA.
 */
class OANDAHistoricFeed(token: String? = null, demoAccount: Boolean = true) : HistoricPriceFeed() {

    private val ctx: Context = OANDAConnection.getContext(token, demoAccount)
    val availableAssets = OANDAConnection.getAvailableAssets(ctx)

    fun retrieveCandles(
        vararg symbols: String,
        timeFrame: TimeFrame = TimeFrame.pastPeriod(1, ChronoUnit.DAYS),
        granularity: String = "M1",
        priceType: String = "M",
    ) {
        for (symbol in symbols) {
            val request = InstrumentCandlesRequest(InstrumentName(symbol))
                .setPrice(priceType)
                .setFrom(timeFrame.start.toString())
                .setTo(timeFrame.end.toString())
                .setGranularity(CandlestickGranularity.valueOf(granularity))
            val resp = ctx.instrument.candles(request)
            val asset = availableAssets[resp.instrument.toString()]!!
            resp.candles.forEach {
                with(it.mid) {
                    val action =
                        PriceBar(asset, o.doubleValue(), h.doubleValue(), l.doubleValue(), c.doubleValue(), it.volume)
                    val now = Instant.parse(it.time)
                    add(now, action)
                }
            }
        }
    }



}