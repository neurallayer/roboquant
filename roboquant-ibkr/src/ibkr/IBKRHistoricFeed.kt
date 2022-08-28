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
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.millis
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.csv.AutoDetectTimeParser
import java.time.Instant
import java.util.logging.Logger

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
        client = IBKRConnection.connect(wrapper, config)
        client.reqCurrentTime()
    }

    fun disconnect() = IBKRConnection.disconnect(client)

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
            val contract = IBKRConnection.getContract(asset)
            val formatted = IBKRConnection.getFormattedTime(endDate)
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

    fun waitTillRetrieved(maxMillis: Int = 10_000) {
        val endTime = Instant.now() + maxMillis.millis
        while (subscriptions.isNotEmpty() && Instant.now() <= endTime) Thread.sleep(1_000)
    }

    inner class Wrapper(logger: Logger) : BaseWrapper(logger) {

        override fun historicalData(reqId: Int, bar: Bar) {
            val asset = subscriptions[reqId]!!
            val action = PriceBar(
                asset, bar.open(), bar.high(), bar.low(), bar.close(), bar.volume().value().toDouble()
            )
            val parser = AutoDetectTimeParser()
            val time = parser.parse(bar.time(), asset.exchange)
            add(time, action)
        }

        override fun historicalDataEnd(reqId: Int, startDateStr: String, endDateStr: String) {
            val v = subscriptions.remove(reqId)
            logger.info("Finished retrieving $v")
        }

    }

}