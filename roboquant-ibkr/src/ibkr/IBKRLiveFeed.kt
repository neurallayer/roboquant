/*
 * Copyright 2021 Neural Layer
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
import org.roboquant.common.severe
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant
import java.util.logging.Logger

/**
 * Get realtime bars from IBKR. Please note that often you need paid subscriptions to get this
 * data and additional there are limitations to the frequency of API calls you can make.
 *
 * The default settings like the port number are the ones for a paper trading account. It is easy to
 * share the market data subscriptions between live and paper trading accounts, so it is recommended to
 * use a paper trading account if possible at all.
 *
 * @constructor
 *
 */
class IBKRLiveFeed(configure: IBKRConfig.() -> Unit = {}) : LiveFeed() {

    private val config = IBKRConfig()

    private var tickerId: Int = 0
    private var client: EClientSocket
    private val subscriptions = mutableMapOf<Int, Asset>()
    private val logger = Logging.getLogger(IBKRLiveFeed::class)

    init {
        config.configure()
        val wrapper = Wrapper(logger)
        client = IBKRConnection.connect(wrapper, config)
        client.reqCurrentTime()
    }

    fun disconnect() = IBKRConnection.disconnect(client)

    /**
     * Subscribe to the realtime bars for a particular contract. Often IBKR platform requires a subscription in order
     * to be able to receive realtime bars. Please check the documentation at hte IBKR website for more details.
     *
     * @param assets
     */
    @Suppress("TooGenericExceptionCaught")
    fun subscribe(assets: Collection<Asset>, interval: Int = 5, type: String = "MIDPOINT") {
        for (asset in assets) {
            try {
                val contract = IBKRConnection.getContract(asset)
                client.reqRealTimeBars(++tickerId, contract, interval, type, false, arrayListOf())
                subscriptions[tickerId] = asset
                logger.info("Added subscription to receive realtime bars for ${contract.symbol()}")
            } catch (e: Throwable) {
                logger.severe("Error during subscribing to $asset", e)
            }
        }
    }

    fun subscribe(vararg assets: Asset, interval: Int = 5, type: String = "MIDPOINT") =
        subscribe(assets.toList(), interval, type)

    inner class Wrapper(logger: Logger) : BaseWrapper(logger) {

        override fun realtimeBar(
            reqId: Int,
            time: Long,
            open: Double,
            high: Double,
            low: Double,
            close: Double,
            volume: Decimal,
            wap: Decimal,
            count: Int
        ) {
            val asset = subscriptions[reqId]
            if (asset == null) {
                logger.warning("unexpected realtimeBar received with request id $reqId")
            } else {
                val action = PriceBar(asset, open, high, low, close, volume.value().toDouble())
                val now = Instant.ofEpochSecond(time) // IBKR uses seconds resolution
                val event = Event(listOf(action), now)
                send(event)
            }
        }

    }

}

