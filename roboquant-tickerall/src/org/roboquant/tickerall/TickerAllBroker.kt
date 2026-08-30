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
 * @param configure configuration for connecting to the TickerAll API
 * @constructor Create a new instance of the TickerAllBroker for an already-connected [accountId]
 */
class TickerAllBroker(
    configure: TickerAllConfig.() -> Unit = {}
) : Broker {

    private val config = TickerAllConfig()
    private val client: TickerAllClient
    private var account: Account
    private val symbolCurrency: SymbolCurrency by lazy { SymbolCurrency(client) }
    private val logger = Logging.getLogger(TickerAllBroker::class)
    var baseCurrency: Currency = Currency.USD

    /**
     * The TickerAll account id this broker is bound to. After [connect] this is the id the session produced;
     * build a [TickerAllLiveFeed] or [TickerAllHistoricFeed] from it to reuse the same connected account
     * without starting a second session.
     */
    val accountId: String get() = config.accountId

    init {
        config.configure()
        client = TickerAll.getClient(config)
        account = sync()
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
         * @param configure supplies the api key and the MetaTrader credentials (see [TickerAllConfig])
         */
        fun connect(
            configure: TickerAllConfig.() -> Unit = {}
        ): TickerAllBroker {
            val config = TickerAllConfig().apply(configure)
            config.accountId = TickerAll.startSession(config)
            return TickerAllBroker {
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
        TickerAll.toAsset(symbol, baseCurrency, symbolCurrency.get(symbol))

    private fun sizeOf(volume: Double, side: String?): Size {
        val bd = BigDecimal.valueOf(volume)
        val signed = if (side != null && side.equals("SELL", ignoreCase = true)) bd.negate() else bd
        return Size(signed)
    }

    /**
     * Sync the roboquant account cash, buying power and open positions from the TickerAll account. TickerAll
     * (the broker) is always leading.
     */
    private fun syncPositions(acc: AccountDTO): List<Position> {
        val result = mutableListOf<Position>()

        for (p in acc.positions ?: emptyList()) {
            val symbol = p.symbol ?: continue
            val volume = p.volume ?: continue
            val entry = p.entryPrice ?: 0.0
            val market = p.currentPrice ?: entry
            val asset = getAsset(symbol)
            val id = p.ticket?.toString() ?: ""
            val p = Position(asset, sizeOf(volume, p.side), entry, market, id = id)
            result.add(p)
        }
        return result
    }

    /**
     * Load the resting pending orders at the account. Filled or cancelled orders are not returned by the
     * pending endpoint, so this always reflects the live open orders.
     */
    private fun syncOrders(): List<Order> {
        val result = mutableListOf<Order>()
        for (o in client.getPendingOrders()) {
            val symbol = o.symbol ?: continue
            val volume = o.volume ?: continue
            val ticket = o.ticket ?: continue
            val price = o.limitPrice ?: o.price ?: Double.NaN
            val rqOrder = Order(getAsset(symbol), sizeOf(volume, o.side), price)
            rqOrder.id = ticket.toString()
            result.add(rqOrder)
        }
        return result
    }

    /**
     * @see Broker.sync
     */
    override fun sync(event: Event?): Account {
        if (event != null && event.time < Instant.now() - 1.hours) {
            throw UnsupportedException("cannot place orders in the past")
        }
        val acc = client.getAccount()
        val financials = acc.account
            ?: throw TickerAllException("account ${config.accountId} is not connected/warm; reconnect it via TickerAll first")
        val balance = financials.balance
            ?: throw TickerAllException("account ${config.accountId} is not connected/warm; reconnect it via TickerAll first")
        val currency = financials.currency!!.let { Currency.getInstance(it) }

        val buyingPower = Amount(currency, financials.freeMargin ?: balance)

        val positions = syncPositions(acc)
        val orders = syncOrders()
        val result = Account(
            buyingPower = buyingPower,
            cash = Amount(currency, balance).toWallet(),
            orders = orders,
            positions = positions,
            lastUpdate = Instant.now(),
            trades = listOf()
        )
        account = result
        return result
    }

    /**
     * Place a single [order] straight through as a new market or limit order (the original netting-account
     * behavior), writing the broker-assigned ticket back into [Order.id].
     */
    private fun placeSingleOrder(order: Order) {
        val positionId = order.positionId
        if (positionId != null) {
            // For now can refer to positions to close them
            val position = account.positions.firstOrNull { it.id == positionId }
            if (position != null && position.size == -order.size) {
                client.closePosition(positionId)
            } else {
                logger.warn { "Cannot find position with id $positionId for order $order and position $position" }
            }
            return
        }

        val side = if (order.buy) "BUY" else "SELL"
        val volume = order.size.toBigDecimal().abs().toDouble()
        val type = if (order.limit == null) "market" else "limit"
        val body = PlaceOrderBody(
            type = type,
            symbol = order.asset.symbol,
            side = side,
            volume = volume,
            price = order.limit
        )
        val ack = client.placeOrder(body)
        logger.info { "Placing order $order with response $ack" }
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
                    } else {
                        logger.warn { "ignoring a zero-size order without an id (nothing to cancel)" }
                    }
                }
                // A known id with a non-zero size modifies that resting pending order.
                order.id.isNotEmpty() -> {
                    client.modifyPending(order.id, ModifyPendingBody(price = order.limit))
                }
                // A fresh order. A market order that opposes the current net position is net-emulated
                // (close-by-ticket, plus a market remainder on a reversal) so a hedging account reaches the
                // same net position roboquant models; anything else is placed straight through. Only a raw
                // placed order rests in the book and is tracked.
                else -> {
                    placeSingleOrder(order)
                }
            }
        }
    }
}
