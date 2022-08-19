package org.roboquant.polygon

import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Configuration for [PolygonHistoricFeed]
 */
data class PolygonConfig(
    var key: String = Config.getProperty("polygon.key", "")
)

/**
* Historic data feed using market data from Polygon.io
*/
class PolygonHistoricFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    val config = PolygonConfig()
    private var client: PolygonRestClient

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = PolygonRestClient(config.key)
    }

    fun retrieve(symbol: String, tf: Timeframe, multiplier:Int = 1, timespan: String = "day", limit: Int = 5000) {
        val aggr = client.getAggregatesBlocking(
            AggregatesParameters(
                symbol,
                multiplier.toLong(),
                timespan,
                tf.start.toEpochMilli().toString(),
                tf.end.toEpochMilli().toString(),
                limit = limit.toLong()
            )
        )

        for (bar in aggr.results) {
            val action =
                PriceBar(Asset(symbol), doubleArrayOf(bar.open!!, bar.high!!, bar.low!!, bar.close!!, bar.volume!!))
            val time = Instant.ofEpochMilli(bar.timestampMillis!!)
            add(time, action)
        }
    }

}