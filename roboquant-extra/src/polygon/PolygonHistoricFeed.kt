package org.roboquant.polygon

import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant

data class PolygonConfig(
    var key: String = org.roboquant.common.Config.getProperty("polygon.key", ""),
    var timeout: Int = 10
)

class PolygonHistoricFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    val config = PolygonConfig()
    var client: PolygonRestClient

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = PolygonRestClient(config.key)
    }

    fun retrieve(symbol: String, tf: Timeframe) {
        val aggr = client.getAggregatesBlocking(
            AggregatesParameters(
                symbol,
                1,
                "day",
                tf.start.toEpochMilli().toString(),
                tf.end.toEpochMilli().toString()
            )
        )
        // val bars = client.stocksClient.getDailyOpenCloseBlocking(symbol, date = "2022-08-04", unadjusted = false)
        for (bar in aggr.results) {
            val action =
                PriceBar(Asset(symbol), doubleArrayOf(bar.open!!, bar.high!!, bar.low!!, bar.close!!, bar.volume!!))
            val time = Instant.ofEpochMilli(bar.timestampMillis!!)
            super.add(time, action)
        }
    }

}