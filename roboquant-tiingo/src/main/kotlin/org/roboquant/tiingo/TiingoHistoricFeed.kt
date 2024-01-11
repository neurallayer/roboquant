/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.tiingo

import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import top.cptl.tiingo4j.apis.TiingoApi
import top.cptl.tiingo4j.enums.RESAMPLE_FREQUENCY
import top.cptl.tiingo4j.requestParameters.PriceParameters
import java.time.Instant


/**
 * Tiingo historic feed
 *
 * This feed uses web-sockets for low letency and has nanosecond resolution
 */
class TiingoHistoricFeed(
    configure: TiingoConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private var api: TiingoApi
    private val config = TiingoConfig()
    private val logger = Logging.getLogger(TiingoHistoricFeed::class)

    init {
        config.configure()
        require(config.key.isNotBlank()) { "no valid key found" }
        api = TiingoApi(config.key)
    }

    /**
     * Subscribe to quotes for provided [symbols].
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe = Timeframe.past(1.years),
        frequency: String = "DAILY"
    ) {
        val from = Exchange.US.getLocalDate(timeframe.start)
        val to = Exchange.US.getLocalDate(timeframe.end)
        val freq = RESAMPLE_FREQUENCY.valueOf(frequency)

        val params = PriceParameters()
            .setStartDate(from.toString())
            .setEndDate(to.toString())
            .setResampleFrequency(freq)

        for (symbol in symbols) {
            val prices = api.getPrices(symbol, params)
            val asset = Asset(symbol)
            for (price in prices) {
                val pb = PriceBar(asset, price.adjOpen, price.adjHigh, price.adjLow, price.adjClose, price.adjVolume)
                val time = Instant.parse(price.date)
                super.add(time, pb)
            }
        }

    }


}
