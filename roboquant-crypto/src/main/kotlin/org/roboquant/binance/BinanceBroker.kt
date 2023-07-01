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

@file:Suppress("unused", "WildcardImport")

package org.roboquant.binance

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.TimeInForce
import com.binance.api.client.domain.account.NewOrder.*
import com.binance.api.client.domain.account.NewOrderResponse
import com.binance.api.client.domain.account.request.CancelOrderRequest
import com.binance.api.client.domain.account.request.OrderRequest
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant
import com.binance.api.client.domain.OrderStatus as BinanceOrderStatus

/**
 * Implementation of the broker interface for the Binance exchange. This enables paper- and live-trading of
 * cryptocurrencies on the Binance exchange. This broker only supports assets of the type [AssetType.CRYPTO].
 *
 * @param baseCurrencyCode The base currency to use
 * @property useMachineTime
 * @param configure additional configure logic, default is to do nothing
 *
 * @constructor
 */
class BinanceBroker(
    baseCurrencyCode: String = "USD",
    private val useMachineTime: Boolean = true,
    configure: BinanceConfig.() -> Unit = {}
) : Broker {

    private val client: BinanceApiRestClient
    private val _account = InternalAccount(Currency.getInstance(baseCurrencyCode))
    private val config = BinanceConfig()

    /**
     * @see Broker.account
     */
    override val account: Account
        get() = _account.toAccount()

    private val logger = Logging.getLogger(BinanceBroker::class)
    private val placedOrders = mutableMapOf<Long, Int>()
    private var orderId = 0
    private val assetMap: Map<String, Asset>

    init {
        config.configure()
        val factory = Binance.getFactory(config)
        client = factory.newRestClient()
        logger.info("Created BinanceBroker with client $client")
        assetMap = Binance.retrieveAssets(client)
        updateAccount()
    }

    /**
     * Return all available assets to trade
     */
    val availableAssets
        get() = assetMap.values.toSortedSet()

    private fun updateAccount() {
        val balances = client.account.balances
        for (balance in balances) {
            logger.info { "${balance.asset} ${balance.free}" }
        }

        for (order in client.getOpenOrders(OrderRequest(""))) {
            val orderId = placedOrders[order.orderId] ?: continue
            val state = _account.getOrder(orderId) ?: continue

            when (order.status) {
                BinanceOrderStatus.FILLED ->
                    _account.updateOrder(state, Instant.now(), OrderStatus.COMPLETED)

                BinanceOrderStatus.CANCELED ->
                    _account.updateOrder(state, Instant.now(), OrderStatus.CANCELLED)

                BinanceOrderStatus.EXPIRED ->
                    _account.updateOrder(state, Instant.now(), OrderStatus.EXPIRED)

                BinanceOrderStatus.REJECTED ->
                    _account.updateOrder(state, Instant.now(), OrderStatus.REJECTED)

                else -> _account.updateOrder(state, Instant.now(), OrderStatus.ACCEPTED)
            }

        }
    }

    /**
     * @see Broker.getAccount
     */
    override fun getAccount(event: Event): Account {
        updateAccount()
        return account
    }

    /**
     * @param orders
     * @return
     */
    override fun place(orders: List<Order>) {
        _account.initializeOrders(orders)

        for (order in orders) {

            when (order) {
                is CancelOrder -> cancelOrder(order)

                is LimitOrder -> {
                    val symbol = order.asset.symbol
                    val newLimitOrder = trade(symbol, order)
                    placedOrders[newLimitOrder.orderId] = order.id
                }

                is MarketOrder -> {
                    val symbol = order.asset.symbol
                    val newMarketOrder = trade(symbol, order)
                    placedOrders[newMarketOrder.orderId] = order.id
                }

                else -> logger.warn {
                    "supports only cancellation, market and limit orders, received ${order::class} instead"
                }
            }

        }

    }

    /**
     * Cancel an order
     *
     * @param cancellation
     */
    private fun cancelOrder(cancellation: CancelOrder) {
        val c = cancellation.order
        val order = cancellation.order
        val r = CancelOrderRequest(order.asset.symbol, c.id.toString())
        client.cancelOrder(r)
    }

    /**
     * Place a limit order for a currency pair
     *
     * @param symbol
     * @param order
     */
    private fun trade(symbol: String, order: LimitOrder): NewOrderResponse {
        val amount = order.size.absoluteValue.toString()
        val price = order.limit.toString()
        val newOrder = if (order.buy)
            client.newOrder(limitBuy(symbol, TimeInForce.GTC, amount, price))
        else
            client.newOrder(limitSell(symbol, TimeInForce.GTC, amount, price))
        logger.info { "$newOrder" }
        return newOrder
    }

    /**
     * Place a market order for a currency pair
     *
     * @param symbol
     * @param order
     */
    private fun trade(symbol: String, order: MarketOrder): NewOrderResponse {
        val amount = order.size.absoluteValue.toString()
        val newOrder = if (order.buy)
            client.newOrder(marketBuy(symbol, amount))
        else
            client.newOrder(marketSell(symbol, amount))
        logger.info { "$newOrder" }
        return newOrder
    }
}

