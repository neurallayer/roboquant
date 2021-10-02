package org.roboquant.jupyter

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.common.clean
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed

/**
 * Shows the correlation between the change in prices between two or more assets
 */
class PriceCorrelationChart(
    private val feed: Feed,
    private val assets: Collection<Asset>,
    private val timeFrame: TimeFrame = TimeFrame.FULL
) : Chart() {

    init {
        require(assets.size > 1) { "Minimum of 2 assets are required, found ${assets.size}" }
    }

    private fun collectPrices(): Map<Asset, List<Double>> = runBlocking {
        val channel = EventChannel(timeFrame = timeFrame)
        val result = mutableMapOf<Asset, MutableList<Double>>()

        val job = launch {
            feed.play(channel)
            channel.close()
        }

        try {
            while (true) {
                val o = channel.receive()
                for (asset in assets) {
                    val price = o.getPrice(asset) ?: Double.NaN
                    val list = result.getOrPut(asset) { mutableListOf()}
                    list.add(price)
                }
            }

        } catch (e: ClosedReceiveChannelException) {

        } finally {
            channel.close()
            if (job.isActive) job.cancel()
        }
        return@runBlocking result
    }

    private fun getMatrix() {
        val calc = PearsonsCorrelation()
        val prices = collectPrices()
        for ((asset1, data1)  in prices)
            for ((asset2, data2) in prices) {
                val data = Pair(data1, data2).clean()
                val corr = calc.correlation(data.first, data.second)
                println("$asset1 $asset2 $corr")
            }
    }


    override fun renderOption(): String {
        val m = getMatrix()
        return ""
    }
}