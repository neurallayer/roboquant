package org.roboquant.jupyter

import org.roboquant.brokers.Trade
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

/**
 * Plot the price of an asset and optionally the trades made for that same asset.
 *
 * @constructor Create new metric chart
 */
class PriceChart(
    val feed: Feed,
    val asset: Asset,
    val trades: List<Trade> = listOf(),
    val timeFrame: TimeFrame = TimeFrame.FULL,
    val priceType: String = "DEFAULT",
    private val scale: Int = 2
) : Chart() {

    /**
     * Play a feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     * @return
     */
    private fun fromFeed(): List<Pair<Instant, BigDecimal>> {
        val entries = feed.filter<PriceAction>(timeFrame) { it.asset == asset }
        val data = entries.map {
            val value = BigDecimal(it.second.getPrice(priceType)).setScale(scale, RoundingMode.HALF_DOWN)
            it.first to value
        }

        return data
    }


    private fun markPoints(): List<Map<String, Any>> {
        val t = trades.filter { it.asset == asset && timeFrame.contains(it.time)}
        val result = mutableListOf<Map<String, Any>>()
        for (trade in t) {
            val entry = mapOf(
                "value" to trade.quantity.toInt(), "xAxis" to trade.time.toString(), "yAxis" to trade.price
            )
            result.add(entry)
        }
        return result
    }

    /** @suppress */
    override fun renderOption(): String {
        val line = fromFeed()
        val lineData =  gsonBuilder.create().toJson(line)
        val timeFrame = if (line.isNotEmpty()) TimeFrame(line.first().first, line.last().first).toPrettyString() else ""

        val marks = markPoints()
        val markData =  gsonBuilder.create().toJson(marks)

        val series = """
            {
                name: '${asset.symbol}',
                type: 'line',
                showSymbol: false,
                lineStyle: {
                    width: 1
                },
                data : $lineData,
                markPoint: {
                    data: $markData
                },
            },
        """

        return """
            {
                xAxis: {
                    type: 'time',
                    scale: true
                },
                yAxis: {
                    type: 'value',
                    scale: true
                },
                 title: {
                    text: '${asset.symbol} $timeFrame'
                },
                tooltip: {
                    trigger: 'axis'
                },
                ${renderDataZoom()},
                ${renderToolbox()},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }


}