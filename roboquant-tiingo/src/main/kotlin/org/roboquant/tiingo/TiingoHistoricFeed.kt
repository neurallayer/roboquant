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

import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.NamedCsvRecord
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit


/**
 * Tiingo historic feed.
 *
 * This feed uses CSV format for faster processing and less bandwidth usage.
 */
class TiingoHistoricFeed(
    configure: TiingoConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val config = TiingoConfig()
    private val logger = Logging.getLogger(TiingoHistoricFeed::class)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()


    init {
        config.configure()
        require(config.key.isNotBlank()) { "no valid key found" }
    }


    private fun getParams(timeframe: Timeframe, frequency: String): Map<String, Any> {
        return mapOf(
            "startDate" to Exchange.US.getLocalDate(timeframe.start),
            "endDate" to Exchange.US.getLocalDate(timeframe.end),
            "resampleFreq" to frequency,
            "format" to "csv",
            "token" to config.key
        )
    }

    private fun getDailyUrl(symbol: String, params: Map<String, Any>): HttpUrl {
        val eodUrl = "https://api.tiingo.com/tiingo/daily/$symbol/prices".toHttpUrl()
        val urlBuilder = eodUrl.newBuilder()
        for ((k,v) in params.entries) urlBuilder.addQueryParameter(k,v.toString())
        return urlBuilder.build()
    }

    private fun getIntradayUrl(symbol: String, params: Map<String, Any>): HttpUrl {
        val eodUrl = "https://api.tiingo.com/iex/$symbol/prices".toHttpUrl()
        val urlBuilder = eodUrl.newBuilder()
        for ((k,v) in params.entries) urlBuilder.addQueryParameter(k,v.toString())
        return urlBuilder.build()
    }


    /**
     * Retrieve the historic end-of-day price-bars for provided [symbols] and [timeframe].
     * If required, the end-of-day prices can be resampled into a lower [frequency] like "weekly" or "monthly"
     *
     * The price-bar include the adjusted prices.
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe = Timeframe.past(1.years),
        frequency: String = "daily"
    ) {

        val params = getParams(timeframe, frequency)

        for (symbol in symbols) {
            val asset = Asset(symbol)
            val url = getDailyUrl(symbol, params)

            url.forEach {
                val pb = PriceBar(
                    asset,
                    it.getField("adjOpen").toDouble(),
                    it.getField("adjHigh").toDouble(),
                    it.getField("adjLow").toDouble(),
                    it.getField("adjClose").toDouble(),
                    it.getField("adjVolume").toDouble()
                )
                val date = LocalDate.parse(it.getField("date"))
                val eodTime = Exchange.US.getClosingTime(date)
                super.add(eodTime, pb)
            }

        }

    }

    private inline fun HttpUrl.forEach(block: (NamedCsvRecord) -> Unit) {
        val request = Request.Builder().url(this).build()
        val resp = client.newCall(request).execute()
        if (! resp.isSuccessful) {
            logger.warn { "error while retrieving message=${resp.message}" }
            return
        }

        val reader = resp.body.charStream()
        reader.use {
            var found = false
            CsvReader.builder().ofNamedCsvRecord(it).forEach { record ->
                found = true
                block(record)
            }

            if (! found) logger.warn { "No entries found" }
        }

    }

    /**
     * Retrieve the historic intraday price-bars for provided [symbols] and [timeframe].
     * The [frequency] determines the time-span, like `15min` or `4hour`. The default is `5min`
     *
     * Note: The price-bar include the un-adjusted prices.
     */
    fun retrieveIntraday(
        vararg symbols: String,
        timeframe: Timeframe = Timeframe.past(5.days),
        frequency: String = "5min"
    ) {
        require(frequency.endsWith("hour") || frequency.endsWith("min")) {"only hour and min frequencies are allowed"}

        val params = getParams(timeframe, frequency)

        for (symbol in symbols) {
            val asset = Asset(symbol)
            val url = getIntradayUrl(symbol, params)

            url.forEach {
                val pb = PriceBar(
                    asset,
                    it.getField("open").toDouble(),
                    it.getField("high").toDouble(),
                    it.getField("low").toDouble(),
                    it.getField("close").toDouble(),
                    it.getField("volume").toDouble()
                )
                val time =  Instant.parse(it.getField("date").replace(' ', 'T'))
                super.add(time, pb)
            }

        }

    }





}
