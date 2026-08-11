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

import org.roboquant.brokers.Broker
import org.roboquant.brokers.InternalAccount
import org.roboquant.common.*
import java.math.BigDecimal
import java.time.Instant

/**
 * Broker implementation for TickerAll (https://tickerall.com), a hosted MetaTrader 4 and 5 API. This allows
 * roboquant to trade a MetaTrader broker account through TickerAll, without a local MetaTrader terminal and
 * from any operating system.
 *
 * Everything on TickerAll is keyed by a connected [accountId]. You get one in either of two ways:
 *
 *  1. **From your MetaTrader credentials — [TickerAllBroker.connect] (recommended).** This starts the broker
 *     session for you and returns a broker already bound to the resulting [accountId], so you go straight
 *     from credentials to a working broker:
 *     ```
 *     val broker = TickerAllBroker.connect {
 *         apiKey = "cf_api_..."            // or the TICKERALL_API_KEY env var
 *         broker = "mt5"                   // the MetaTrader platform, "mt4" or "mt5"
 *         server = "Exness-MT5Trial7"      // the broker/trade server name
 *         account = "12345678"             // your numeric broker login
 *         password = "..."                 // never stored beyond the session
 *         // terminalType = "CLIENT"       // optional; blank/omitted means MOBILE
 *     }
 *     val liveFeed = TickerAllLiveFeed { apiKey = "cf_api_..."; accountId = broker.accountId }
 *     ```
 *  2. **From an [accountId] you already have** (e.g. an account already connected on the TickerAll dashboard,
 *     or a [broker.accountId][accountId] from an earlier [connect]) — use the primary constructor:
 *     ```
 *     val broker = TickerAllBroker { apiKey = "cf_api_..."; accountId = "..." }
 *     ```
 *
 * The primary constructor assumes the account is **already connected/warm** on TickerAll; if it isn't, the
 * initial sync fails fast. Use [connect] when you only have MetaTrader credentials. See also
 * [TickerAllLiveFeed] and [TickerAllHistoricFeed] to use TickerAll for market data, building them from
 * [accountId] so the whole session is shared and started only once.
 *
 * Note that roboquant models one net position per asset, which maps to a MetaTrader **netting** account.
 * Hedging accounts (multiple open positions per symbol) are not represented one-to-one.
 *
 * @param loadExistingOrders load the resting pending orders already at the account on startup, default true
 * @param configure configuration for connecting to the TickerAll API
 * @constructor Create a new instance of the TickerAllBroker for an already-connected [accountId]
 */
