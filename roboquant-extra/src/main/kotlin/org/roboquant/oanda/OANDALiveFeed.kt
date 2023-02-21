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

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.account.AccountID
import com.oanda.v20.instrument.CandlestickGranularity
import com.oanda.v20.instrument.InstrumentCandlesRequest
import com.oanda.v20.pricing.PricingGetRequest
import com.oanda.v20.primitives.DateTime
import com.oanda.v20.primitives.InstrumentName
import kotlinx.coroutines.delay
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.ParallelJobs
import org.roboquant.feeds.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.collections.set

/**
 * Retrieve live market data from OANDA.
 *
 * @param configure additional configuration
 */
class OANDALiveFeed(
    configure: OANDAConfig.() -> Unit = {}
) : LiveFeed(), AssetFeed {

    private val config = OANDAConfig()
    private val ctx: Context
    private val accountID: AccountID
    private val subscriptions = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger(OANDALiveFeed::class)
    private val jobs = ParallelJobs()

    init {
        config.configure()
        ctx = OANDA.getContext(config)
        accountID = OANDA.getAccountID(config.account, ctx)
    }

    private val availableAssetsMap by lazy {
        OANDA.getAvailableAssets(ctx, this.accountID)
    }

    /**
     * Return all available assets to subscribe to
     */
    val availableAssets
        get() = availableAssetsMap.values

    /**
     * Return all subscribed assets
     */
    override val assets
        get() = subscriptions.values.toSortedSet()

    /**
     * Stop all background jobs
     */
    override fun close() {
        jobs.cancelAll()
    }

    /**
     * Subscribe to price bars using a polling mechanism.
     */
    fun subscribePriceBar(
        vararg symbols: String,
        granularity: String = "M1",
        priceType: String = "M",
        delay: Long = 60_000L,
    ) {
        val gr = CandlestickGranularity.valueOf(granularity)
        symbols.forEach {
            val asset = availableAssetsMap[it]
            require(asset != null) { "invalid symbol=$it" }
            subscriptions[it] = asset
        }
        val requests = symbols.map {
            InstrumentCandlesRequest(InstrumentName(it))
                .setPrice(priceType)
                .setGranularity(gr)
                .setCount(1)
        }

        jobs.add {
            while (true) {
                val now = Instant.now()
                val actions = mutableListOf<Action>()
                for (request in requests) {
                    val resp = ctx.instrument.candles(request)
                    val asset = availableAssetsMap[resp.instrument.toString()]!!
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
                            actions.add(action)
                            logger.debug { "Got price bar at ${now.truncatedTo(ChronoUnit.SECONDS)} for $action" }
                        }
                    }
                }
                val event = Event(actions, now)
                send(event)
                delay(delay)
            }
        }
    }


    /**
     * Subscribe to the order book data for the provided [assets]. Since this is a pulling solution, you can also
     * specify the [delay] interval between two pulls, default being 5000 milliseconds (so 5 second data)
     */
    fun subscribeOrderBook(vararg symbols: String, delay: Long = 5_000L) {
        logger.info { "Subscribing to order books for ${symbols.size} symbols" }
        symbols.forEach {
            val asset = availableAssetsMap[it]
            require(asset != null) { "No available asset found for symbol $it" }
            subscriptions[it] = asset
        }
        jobs.add {
            var since: DateTime? = null
            while (true) {
                if (isActive) {
                    val request = PricingGetRequest(accountID, symbols.toList())
                    if (since != null) request.setSince(since)
                    val resp = ctx.pricing[request]
                    val now = Instant.now()
                    val actions = resp.prices.map {
                        val asset = subscriptions.getValue(it.instrument.toString())
                        OrderBook(
                            asset,
                            it.asks.map { entry ->
                                OrderBook.OrderBookEntry(
                                    entry.liquidity.toDouble(),
                                    entry.price.doubleValue()
                                )
                            },
                            it.bids.map { entry ->
                                OrderBook.OrderBookEntry(
                                    entry.liquidity.toDouble(),
                                    entry.price.doubleValue()
                                )
                            }
                        )
                    }
                    logger.debug { "actions=${actions.size} type=order-book time=$now" }
                    if (actions.isNotEmpty()) send(Event(actions, now))
                    since = resp.time
                }
                delay(delay)
            }

        }
    }

}