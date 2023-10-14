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
import org.roboquant.common.seconds
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.ibkr.IBKR.toContract
import java.time.Instant

/**
 * Get realtime bars from IBKR. Please note that often you need paid subscriptions to get this
 * data and additional there are limitations to the frequency of API calls you can make.
 *
 * The default settings like the port number are the ones for a paper trading account. It is convenient to
 * share the market data subscriptions between live and paper trading accounts, so it is recommended to
 * use a paper trading account if possible at all.
 *
 * @constructor
 */
class IBKRLiveFeed(configure: IBKRConfig.() -> Unit = {}) : LiveFeed(30_000) {

    private class Subscription(val asset: Asset, val interval: Int)

    private val config = IBKRConfig()
    private var reqId: Int = 0
    private var client: EClientSocket
    private val subscriptions = mutableMapOf<Int, Subscription>()
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
     * Subscribe to the realtime bars for a set of [assets].
     *
     * The [interval] specifies the number of seconds between the
     * price-bars. IBKR currently only supports 5 seconds.
     *
     * The [type] specifies what price is used to calculate the price-bars, default being MIDPOINT of the order-book.
     * All the available options for the [type] parameter are:
     *
     * - TRADES
     * - MIDPOINT
     * - BID
     * - ASK
     *
     * Often IBKR platform requires a subscription in order to be able to receive realtime bars. Please check the
     * documentation on the IBKR website for more details.
     */
    fun subscribe(assets: Collection<Asset>, interval: Int = 5, type: String = "MIDPOINT") {
        for (asset in assets) {
            val contract = asset.toContract()
            client.reqRealTimeBars(++reqId, contract, interval, type, false, arrayListOf())
            subscriptions[reqId] = Subscription(asset, interval)
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
            val subscription = subscriptions[reqId]
            if (subscription == null) {
                logger.warn("unexpected realtimeBar received with request id $reqId")
            } else {
                val v = volume?.value()?.toDouble() ?: Double.NaN
                val action = PriceBar(subscription.asset, open, high, low, close, v, subscription.interval.seconds)
                val now = Instant.ofEpochSecond(time) // IBKR uses second-resolution
                val event = Event(listOf(action), now)
                send(event)
            }
        }

    }

}

