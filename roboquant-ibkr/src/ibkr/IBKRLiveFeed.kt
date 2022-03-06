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

import com.ib.client.EClientSocket
import ibkr.BaseWrapper
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
 * @param host The host to connect to
 * @param port The port to connect to
 * @param clientId The client id to use. By default, roboquant uses clientId=2 for the IBKR feed
 */
class IBKRLiveFeed(host: String = "127.0.0.1", port: Int = 4002, clientId: Int = 2) : LiveFeed() {

    private var tickerId: Int = 0
    private var client: EClientSocket
    private val subscriptions = mutableMapOf<Int, Asset>()
    val logger = Logging.getLogger(IBKRLiveFeed::class)

    init {
        val wrapper = Wrapper(logger)
        client = IBKRConnection.connect(wrapper, host, port, clientId)
        client.reqCurrentTime()
    }

    fun disconnect() = IBKRConnection.disconnect(client)

    /**
     * Subscribe to the realtime bars for a particular contract. Often IBKR platform requires a subscription in order
     * to be able to receive realtime bars. Please check the documentation at hte IBKR website for more details.
     *
     * @param assets
     */
    fun subscribe(assets: Collection<Asset>, interval: Int = 5, type: String = "MIDPOINT") {
        for (asset in assets) {
            try {
                val contract = IBKRConnection.getContract(asset)
                client.reqRealTimeBars(++tickerId, contract, interval, type, false, arrayListOf())
                subscriptions[tickerId] = asset
                logger.info("Added subscription to receive realtime bars for ${contract.symbol()}")
            } catch (e: Exception) {
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
            volume: Long,
            wap: Double,
            count: Int
        ) {
            val asset = subscriptions[reqId]
            if (asset == null) {
                logger.warning("unexpected realtimeBar received with request id $reqId")
            } else {
                val action = PriceBar(asset, open, high, low, close, volume.toDouble())
                val now = Instant.ofEpochSecond(time) // IBKR uses seconds resolution
                val event = Event(listOf(action), now)
                channel?.offer(event)
            }
        }

        override fun error(var1: Exception) {
            logger.severe("Received exception", var1)
        }

        override fun error(var1: String?) {
            logger.warning { "$var1" }
        }

        override fun error(var1: Int, var2: Int, var3: String?) {
            if (var1 == -1)
                logger.fine { "$var1 $var2 $var3" }
            else
                logger.warning { "$var1 $var2 $var3" }
        }

    }

}

