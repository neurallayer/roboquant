/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.samples

import com.numericalmethod.suanshu.stats.cointegration.CointegrationMLE
import com.numericalmethod.suanshu.stats.cointegration.JohansenAsymptoticDistribution
import com.numericalmethod.suanshu.stats.cointegration.JohansenTest
import com.numericalmethod.suanshu.stats.timeseries.multivariate.realtime.SimpleMultiVariateTimeSeries
import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.common.*
import org.roboquant.feeds.AssetFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceType
import org.roboquant.feeds.applyEvents
import org.roboquant.loggers.SilentLogger
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy

/**
 * These values should not be conintegrated
 */
private fun getTimeSeriesAppleTesla(feed: AssetFeed): SimpleMultiVariateTimeSeries {
    val assets = feed.assets.findBySymbols("AAPL", "TSLA")
    val result = mutableListOf<DoubleArray>()
    feed.applyEvents { event ->
        val prices = assets.map { event.getPrice(it) ?: Double.NaN }
        if (prices.all { it.isFinite() }) result.add(prices.toDoubleArray())
    }
    return SimpleMultiVariateTimeSeries(*result.toTypedArray())

}

/**
 * The HIGH-LOW values for a single asset should be conintegrated
 */
private fun getTimeSeriesHighLow(feed: AssetFeed): SimpleMultiVariateTimeSeries {
    val apple = feed.assets.getBySymbol("AAPL")
    val result = mutableListOf<DoubleArray>()
    feed.applyEvents { event ->
        val price = event.prices[apple]
        if (price != null) result.add(doubleArrayOf(price.getPrice(PriceType.HIGH), price.getPrice(PriceType.HIGH)))

    }
    return SimpleMultiVariateTimeSeries(*result.toTypedArray())

}

fun runJohansenTest(ts: SimpleMultiVariateTimeSeries) {
    // Lets do the cointegration
    val coint = CointegrationMLE(ts, true, 2)

    // Lets test if they are actually cointegratedd
    val test = JohansenTest(
        JohansenAsymptoticDistribution.Test.EIGEN,
        JohansenAsymptoticDistribution.TrendType.RESTRICTED_CONSTANT,
        coint.rank()
    )
    println("JohansenTest statistics: ${test.getStats(coint)}")
    println("JohansenTest r@0.05: ${test.r(coint, 5.percent)}")
}



fun testStrat(feed: AssetFeed) {

    class PairTradingStrategy(val period: Int, val priceType: PriceType = PriceType.DEFAULT)  : Strategy {


        /**
         * Contains the history of all assets
         */
        private val history = mutableMapOf<Asset, PriceSeries>()


        fun johansenTest(data: Array<DoubleArray>): Int {
            val ts = SimpleMultiVariateTimeSeries(*data)
            val coint = CointegrationMLE(ts, true, 2)

            // Lets test if they are actually cointegratedd
            val test = JohansenTest(
                JohansenAsymptoticDistribution.Test.EIGEN,
                JohansenAsymptoticDistribution.TrendType.RESTRICTED_CONSTANT,
                coint.rank()
            )
            val result =  test.r(coint, 5.percent)
            if (result != 0) {
                println(coint.alpha())
                println(coint.beta())
            }
            return result
        }

        override fun generate(event: Event): List<Signal> {
            val contenders = mutableMapOf<Asset, DoubleArray>()
            for ((asset, action) in event.prices) {
                val priceSeries = history.getOrPut(asset) { PriceSeries(period) }
                val price = action.getPrice(priceType)
                if (priceSeries.add(price)) {
                    val data = priceSeries.toDoubleArray()
                    contenders[asset] = data
                }
            }
            for ((asset1, data1) in contenders) {
                for ((asset2, data2) in contenders) {
                    if (asset1 == asset2) continue
                    val fData = mutableListOf<DoubleArray>()
                    for (i in data1.indices) fData.add(doubleArrayOf(data1[i], data2[i]))
                    val r = johansenTest(fData.toTypedArray())
                    if (r != 0) println("${asset1.symbol} ${asset2.symbol} $r")
                }

            }

            return emptyList()
        }

    }

    val strat = PairTradingStrategy(60)
    val rq = Roboquant(strat, logger= SilentLogger())
    rq.run(feed)

}



fun main() {
    val feed = AvroFeed.sp500()
    runJohansenTest(getTimeSeriesAppleTesla(feed))
    runJohansenTest(getTimeSeriesHighLow(feed))
    testStrat(feed)
}