class TickerAllBroker(
    loadExistingOrders: Boolean = true,
    configure: TickerAllConfig.() -> Unit = {}
) : Broker {

    private val config = TickerAllConfig()
    private val _account = InternalAccount(Currency.USD)
    private val client: TickerAllClient
    private val orderPlacer: TickerAllOrderPlacer
    private val logger = Logging.getLogger(TickerAllBroker::class)

    /**
     * The TickerAll account id this broker is bound to. After [connect] this is the id the session produced;
     * build a [TickerAllLiveFeed] or [TickerAllHistoricFeed] from it to reuse the same connected account
     * without starting a second session.
     */
    val accountId: String get() = config.accountId

    init {
        config.configure()
        client = TickerAll.getClient(config)
        orderPlacer = TickerAllOrderPlacer(client)
        syncAccountAndPositions()
        if (loadExistingOrders) loadExistingOrders()
    }

    companion object {
        /**
         * Connect a MetaTrader account from its credentials and return a [TickerAllBroker] bound to the
         * resulting account. This performs the session-start step (`POST /v1/sessions`) that TickerAll
         * requires before an account can be traded or streamed, so you do not have to run a separate connect
         * step yourself: supply `apiKey`, `broker` (`"mt4"` or `"mt5"`), `server`, `account` (the numeric
         * broker login) and `password` through [configure], and the returned broker is already synced and
         * ready. The new account id is available as [accountId] for building matching feeds.
         *
         * `terminalType` is optional (blank/omitted means MOBILE); `"WEB"` is not supported here as it needs
         * additional web fields. The credentials are used only to open the session and are not stored beyond
         * it.
         *
         * This is a plain one-shot session start. Unlike the official TickerAll SDKs' `keep_alive`, the raw
         * client cannot auto-re-arm a session that later goes cold (that re-arm loop is SDK-side machinery);
         * for a connection that must stay warm long-term, use a server-side always-hot account or reconnect
         * periodically.
         *
         * @param loadExistingOrders load the resting pending orders already at the account on startup
         * @param configure supplies the api key and the MetaTrader credentials (see [TickerAllConfig])
         */
        fun connect(
            loadExistingOrders: Boolean = true,
            configure: TickerAllConfig.() -> Unit = {}
        ): TickerAllBroker {
            val config = TickerAllConfig().apply(configure)
            config.accountId = TickerAll.startSession(config)
            return TickerAllBroker(loadExistingOrders) {
                apiKey = config.apiKey
                accountId = config.accountId
                baseUrl = config.baseUrl
                wsUrl = config.wsUrl
            }
        }
    }

    private fun getAsset(symbol: String): Asset = TickerAll.toAsset(symbol, _account.baseCurrency)

    private fun sizeOf(volume: Double, side: String?): Size {
        val bd = BigDecimal.valueOf(volume)
        val signed = if (side != null && side.equals("SELL", ignoreCase = true)) bd.negate() else bd
        return Size(signed)
    }

    /**
     * Sync the roboquant account cash, buying power and open positions from the TickerAll account. TickerAll
     * (the broker) is always leading.
     */
    private fun syncAccountAndPositions() {
        val acc = client.getAccount()
        // Financials are nested under `account`. A missing account block or balance means the account is not
        // connected/warm; a real 0.0 balance is a valid (unfunded) account state and must not be rejected.
        val financials = acc.account
            ?: throw TickerAllException("account ${config.accountId} is not connected/warm; reconnect it via TickerAll first")
        val balance = financials.balance
            ?: throw TickerAllException("account ${config.accountId} is not connected/warm; reconnect it via TickerAll first")
        val currency = financials.currency?.let { Currency.getInstance(it) } ?: _account.baseCurrency
        _account.baseCurrency = currency
        _account.buyingPower = Amount(currency, financials.freeMargin ?: balance)
        _account.cash.clear()
        _account.cash.deposit(currency, balance)

        _account.positions.clear()
        for (p in acc.positions ?: emptyList()) {
            val symbol = p.symbol ?: continue
            val volume = p.volume ?: continue
            val entry = p.entryPrice ?: 0.0
            val market = p.currentPrice ?: entry
            _account.setPosition(getAsset(symbol), Position(sizeOf(volume, p.side), entry, market))
        }
        _account.lastUpdate = Instant.now()
    }

    private fun syncOrders() = loadExistingOrders()

    /**
     * Load the resting pending orders at the account. Filled or cancelled orders are not returned by the
     * pending endpoint, so this always reflects the live open orders.
     */
    private fun loadExistingOrders() {
        _account.orders.clear()
        for (o in client.getPendingOrders()) {
            val symbol = o.symbol ?: continue
            val volume = o.volume ?: continue
            val ticket = o.ticket ?: continue
            val price = o.limitPrice ?: o.price ?: Double.NaN
            val rqOrder = Order(getAsset(symbol), sizeOf(volume, o.side), price)
            rqOrder.id = ticket.toString()
            _account.orders.add(rqOrder)
        }
    }

    /**
     * @see Broker.sync
     */
    override fun sync(event: Event?): Account {
        if (event != null && event.time < Instant.now() - 1.hours) {
            throw UnsupportedException("cannot place orders in the past")
        }
        syncAccountAndPositions()
        syncOrders()
        return _account.toAccount()
    }

    /**
     * Place, modify or cancel [orders] at TickerAll. Following roboquant's order model:
     * - a cancellation order (known id, zero size) cancels the pending order,
     * - an order with a known id and non-zero size modifies that pending order's price,
     * - an order without an id is placed as a new market or limit order.
     */
    override fun placeOrders(orders: List<Order>) {
        for (order in orders) {
            when {
                // A zero-size order is a cancellation (same detection as SimBroker); it must carry the id.
                order.size.iszero -> {
                    if (order.id.isNotEmpty()) {
                        client.cancelPending(order.id)
                        _account.orders.removeAll { it.id == order.id }
                    } else {
                        logger.warn { "ignoring a zero-size order without an id (nothing to cancel)" }
                    }
                }
                // A known id with a non-zero size modifies that resting pending order.
                order.id.isNotEmpty() -> {
                    val price = if (order.limit.isNaN()) null else order.limit
                    client.modifyPending(order.id, ModifyPendingBody(price = price))
                }
                // A fresh order is placed as a new market or limit order.
                else -> {
                    orderPlacer.placeSingleOrder(order)
                    _account.orders.add(order)
                }
            }
        }
    }
}
