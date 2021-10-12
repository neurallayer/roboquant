package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.instrument.InstrumentCandlesRequest
import com.oanda.v20.primitives.InstrumentName
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.ForexBuilder
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Retrieve historic FOREX data from OANDA.
 */
class OANDAHistoricFeed(token: String? = null , url: String = "https://api-fxpractice.oanda.com/") : HistoricPriceFeed() {

    private val ctx: Context
    private val template = Asset("", AssetType.FOREX)

    init {
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) {"Couldn't locate API token OANDA_API_KEY"}
        ctx = ContextBuilder(url)
            .setToken(apiToken)
            .setApplication("roboquantHistoricFeed")
            .build()
    }

    fun retrieveCandles(vararg symbols: String) {
        val tf = TimeFrame.pastPeriod(60, ChronoUnit.MINUTES)
        for (symbol in symbols) {
            val request = InstrumentCandlesRequest(InstrumentName(symbol))
                .setPrice("M")
                .setFrom(tf.start.toString())
                .setTo(tf.end.toString())
            val resp = ctx.instrument.candles(request)
            val asset = ForexBuilder().invoke(symbol, template)
            resp.candles.forEach {
                with (it.mid) {
                    val action = PriceBar(asset, o.doubleValue(), h.doubleValue(), l.doubleValue(), c.doubleValue(), it.volume)
                    val now = Instant.parse(it.time)
                    add(now, action)
                }
            }
        }
    }


}