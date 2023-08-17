/*
 * Copyright 2020-2023 Neural Layer
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
import org.roboquant.common.plus
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.ibkr.IBKR.toContract
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.set

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
     * Retrieve historical price-bars from IBKR for the given [assets].
     *
     * Valid [duration] units:
     * ```
     * S	Seconds
     * D	Day
     * W	Week
     * M	Month
     * Y	Year
     * ```
     *
     * Valid [barSize] values:
     * ```
     * 1 secs	5 secs	10 secs	15 secs	30 secs
     * 1 min    2 mins	3 mins	5 mins	10 mins	15 mins	20 mins	30 mins
     * 1 hour	2 hours	3 hours	4 hours	8 hours
     * 1 day
     * 1 week
     * 1 month
     * ```
     *
     * Historical Data requests need to be assembled in such a way that only a few thousand bars are returned at a time
     * due to API limits of IBKR. So you cannot retrieve short [barSize] for a very long [duration].
     */
    fun retrieve(
        assets: Collection<Asset>,
        endDate: Instant = Instant.now(),
        duration: String = "1 Y",
        barSize: String = "1 day",
        dataType: String = "TRADES"
    ) {
        for (asset in assets) {
            val contract = asset.toContract()
            val formatted = IBKR.getFormattedTime(endDate)
            client.reqHistoricalData(++tickerId, contract, formatted, duration, barSize, dataType, 1, 1, false, null)
            subscriptions[tickerId] = asset
        }
    }

    /**
     * @see retrieve
     */
    fun retrieve(
        vararg assets: Asset,
        endDate: Instant = Instant.now(),
        duration: String = "1 Y",
        barSize: String = "1 day",
        dataType: String = "TRADES"
    ) = retrieve(assets.toList(), endDate, duration, barSize, dataType)

    /**
     * Block until all the subscribed data is retrieved. Right now, the implementation is a sleep statement.
     * But in the future it will be replaced with a better mechanism.
     */
    fun waitTillRetrieved(maxMillis: Int = 10_000) {
        val endTime = Instant.now() + maxMillis.millis
        while (subscriptions.isNotEmpty() && Instant.now() <= endTime) Thread.sleep(1_000)
    }

    private inner class Wrapper(logger: Logging.Logger) : BaseWrapper(logger) {

        private val dtf = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss")
        private val df = DateTimeFormatter.ofPattern("yyyyMMdd")

        override fun historicalData(reqId: Int, bar: Bar) {
            val asset = subscriptions.getValue(reqId)
            val action = PriceBar(
                asset, bar.open(), bar.high(), bar.low(), bar.close(), bar.volume().value().toDouble()
            )
            val timeStr = bar.time()
            val time = if (timeStr.length > 10)
                LocalDateTime.parse(timeStr, dtf).toInstant(ZoneOffset.UTC)
            else
                asset.exchange.getClosingTime(LocalDate.parse(timeStr, df))
            add(time, action)
            logger.trace { "bar at $timeStr tranlated into $time and $action" }
        }

        override fun historicalDataEnd(reqId: Int, startDateStr: String?, endDateStr: String?) {
            val v = subscriptions.remove(reqId)
            logger.info("Finished retrieving $v")
        }

    }

}