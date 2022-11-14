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

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.account.AccountID
import com.oanda.v20.instrument.CandlestickGranularity
import com.oanda.v20.instrument.InstrumentCandlesRequest
import com.oanda.v20.primitives.InstrumentName
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Retrieve historic data from OANDA. Right now only [PriceBar] (= candlesticks) data is supported.
 *
 * @param configure additional configuration
 */
class OANDAHistoricFeed(configure: OANDAConfig.() -> Unit = {}) : HistoricPriceFeed() {

    private val config = OANDAConfig()
    private val ctx: Context
    private val accountID: AccountID
    private val logger = Logging.getLogger(OANDAHistoricFeed::class)

    init {
        config.configure()
        ctx = OANDA.getContext(config)
        accountID = OANDA.getAccountID(config.account, ctx)
    }

    private val availableAssetsMap by lazy {
        OANDA.getAvailableAssets(ctx, accountID)
    }

    /**
     * Return the available assets to retrieve
     */
    val availableAssets
        get() = availableAssetsMap.values

    /**
     * Retrieve historic data for the provided [symbols]
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe = Timeframe.past(1.days),
        granularity: String = "M1",
        priceType: String = "M",
    ) {
        for (symbol in symbols) require(symbol in availableAssetsMap) { "symbol=$symbol not available"}
        for (symbol in symbols) {
            val request = InstrumentCandlesRequest(InstrumentName(symbol))
                .setPrice(priceType)
                .setFrom(timeframe.start.toString())
                .setTo(timeframe.end.toString())
                .setGranularity(CandlestickGranularity.valueOf(granularity))
            val resp = ctx.instrument.candles(request)
            val asset = availableAssetsMap[resp.instrument.toString()]!!
            if (resp.candles.isEmpty()) logger.warn("No candles retrieved for $symbol for period $timeframe")
            resp.candles.forEach {
                with(it.mid) {
                    val action =
                        PriceBar(
                            asset,
                            o.doubleValue(),
                            h.doubleValue(),
                            l.doubleValue(),
                            c.doubleValue(),
                            it.volume.toDouble()
                        )
                    val now = Instant.parse(it.time)
                    add(now, action)
                }
            }
        }
    }

}