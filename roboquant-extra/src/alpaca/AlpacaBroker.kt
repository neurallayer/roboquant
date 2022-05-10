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
import net.jacobpeterson.alpaca.model.endpoint.accountactivities.TradeActivity
import net.jacobpeterson.alpaca.model.endpoint.accountactivities.enums.ActivityType
import net.jacobpeterson.alpaca.model.endpoint.common.enums.SortDirection
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.CurrentOrderStatus
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce
import net.jacobpeterson.alpaca.rest.AlpacaClientException
import org.roboquant.brokers.*
import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import net.jacobpeterson.alpaca.model.endpoint.orders.Order as AlpacaOrder
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderStatus as AlpacaOrderStatus
import net.jacobpeterson.alpaca.model.endpoint.positions.Position as AlpacaPosition

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
    configure: AlpacaConfig.() -> Unit = {}
) : Broker {

    private val _account = InternalAccount()
    private val config = AlpacaConfig()

    override val account: Account
        get() = _account.toAccount()

    private val alpacaAPI: AlpacaAPI
    private val handledTrades = mutableSetOf<String>()
    private val logger = Logging.getLogger(AlpacaOrder::class)
    private val orderMapping = mutableMapOf<Order, AlpacaOrder>()

    // All available assets
    val availableAssets: SortedSet<Asset>

    // Stored the asset by the Alpaca assetId
    private val assetsMap: Map<String, Asset>

    init {
        config.configure()
        alpacaAPI = AlpacaConnection.getAPI(config)
        assetsMap = AlpacaConnection.getAvailableAssets(alpacaAPI)
        availableAssets = assetsMap.values.toSortedSet()
        syncAccount()
        syncPortfolio()
        loadInitialOrders()
        // updateOpenOrders()
    }

    /**
     * Sync the roboquant account with the details from Alpaca account
     */
    private fun syncAccount() {
        try {
            val acc = alpacaAPI.account().get()

            _account.baseCurrency = Currency.getInstance(acc.currency)
            _account.buyingPower = Amount(_account.baseCurrency, acc.buyingPower.toDouble())

            _account.cash.clear()
            _account.cash.set(_account.baseCurrency, acc.cash.toDouble())
            _account.lastUpdate = Instant.now()
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    /**
     * Sync positions in the portfolio based on positions received from Alpaca.
     *
     */
    private fun syncPortfolio() {
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
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun syncOrders() {

        val states = _account.openOrders.values.map {
            val aOrder = orderMapping.getValue(it.order)
            val order = alpacaAPI.orders().get(aOrder.id, false)
            toState(order, it.order)
        }
        _account.putOrders(states)
    }

    /**
     * Load the open orders already linked to the account. This is only called once during initiatlization.
     * Closed orders will be ignored.
     */
    private fun loadInitialOrders() {
        try {
            for (order in alpacaAPI.orders().get(CurrentOrderStatus.OPEN, null, null, null, null, false, null)) {
                logger.fine { "received open $order" }
                _account.putOrders(listOf(toOrder(order)))
            }
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    private fun toState(order: AlpacaOrder, roboquantOrder: Order): OrderState {
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
        return OrderState(roboquantOrder, status, order.createdAt.toInstant(), closeTime)
    }

    /**
     * Convert an alpaca order to a roboquant order
     */
    private fun toOrder(order: AlpacaOrder): OrderState {
        val asset = assetsMap[order.assetId]!!
        val qty = if (order.side == OrderSide.BUY) order.quantity.toBigDecimal() else -order.quantity.toBigDecimal()
        val marketOrder = MarketOrder(asset, Size(qty))
        return toState(order, marketOrder)
    }

    /**
     * Convert an Alpaca position to a roboquant position
     *
     * @param pos the Alpaca position
     * @return
     */
    private fun convertPos(pos: AlpacaPosition): Position {
        val asset = assetsMap.getValue(pos.assetId)
        val size = Size(pos.quantity)
        return Position(asset, size, pos.averageEntryPrice.toDouble(), pos.currentPrice.toDouble())
    }



    /**
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun syncTrades() {
        val now = ZonedDateTime.now()
        val trades = alpacaAPI.accountActivities().get(
            now, null, null, SortDirection.ASCENDING, 100, "", ActivityType.FILL)
        logger.fine { "Found ${trades.size} fill activities"}
        for (activity in trades.filterIsInstance<TradeActivity>()) {
            // Only add trades we know the order id of
            logger.fine { "Found trade $activity"}
            val order = orderMapping.filterValues { it.id == activity.orderId }.keys.firstOrNull()
            if (order != null) {
                if (activity.id !in handledTrades) {
                    val trade = Trade(
                        activity.transactionTime.toInstant(),
                        order.asset,
                        Size(activity.quantity.toBigDecimal()),
                        activity.price.toDouble(),
                        0.0,
                        Double.NaN,
                        order.id
                    )
                    _account.trades.add(trade)
                    handledTrades.add(activity.id)
                }
            }
        }
    }

    /**
     * Place an order at Alpaca.
     *
     * @param order
     */
    private fun placeOrder(order: SingleOrder) {
        val asset = order.asset
        require(asset.type in setOf(AssetType.STOCK, AssetType.CRYPTO)) {
            "Only stocks and crypto supported, received ${asset.type}"
        }

        val tif = when (order.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> {
                throw UnsupportedException("Unsupported TIF ${order.tif} for order $order")
            }
        }

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL
        val qty = order.size.toBigDecimal().abs().toInt()

        val alpacaOrder = when (order) {
            is MarketOrder -> alpacaAPI.orders().requestMarketOrder(asset.symbol, qty, side, tif)
            is LimitOrder -> alpacaAPI.orders()
                .requestLimitOrder(asset.symbol, qty, side, tif, order.limit, false)
            else -> {
                throw UnsupportedException(
                    "Unsupported order type $order. Right now only Market and Limit orders are mapped"
                )
            }
        }
        orderMapping[order] = alpacaOrder
        _account.putOrders(listOf(OrderState(order)))

    }

    /**
     * Place new [orders] at this broker. After any processing, this method returns an instance of Account.
     *
     * @return the updated account that reflects the latest state
     */
    override fun place(orders: List<Order>, event: Event): Account {
        syncOrders()
        syncTrades()
        for (order in orders) {
            if (order is SingleOrder) {
                try {
                    placeOrder(order)
                    _account.putOrder(OrderState(order))
                } catch (e: AlpacaClientException) {
                    logger.severe("couldn't place order=$order", e)
                    _account.putOrder(OrderState(order, status = OrderStatus.REJECTED))
                }
            } else {
                throw UnsupportedException("Unsupported order type $order")
            }
        }
        _account.lastUpdate = event.time
        return _account.toAccount()
    }
}
