/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ibkr

import com.ib.client.Bar
import com.ib.client.EClientSocket
import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.util.AutoDetectTimeParser
import org.roboquant.ibkr.IBKR.getContract
import java.time.Instant

/**
 * Historic prices from IBKR APIs
 *
 * @param configure additional configuration
 */
class IBKRHistoricFeed(
    configure: IBKRConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val config = IBKRConfig()
    private var tickerId: Int = 0
    private val subscriptions = mutableMapOf<Int, Asset>()
    private val logger = Logging.getLogger(IBKRHistoricFeed::class)
    private var client: EClientSocket

    init {
        config.configure()
        val wrapper = Wrapper(logger)
        client = IBKR.connect(wrapper, config)
        client.reqCurrentTime()
    }

    /**
     * Disconnect this client
     */
    fun disconnect() = IBKR.disconnect(client)

    /**
     * Historical Data requests need to be assembled in such a way that only a few thousand bars are returned at a time.
     * So you cannot retrieve short barSize for a very long duration.
     */
    fun retrieve(
        assets: Collection<Asset>,
        endDate: Instant = Instant.now(),
        duration: String = "1 Y",
        barSize: String = "1 day",
        dataType: String = "TRADES"
    ) {
        for (asset in assets) {
            val contract = asset.getContract()
            val formatted = IBKR.getFormattedTime(endDate)
            client.reqHistoricalData(++tickerId, contract, formatted, duration, barSize, dataType, 1, 1, false, null)
            subscriptions[tickerId] = asset
        }
    }

    /**
     * Historical Data requests need to be assembled in such a way that only a few thousand bars are returned at a time.
     * So you cannot retrieve short barSize for a very long duration.
     */
    fun retrieve(
        vararg assets: Asset,
        endDate: Instant = Instant.now(),
        duration: String = "1 Y",
        barSize: String = "1 day",
        dataType: String = "TRADES"
    ) = retrieve(assets.toList(), endDate, duration, barSize, dataType)

    /**
     * Block until all the subscribed data is retrieved. Right now just a simple sleep, in the future replaced with
     * better mechanism.
     */
    fun waitTillRetrieved(maxMillis: Int = 10_000) {
        val endTime = Instant.now() + maxMillis.millis
        while (subscriptions.isNotEmpty() && Instant.now() <= endTime) Thread.sleep(1_000)
    }

    private inner class Wrapper(logger: Logging.Logger) : BaseWrapper(logger) {

        private val timeParsers = mutableMapOf<Int, AutoDetectTimeParser>()

        override fun historicalData(reqId: Int, bar: Bar) {
            val asset = subscriptions[reqId]!!
            val action = PriceBar(
                asset, bar.open(), bar.high(), bar.low(), bar.close(), bar.volume().value().toDouble()
            )
            val parser = timeParsers.getOrPut(reqId) { AutoDetectTimeParser() }
            val timeStr = bar.time()
            val time = parser.parse(timeStr, asset.exchange)
            add(time, action)
            logger.trace { "bar at $timeStr tranlated into $time and $action" }
        }

        override fun historicalDataEnd(reqId: Int, startDateStr: String, endDateStr: String) {
            val v = subscriptions.remove(reqId)
            timeParsers.remove(reqId)
            logger.info("Finished retrieving $v")
        }

    }

}