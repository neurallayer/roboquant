/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.tickerall

import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.common.PriceBar
import org.roboquant.common.TimeSpan
import org.roboquant.feeds.HistoricPriceFeed
import java.time.Instant

/**
 * Historic price feed that retrieves OHLC candles for one connected MetaTrader account from TickerAll. The
 * retrieved bars are stored in memory and can be replayed like any other roboquant historic feed.
 *
 * @param configure configuration for connecting to the TickerAll API
 * @constructor Create a new instance of the TickerAllHistoricFeed
 */
class TickerAllHistoricFeed(
    configure: TickerAllConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val config = TickerAllConfig()
    private val client: TickerAllClient
    private val logger = Logging.getLogger(TickerAllHistoricFeed::class)

    init {
        config.configure()
        client = TickerAll.getClient(config)
    }

    /**
     * Retrieve [PriceBar]s for the given [symbols] at the given [timeframe] resolution, covering the last
     * [hours] hours. Supported timeframes are `M1`, `M5`, `M15`, `M30`, `H1`, `H4`, `D1`, `W1` and `MN1`.
     * Coarser timeframes reach further back in history.
     */
    fun retrieve(vararg symbols: String, timeframe: String = "H1", hours: Int = 720) {
        val timeSpan = timeSpanFor(timeframe)
        for (symbol in symbols) {
            val asset = TickerAll.toAsset(symbol, Currency.USD)
            for (candle in client.getCandles(symbol, hours, timeframe)) {
                val ts = candle.timestamp ?: continue
                val open = candle.open ?: continue
                val high = candle.high ?: continue
                val low = candle.low ?: continue
                val close = candle.close ?: continue
                val bar = PriceBar(asset, open, high, low, close, (candle.tickVolume ?: 0L).toDouble(), timeSpan)
                add(Instant.ofEpochSecond(ts), bar)
            }
        }
        logger.debug { "retrieved candles symbols=${symbols.toList()} timeframe=$timeframe hours=$hours" }
    }

    private fun timeSpanFor(timeframe: String): TimeSpan = when (timeframe.uppercase()) {
        "M1" -> TimeSpan(minutes = 1)
        "M5" -> TimeSpan(minutes = 5)
        "M15" -> TimeSpan(minutes = 15)
        "M30" -> TimeSpan(minutes = 30)
        "H1" -> TimeSpan(hours = 1)
        "H4" -> TimeSpan(hours = 4)
        "D1" -> TimeSpan(days = 1)
        "W1" -> TimeSpan(days = 7)
        "MN1" -> TimeSpan(months = 1)
        else -> TimeSpan(hours = 1)
    }
}
