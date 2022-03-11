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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.CurrentOrderStatus
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce
import net.jacobpeterson.alpaca.rest.AlpacaClientException
import org.roboquant.brokers.*
import org.roboquant.brokers.DefaultOrderState
import org.roboquant.brokers.initialOrderState
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant
import net.jacobpeterson.alpaca.model.endpoint.orders.Order as AlpacaOrder
import net.jacobpeterson.alpaca.model.endpoint.positions.Position as AlpacaPosition
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderStatus as AlpacaOrderStatus

/**
 * Broker implementation for Alpaca. This implementation allows using the Alpaca live- and paper-trading capabilities
 * in combination with roboquant.
 *
 * See also the Alpaca feed components if you want to use Alpaca also for retrieving market data.
 *
 * @constructor Create new Alpaca broker
 *
 * @sample org.roboquant.samples.alpacaBroker
 */
class AlpacaBroker(
    apiKey: String? = null,
    apiSecret: String? = null,
    accountType: AccountType = AccountType.PAPER,
    dataType: DataType = DataType.IEX
) : Broker {

    private val _account = InternalAccount()

    override val account: Account
        get() = _account.toAccount()

    private val alpacaAPI: AlpacaAPI = AlpacaConnection.getAPI(apiKey, apiSecret, accountType, dataType)
    private val logger = Logging.getLogger(AlpacaOrder::class)
    var enableTrading = false

    private val orderMapping = mutableMapOf<Order, AlpacaOrder>()

    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI)
    }

    private val assetsMap : Map<String, Asset> by lazy {
        availableAssets.associateBy { it.id }
    }

    init {
        updateAccount()
        updatePositions()
        loadInitialOrders()
        // updateOpenOrders()
    }

    /**
     * Update the roboquant account with the details from Alpaca account
     */
    private fun updateAccount() {
        try {
            val acc = alpacaAPI.account().get()

            _account.baseCurrency = Currency.getInstance(acc.currency)
            _account.buyingPower = Amount(account.baseCurrency, acc.buyingPower.toDouble())

            _account.cash.clear()
            val balance = Amount(account.baseCurrency, acc.cash.toDouble())
            _account.cash.deposit(balance)
            _account.lastUpdate = Instant.now()
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    /**
     * Update positions in the portfolio based on positions received from Alpaca.
     *
     */
    private fun updatePositions() {
        try {
            _account.portfolio.clear()
            for (openPosition in alpacaAPI.positions().get()) {
                logger.fine { "received $openPosition" }
                val p = convertPos(openPosition)
                _account.setPosition(p)
            }
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    /**
     * Load all the orders already linked to the account. This is only called once during initiatlization.
     */
    private fun loadInitialOrders() {
        try {
            for (order in alpacaAPI.orders().get(CurrentOrderStatus.ALL, null, null, null, null, false, null)) {
                logger.fine { "received $order" }
                _account.putOrders(listOf(toOrder(order)))
            }
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    private fun toAsset(assetId: String) = assetsMap[assetId]!!


    private fun toState(order: AlpacaOrder, marketOrder: MarketOrder) : DefaultOrderState {
        val status = when (order.status) {
            AlpacaOrderStatus.CANCELED -> OrderStatus.CANCELLED
            AlpacaOrderStatus.EXPIRED -> OrderStatus.EXPIRED
            AlpacaOrderStatus.FILLED -> OrderStatus.COMPLETED
            AlpacaOrderStatus.REJECTED -> OrderStatus.REJECTED
            else -> {
                logger.info { "Received UNSUPPORTED order status ${order.status}" }
                OrderStatus.ACCEPTED
            }
        }
        val close = order.filledAt ?: order.canceledAt ?: order.expiredAt
        val closeTime = close?.toInstant() ?: Instant.MAX
        return DefaultOrderState(marketOrder, status, order.createdAt.toInstant(), closeTime)
    }


    /**
     * Convert an alpaca order to a roboquant order
     */
    private fun toOrder(order: AlpacaOrder): DefaultOrderState {
        val asset = toAsset(order.assetId)
        val qty = if (order.side == OrderSide.BUY) order.quantity.toDouble() else - order.quantity.toDouble()
        val marketOrder = MarketOrder(asset, qty)
        return toState(order, marketOrder)
    }

    /**
     * Convert an Alpaca position to a roboquant position
     *
     * @param pos the Alpaca position
     * @return
     */
    private fun convertPos(pos: AlpacaPosition): Position {
        assert(pos.assetClass == "us_equity") { "Unsupported asset class found ${pos.assetClass} for position $pos" }
        val asset = toAsset(pos.assetId)
        return Position(asset, pos.quantity.toDouble(), pos.averageEntryPrice.toDouble(), pos.currentPrice.toDouble())
    }

    /**
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun updateOpenOrders() {

  /*      val slips = _account.orders.values.open.filterIsInstance<SingleOrder>().map {
            val aOrder = orderMapping[it]!!
            val order = alpacaAPI.orders().get(aOrder.id, false)
            val state = toState(order)
            OrderSlip(it, state)
        }
        _account.putOrders(slips)*/
    }

    /**
     * Place an order at Alpaca.
     *
     * @param order
     */
    private fun placeOrder(order: SingleOrder) {
        val asset = order.asset
        require(asset.type == AssetType.STOCK) { "Only stocks supported, received ${asset.type}" }
        require(asset.currencyCode == "USD") { "Only USD supported, received ${asset.currencyCode}" }

        val tif = when (order.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> {
                throw Exception("Unsupported TIF ${order.tif} for order $order")
            }
        }

        if (!enableTrading) {
            logger.info { "Trading not enabled, skipping $order" }
            return
        }

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL
        val qty = order.quantity.absInt

        val alpacaOrder = when (order) {
            is MarketOrder -> alpacaAPI.orders().requestMarketOrder(asset.symbol, qty, side, tif)
            is LimitOrder -> alpacaAPI.orders()
                .requestLimitOrder(asset.symbol, qty, side, tif, order.limit, false)
            else -> {
                throw Exception("Unsupported order type $order. Right now only Market and Limit orders are mapped")
            }
        }
        orderMapping[order] = alpacaOrder
        _account.putOrders(listOf(DefaultOrderState(order)))


    }

    /**
     * Place new instructions at this broker, the most common instruction being an order. After any processing,
     * it returns an instance of Account.
     *
     * Right now only Order type instructions are supported
     *
     * See also [Order]
     *
     * @param orders list of action to be placed at the broker
     * @return the updated account that reflects the latest state
     */
    override fun place(orders: List<Order>, event: Event): Account {
        updateOpenOrders()
        for (order in orders) {
            if (order is SingleOrder) {
                placeOrder(order)
                _account.putOrders(listOf(order).initialOrderState)
            } else {
                throw Exception("Unsupported order type $order")
            }
        }
        _account.lastUpdate = event.time
        return _account.toAccount()
    }
}
