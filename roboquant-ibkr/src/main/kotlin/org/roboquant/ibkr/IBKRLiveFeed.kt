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

import com.ib.client.Decimal
import com.ib.client.EClientSocket
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.ibkr.IBKR.toContract
import java.time.Instant

/**
 * Get realtime bars from IBKR. Please note that often you need paid subscriptions to get this
 * data and additional there are limitations to the frequency of API calls you can make.
 *
 * The default settings like the port number are the ones for a paper trading account. It is easy to
 * share the market data subscriptions between live and paper trading accounts, so it is recommended to
 * use a paper trading account if possible at all.
 *
 * @constructor
 */
class IBKRLiveFeed(configure: IBKRConfig.() -> Unit = {}) : LiveFeed() {

    private val config = IBKRConfig()
    private var reqId: Int = 0
    private var client: EClientSocket
    private val subscriptions = mutableMapOf<Int, Asset>()
    private val logger = Logging.getLogger(IBKRLiveFeed::class)

    init {
        config.configure()
        val wrapper = Wrapper(logger)
        client = IBKR.connect(wrapper, config)
        client.reqCurrentTime()
    }

    /**
     * Disconnect from the IBKR APIs and stop receiving market data
     */
    fun disconnect() = IBKR.disconnect(client)

    /**
     * Subscribe to the realtime bars for a particular contract. Often IBKR platform requires a subscription in order
     * to be able to receive realtime bars. Please check the documentation at the IBKR website for more details.
     *
     * @param assets
     */
    fun subscribe(assets: Collection<Asset>, interval: Int = 5, type: String = "MIDPOINT") {
        for (asset in assets) {
            val contract = asset.toContract()
            client.reqRealTimeBars(++reqId, contract, interval, type, false, arrayListOf())
            subscriptions[reqId] = asset
            logger.info("Added subscription to receive realtime bars for ${contract.symbol()}")
        }
    }

    /**
     * @see subscribe
     */
    fun subscribe(vararg assets: Asset, interval: Int = 5, type: String = "MIDPOINT") =
        subscribe(assets.toList(), interval, type)


    private inner class Wrapper(logger: Logging.Logger) : BaseWrapper(logger) {

        override fun realtimeBar(
            reqId: Int,
            time: Long,
            open: Double,
            high: Double,
            low: Double,
            close: Double,
            volume: Decimal?,
            wap: Decimal?,
            count: Int
        ) {
            val asset = subscriptions[reqId]
            if (asset == null) {
                logger.warn("unexpected realtimeBar received with request id $reqId")
            } else {
                val v = volume?.value()?.toDouble() ?: Double.NaN
                val action = PriceBar(asset, open, high, low, close, v)
                val now = Instant.ofEpochSecond(time) // IBKR uses seconds resolution
                val event = Event(listOf(action), now)
                send(event)
            }
        }

    }

}

