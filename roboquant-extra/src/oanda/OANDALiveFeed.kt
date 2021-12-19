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

import com.google.gson.JsonParser
import com.oanda.v20.Context
import com.oanda.v20.pricing.PricingGetRequest
import com.oanda.v20.primitives.DateTime
import kotlinx.coroutines.delay
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.roboquant.common.Asset
import org.roboquant.common.Background
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.OrderBook
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant


/**
 * Retrieve live  data from OANDA.
 */
class OANDALiveFeed(
    accountID: String? = null,
    token: String? = null,
    private val demoAccount: Boolean = true
) : LiveFeed() {

    private val ctx: Context = OANDA.getContext(token, demoAccount)
    private val assetMap = mutableMapOf<String, Asset>()
    private val accessToken = OANDA.getToken(token)
    private val accountID = OANDA.getAccountID(accountID, ctx)
    private val logger = Logging.getLogger(OANDALiveFeed::class)

    val availableAssets by lazy {
        OANDA.getAvailableAssets(ctx, this.accountID)
    }

    val assets
        get() = assetMap.values.toSortedSet()

    fun subscribePrices(vararg symbols: String) {
        val httpClient = HttpClientBuilder.create().build()

        symbols.forEach {
            assetMap[it] = availableAssets[it]!!
        }

        val domain = if (demoAccount) "https://stream-fxpractice.oanda.com/" else "https://stream-fxtrade.oanda.com/"
        val instruments = symbols.joinToString(",")
        val httpGet = HttpGet("${domain}v3/accounts/$accountID/pricing/stream?instruments=$instruments")
        httpGet.setHeader(BasicHeader("Authorization", "Bearer $accessToken"))
        logger.finer { "Executing request: ${httpGet.requestLine}" }
        val resp = httpClient.execute(httpGet)
        if (resp.statusLine.statusCode == 200 && resp.entity != null) {
            val job = Background.ioJob {
                try {
                    handleResponse(resp)
                } catch (e: Exception) {
                    logger.severe("$e")
                }
            }
            job.invokeOnCompletion { httpClient.close() }
        } else {
            val responseString = EntityUtils.toString(resp.entity, "UTF-8")
            logger.warning(responseString)
        }
        logger.fine { "subscribed to ${symbols.toList()}" }

    }

    /**
     * Subscribe to price events for the provides symbols. These events will be delivered at a maximum rate of once
     * per 250 milliseconds.
     *
     * TODO use an async HTTP client solution
     */
    private fun handleResponse(resp: CloseableHttpResponse) {

        val entity = resp.entity
        val stream = entity.content

        var line: String?
        val br = BufferedReader(InputStreamReader(stream))
        while (br.readLine().also { line = it } != null) {
            val tick = JsonParser.parseString(line).asJsonObject
            logger.finer { "received tick: $tick" }

            val type = tick.get("type").asString
            if (type == "PRICE") {
                val symbol = tick.get("instrument")?.asString
                val asset = availableAssets[symbol]!!
                val bids = tick.get("bids").asJsonArray.map {
                    val map = it.asJsonObject
                    OrderBook.OrderBookEntry(map.get("liquidity").asDouble, map.get("price").asDouble)
                }
                val asks = tick.get("asks").asJsonArray.map {
                    val map = it.asJsonObject
                    OrderBook.OrderBookEntry(map.get("liquidity").asDouble, map.get("price").asDouble)
                }
                val action = OrderBook(asset, asks, bids)
                val time = Instant.now() // parse(tick.get("time").asString)
                val event = Event(listOf(action), time)
                channel?.offer(event)
            }

        }

    }

    /**
     * Subscribe to the order book data for the provided [symbols]. Since this is a pulling solution, you can also
     * specify the [delay] interval between two pulls, default being 5000 milliseconds (so 5 seocond data)
     */
    fun subscribeOrderBook(vararg symbols: String, delay: Long = 5_000L) {

        symbols.forEach {
            val asset = availableAssets[it]
            if (asset != null)
                assetMap[it] = asset
            else
                logger.warning("No asset found for symbol $it. See broker.availableAssets for all available assets")
        }
        val job = Background.ioJob {
            var since: DateTime? = null
            while (true) {

                if (channel != null) {
                    val request = PricingGetRequest(accountID, symbols.toList())
                    if (since != null) request.setSince(since)
                    val resp = ctx.pricing[request]
                    val now = Instant.now()
                    val actions = resp.prices.map {
                        val asset = assetMap[it.instrument.toString()]!!
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
                    if (actions.isNotEmpty()) channel?.offer(Event(actions, now))
                    since = resp.time
                }
                delay(delay)
            }

        }
        logger.finer { "Started job $job" }
    }


}