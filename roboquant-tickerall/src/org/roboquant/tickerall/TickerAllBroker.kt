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
import java.math.MathContext
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
 * roboquant models one net position per asset. A MetaTrader **netting** account maps to that directly. A
 * **hedging** account (a separate ticket per trade) is aggregated into a single net position per asset on
 * sync, and a market order that opposes the current net is executed as a close-by-ticket (plus a market
 * remainder on a reversal) rather than opening an offsetting ticket — so either account type behaves as the
 * one-net-position-per-asset model roboquant expects.
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
    private val symbolCurrency: SymbolCurrency by lazy { SymbolCurrency(client) }
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

    // The asset currency is the instrument's quote currency from broker metadata (see SymbolCurrency), not
    // the account currency; the feeds resolve it the same way so a position and its price events match.
    private fun getAsset(symbol: String): Asset =
        TickerAll.toAsset(symbol, _account.baseCurrency, symbolCurrency.get(symbol))

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
        // Aggregate the broker's tickets into ONE net position per asset. A MetaTrader account may be HEDGING
        // (a separate ticket per trade) rather than netting; roboquant models one net position per asset, so
        // sum the tickets' signed volumes and take a volume-weighted average entry over the net side.
        // Otherwise same-symbol tickets would overwrite each other and only the last one would survive.
        val bySymbol = (acc.positions ?: emptyList())
            .filter { it.symbol != null && it.volume != null }
            .groupBy { it.symbol!! }
        for ((symbol, tickets) in bySymbol) {
            var net = BigDecimal.ZERO
            for (t in tickets) {
                val v = BigDecimal.valueOf(t.volume!!)
                net = if (t.side?.equals("SELL", ignoreCase = true) == true) net.subtract(v) else net.add(v)
            }
            if (net.signum() == 0) continue // fully hedged flat — no net exposure to report
            val netLong = net.signum() > 0
            // Volume-weighted average entry over the net-side tickets (the side matching the net sign).
            var num = BigDecimal.ZERO
            var den = BigDecimal.ZERO
            for (t in tickets) {
                val sell = t.side?.equals("SELL", ignoreCase = true) == true
                if (sell != netLong) {
                    val v = BigDecimal.valueOf(t.volume!!)
                    num = num.add(v.multiply(BigDecimal.valueOf(t.entryPrice ?: 0.0)))
                    den = den.add(v)
                }
            }
            val entry = if (den.signum() != 0) num.divide(den, MathContext.DECIMAL64).toDouble() else 0.0
            val market = tickets.first().currentPrice ?: entry
            _account.setPosition(getAsset(symbol), Position(Size(net), entry, market))
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
     * - an order without an id is placed as a new order; a market order that opposes the current net
     *   position is net-emulated as a close-by-ticket (plus a market remainder on a reversal), and anything
     *   else is a straight market or limit order.
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
                // A fresh order. A market order that opposes the current net position is net-emulated
                // (close-by-ticket, plus a market remainder on a reversal) so a hedging account reaches the
                // same net position roboquant models; anything else is placed straight through. Only a raw
                // placed order rests in the book and is tracked.
                else -> {
                    if (orderPlacer.place(order)) _account.orders.add(order)
                }
            }
        }
    }
}
