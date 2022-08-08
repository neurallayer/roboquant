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
 * Retrieve live data from OANDA.
 */
class OANDALiveFeed(
    configure: OANDAConfig.() -> Unit = {}
) : LiveFeed(), AssetFeed {

    val config = OANDAConfig()
    private val ctx: Context
    private val accountID: AccountID
    private val assetMap = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger(OANDALiveFeed::class)
    private val jobs = ParallelJobs()

    init {
        config.configure()
        ctx = OANDA.getContext(config)
        accountID = OANDA.getAccountID(config.account, ctx)
    }

    val availableAssets by lazy {
        OANDA.getAvailableAssets(ctx, this.accountID)
    }

    override val assets
        get() = assetMap.values.toSortedSet()

    /**
     * Stop all background jobs
     */
    override fun close() {
        jobs.cancelAll()
    }

    fun subscribePriceBar(
        vararg symbols: String,
        granularity: String = "M1",
        priceType: String = "M",
        delay: Long = 60_000L,
    ) {
        val gr = CandlestickGranularity.valueOf(granularity)
        symbols.forEach { assetMap[it] = availableAssets[it]!! }
        val requests = symbols.map { InstrumentCandlesRequest(InstrumentName(it))
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
                    val asset = availableAssets[resp.instrument.toString()]!!
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
                            logger.fine {"Got price bar at ${now.truncatedTo(ChronoUnit.SECONDS)} for $action" }
                        }
                    }
                }
                val event = Event(actions, now)
                send(event)
                delay(delay)
            }
        }
    }

    fun subscribeOrderBook(vararg symbols: String, delay: Long = 5_000L) {
        val assets = symbols.map { Asset(it) }
        subscribeOrderBook(assets, delay = delay)
    }

    /**
     * Subscribe to the order book data for the provided [assets]. Since this is a pulling solution, you can also
     * specify the [delay] interval between two pulls, default being 5000 milliseconds (so 5 second data)
     */
    fun subscribeOrderBook(assets: Collection<Asset>, delay: Long = 5_000L) {
        logger.info { "Subscribing to ${assets.size} order books" }
        val symbols = assets.map { it.symbol }
        symbols.forEach {
            val asset = availableAssets[it]
            if (asset != null)
                assetMap[it] = asset
            else
                logger.warning("No asset found for symbol $it. See broker.availableAssets for all available assets")
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
                        val asset = assetMap.getValue(it.instrument.toString())
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
                    logger.fine("Got ${actions.size} order-book actions")
                    if (actions.isNotEmpty()) send(Event(actions, now))
                    since = resp.time
                }
                delay(delay)
            }

        }
    }

}