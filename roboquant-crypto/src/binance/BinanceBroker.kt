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

@file:Suppress("unused")

package org.roboquant.binance

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.TimeInForce
import com.binance.api.client.domain.account.NewOrder.*
import com.binance.api.client.domain.account.NewOrderResponse
import com.binance.api.client.domain.account.request.CancelOrderRequest
import com.binance.api.client.domain.account.request.OrderRequest
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.InternalAccount
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.math.BigDecimal
import kotlin.math.absoluteValue

/**
 * Implementation of the broker interface for Binance exchange. This enables live trading of cryptocurrencies
 * on the Binance exchange. This broker only supports assets of the type Crypto.
 *
 * @constructor
 *
 */
class BinanceBroker(
    baseCurrencyCode: String = "USD",
    private val useMachineTime: Boolean = true,
    configure: BinanceConfig.() -> Unit = {}
) : Broker {

    private val client: BinanceApiRestClient
    private val _account = InternalAccount(Currency.getInstance(baseCurrencyCode))
    private val config = BinanceConfig()

    override val account: Account
        get() = _account.toAccount()


    private val logger = Logging.getLogger(BinanceBroker::class)
    private val placedOrders = mutableMapOf<Long, SingleOrder>()
    private var orderId = 0

    val assets by lazy {
        retrieveAssets()
    }

    init {
        config.configure()
        val factory = BinanceConnection.getFactory(config)
        client = factory.newRestClient()
        logger.info("Created BinanceBroker with client $client")
        updateAccount()
    }

    private fun retrieveAssets(): List<Asset> {
        return client.exchangeInfo.symbols.map {
            Asset(it.symbol, AssetType.CRYPTO, it.quoteAsset, "BINANCE")
        }
    }

    private fun updateAccount() {
        val balances = client.account.balances
        for (balance in balances) {
            logger.info { "${balance.asset} ${balance.free}" }
        }

        for (order in client.getOpenOrders(OrderRequest(""))) {
            val o = placedOrders[order.orderId]
            if (o !== null) {
                // o.fill = order.executedQty.toDouble()
            } else {
                logger.info("Received unknown order $order")
            }
        }
    }

    /**
     * @TODO test with a real account on BinanceBroker
     *
     * @param orders
     * @return
     */
    override fun place(orders: List<Order>, event: Event): Account {
        val slips = orders.map {
            OrderState(it, OrderStatus.REJECTED, event.time, event.time)
        }
        _account.putOrders(slips)
        true && return _account.toAccount()

        for (order in orders) {
            val asset = order.asset
            if (asset.type == AssetType.CRYPTO) {
                val symbol = binanceSymbol(asset)

                when (order) {
                    is CancelOrder -> cancelOrder(order)

                    is LimitOrder -> {
                        val newLimitOrder = trade(symbol, order)
                        placedOrders[newLimitOrder.orderId] = order
                    }
                    is MarketOrder -> {
                        val newMarketOrder = trade(symbol, order)
                        placedOrders[newMarketOrder.orderId] = order
                    }
                    else -> {
                        logger.warning { "BinanceBroker supports only cancellation, market and limit orders, received ${order::class} instead" }
                    }
                }

            } else {
                logger.warning { "BinanceBroker supports only CRYPTO assets, received ${asset.type} instead" }
            }
        }

        return account
    }

    private fun binanceSymbol(asset: Asset): String {
        return asset.symbol.uppercase()
    }

    /**
     * Cancel an order
     *
     * @param cancellation
     */
    private fun cancelOrder(cancellation: CancelOrder) {
        val c = cancellation.order.order
        // require(c.id.isNotEmpty()) { "Require non empty id when cancelling and order $c" }
        require(c.asset.type == AssetType.CRYPTO) { "BinanceBroker only support CRYPTO orders ${c.asset}" }
        val symbol = binanceSymbol(c.asset)
        val r = CancelOrderRequest(symbol, c.id.toString())
        client.cancelOrder(r)
    }

    /**
     * Place a limit order for a currency pair
     *
     * @param symbol
     * @param order
     */
    private fun trade(symbol: String, order: LimitOrder): NewOrderResponse {
        val amount = BigDecimal(order.quantity.absoluteValue).toBigInteger().toString()
        val price = order.limit.toString()
        val newOrder = if (order.buy)
            client.newOrder(limitBuy(symbol, TimeInForce.GTC, amount, price))
        else
            client.newOrder(limitSell(symbol, TimeInForce.GTC, amount, price))
        logger.info { "$newOrder" }
        return newOrder
    }

    /**
     * place a market order for a currency pair
     *
     * @param symbol
     * @param order
     */
    private fun trade(symbol: String, order: MarketOrder): NewOrderResponse {
        val amount = BigDecimal(order.quantity.absoluteValue).toBigInteger().toString()
        val newOrder = if (order.buy)
            client.newOrder(marketBuy(symbol, amount))
        else
            client.newOrder(marketSell(symbol, amount))
        logger.info { "$newOrder" }
        return newOrder
    }
}

