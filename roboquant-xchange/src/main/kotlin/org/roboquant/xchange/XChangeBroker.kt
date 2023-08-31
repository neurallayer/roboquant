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

package org.roboquant.xchange

import org.knowm.xchange.Exchange
import org.knowm.xchange.currency.CurrencyPair
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.math.BigDecimal
import java.time.Instant
import org.knowm.xchange.dto.Order as CryptoOrder
import org.knowm.xchange.dto.trade.LimitOrder as CryptoLimitOrder
import org.knowm.xchange.dto.trade.MarketOrder as CryptoMarketOrder

/**
 * Generic cryptocurrency broker implementation for exchanges that are supported by the XChange package.
 *
 * XChange is a Java library providing a streamlined API for interacting with 60+ Bitcoin and Altcoin exchanges
 * See also https://knowm.org/open-source/xchange/ for a complete overview of the supported exchanges.
 *
 * @constructor
 *
 * @param exchange
 */
class XChangeBroker(exchange: Exchange, baseCurrencyCode: String = "USD") : Broker {

    private val _account = InternalAccount(Currency.getInstance(baseCurrencyCode))

    /**
     * @see Broker.account
     */
    override var account: Account = _account.toAccount()
        private set

    private val logger = Logging.getLogger(XChangeBroker::class)
    private val tradeService = exchange.tradeService
    private val accountService = exchange.accountService
    private val supportCurrencies = exchange.exchangeInstruments.map { CurrencyPair(it.base, it.counter) }
    private val placedOrders = mutableMapOf<String, SingleOrder>()
    private var orderId = 0

    init {
        logger.info("Created CryptoBroker for $exchange")
        sync()
    }

    /**
     * Update the account
     */
    private fun updateAccount() {
        val info = accountService.accountInfo
        for (wallet in info.wallets) {
            logger.info { "${wallet.key} ${wallet.value}" }
        }
        for (position in info.openPositions) {
            logger.info { "$position" }
        }
    }

    /**
     * @see Broker.sync
     */
    override fun sync(event: Event) {
        updateAccount()
        account = _account.toAccount()
    }

    /**
     * Place orders on a XChange supported exchange using the trade service.
     *
     * @param orders
     * @return
     */
    override fun place(orders: List<Order>, time: Instant) {
        if (time < Instant.now() - 1.hours) throw UnsupportedException("cannot place orders in the past")

        val now = Instant.now()
        for (order in orders.filterIsInstance<CreateOrder>()) {
            val asset = order.asset
            if (asset.type == AssetType.CRYPTO) {

                val currencyPair = CurrencyPair(asset.symbol, asset.currency.currencyCode)

                if (supportCurrencies.isNotEmpty() && currencyPair !in supportCurrencies) {
                    logger.warn { "Unsupported currency pair $currencyPair for exchange" }
                    return
                }
                val orderId = orderId++.toString()
                when (order) {
                    is LimitOrder -> {
                        trade(currencyPair, order, orderId)
                        placedOrders[orderId] = order
                        _account.acceptOrder(order, now)
                    }

                    is MarketOrder -> {
                        trade(currencyPair, order)
                        placedOrders[orderId] = order
                        _account.acceptOrder(order, now)
                    }

                    else -> {
                        logger.warn {
                            "only market and limit orders are supported, received $order instead"
                        }
                        _account.rejectOrder(order, now)
                    }
                }

            } else {
                logger.warn { "only CRYPTO assets are supported, received ${asset.type} instead" }
                _account.rejectOrder(order, now)
            }
        }

    }

    /**
     * Place a limit order for a currency pair
     *
     * @param currencyPair
     * @param order
     * @param orderId
     */
    private fun trade(currencyPair: CurrencyPair, order: LimitOrder, orderId: String) {
        val amount = order.size.absoluteValue.toBigDecimal()
        val orderType = if (order.buy) CryptoOrder.OrderType.BID else CryptoOrder.OrderType.ASK
        val limitPrice = BigDecimal(order.limit)
        val limitOrder = CryptoLimitOrder(orderType, amount, currencyPair, orderId, null, limitPrice)
        val returnValue = tradeService.placeLimitOrder(limitOrder)
        logger.debug { "Limit Order return value: $returnValue" }
    }

    /**
     * Place a market order for a currency pair
     *
     * @param currencyPair
     * @param order
     */
    private fun trade(currencyPair: CurrencyPair, order: MarketOrder) {
        val amount = order.size.absoluteValue.toBigDecimal()
        val orderType = if (order.buy) CryptoOrder.OrderType.BID else CryptoOrder.OrderType.ASK
        val marketOrder = CryptoMarketOrder(orderType, amount, currencyPair)
        val returnValue = tradeService.placeMarketOrder(marketOrder)
        logger.debug { "Market Order return value: $returnValue" }
    }

}