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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.accountactivities.TradeActivity
import net.jacobpeterson.alpaca.model.endpoint.accountactivities.enums.ActivityType
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetClass
import net.jacobpeterson.alpaca.model.endpoint.common.enums.SortDirection
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.CurrentOrderStatus
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderClass
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderType
import org.roboquant.brokers.*
import org.roboquant.brokers.sim.execution.InternalAccount
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
 * Broker implementation for Alpaca. This implementation allows using the Alpaca live- and paper-trading accounts
 * in combination with roboquant.
 *
 * See also the Alpaca feed components if you want to use Alpaca also for retrieving market data.
 *
 * @param configure additional configuration parameters to connecting to the Alpaca API
 * @constructor Create a new instance of the AlpacaBroker
 */
class AlpacaBroker(
    loadExistingOrders: Boolean = true,
    configure: AlpacaConfig.() -> Unit = {}
) : Broker {

    private val _account = InternalAccount(Currency.USD)
    private val config = AlpacaConfig()

    /**
     * @see Broker.account
     */
    override var account: Account
        private set

    private val alpacaAPI: AlpacaAPI
    private val handledTrades = mutableSetOf<String>()
    private val logger = Logging.getLogger(AlpacaOrder::class)
    private val availableStocks: Map<String, Asset>
    private val availableCrypto: Map<String, Asset>
    private val orderPlacer: AlpaceOrderPlacer

    /**
     * Get all available assets to trade (both stocks and cryptocurrencies)
     */
    val availableAssets: SortedSet<Asset>

    init {
        config.configure()

        if (config.accountType == AccountType.LIVE) {
            logger.warn { "Live accounts are not recommended, use at your own risk" }
        }

        alpacaAPI = Alpaca.getAPI(config)
        orderPlacer = AlpaceOrderPlacer(alpacaAPI, config.extendedHours)
        availableStocks = Alpaca.getAvailableStocks(alpacaAPI)
        availableCrypto = Alpaca.getAvailableCrypto(alpacaAPI)
        availableAssets = (availableStocks.values + availableCrypto.values).toSortedSet()
        syncAccount()
        syncPositions()
        if (loadExistingOrders) loadExistingOrders()
        account = _account.toAccount()

    }


    private fun getAsset(symbol: String, assetClass: String): Asset {
        val type = when (assetClass) {
            AssetClass.US_EQUITY.value() -> AssetType.STOCK
            AssetClass.CRYPTO.value() -> AssetType.CRYPTO
            else -> throw RoboquantException("Unknown asset class=$assetClass")
        }
        return availableAssets.find { it.symbol == symbol && it.type == type }
            ?: throw RoboquantException("Unknown Symbol=$symbol")
    }

    /**
     * Sync the roboquant account with the current state from an Alpaca account. Alpaca state is always leading.
     */
    private fun syncAccount() {
        val acc = alpacaAPI.account().get()

        // Alpaca accounts are always in USD
        // _account.baseCurrency = Currency.getInstance(acc.currency)
        _account.buyingPower = Amount(_account.baseCurrency, acc.buyingPower.toDouble())

        _account.cash.clear()
        _account.cash.deposit(_account.baseCurrency, acc.cash.toDouble())
        _account.lastUpdate = Instant.now()
    }

    /**
     * Sync positions in the portfolio based on positions received from Alpaca.
     */
    private fun syncPositions() {
        _account.portfolio.clear()
        val positions = alpacaAPI.positions().get()
        for (openPosition in positions) {
            logger.debug { "received $openPosition" }
            val p = convertPos(openPosition)
            _account.setPosition(p)
        }
    }


    private fun updateIAccountOrder(rqOrder: Order, order: AlpacaOrder) {
        val status = toState(order)
        val time = if (status.open) {
            order.submittedAt ?: order.createdAt ?: ZonedDateTime.now()
        } else {
            order.filledAt ?: order.canceledAt ?: order.expiredAt ?: ZonedDateTime.now()
        }
        _account.updateOrder(rqOrder, time.toInstant(), status)
    }

    /**
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun syncOrders() {
        _account.orders.forEach {
            val aOrderId = orderPlacer.get(it.order)
            if (aOrderId != null) {
                val alpacaOrder = alpacaAPI.orders().get(aOrderId, false)
                updateIAccountOrder(it.order, alpacaOrder)
            } else {
                logger.warn("cannot find order ${it.order} in orderMap")
            }
        }
    }

    /**
     * Load the open orders already at the Alpaca account when starting. This is only called once during initialization.
     * Closed orders will be ignored all together.
     */
    private fun loadExistingOrders() {
        val openOrders = alpacaAPI.orders().get(CurrentOrderStatus.OPEN, null, null, null, null, false, null)
        for (order in openOrders) {
            logger.debug { "received open $order" }
            val rqOrder = toOrder(order)
            _account.initializeOrders(listOf(rqOrder))
            orderPlacer.addExistingOrder(rqOrder, order.id)
            updateIAccountOrder(rqOrder, order)
        }
    }

    /**
     * Map Alpaca order states to roboquant order status
     */
    private fun toState(order: AlpacaOrder): OrderStatus {
        return when (order.status) {
            AlpacaOrderStatus.NEW -> OrderStatus.INITIAL
            AlpacaOrderStatus.CANCELED -> OrderStatus.CANCELLED
            AlpacaOrderStatus.EXPIRED -> OrderStatus.EXPIRED
            AlpacaOrderStatus.FILLED -> OrderStatus.COMPLETED
            AlpacaOrderStatus.REJECTED -> OrderStatus.REJECTED
            AlpacaOrderStatus.ACCEPTED -> OrderStatus.ACCEPTED
            else -> {
                logger.warn { "received unsupported order status ${order.status}" }
                OrderStatus.ACCEPTED
            }
        }
    }

    /**
     * Supports both regular Market Orders and Bracket Market Order
     */
    private fun toMarketOrder(order: AlpacaOrder): Order {
        val asset = getAsset(order.symbol, order.assetClass)
        val qty = if (order.side == OrderSide.BUY) order.quantity.toBigDecimal() else -order.quantity.toBigDecimal()
        val size = Size(qty)

        return when {
            order.type == OrderType.MARKET -> MarketOrder(asset, size)
            order.type == OrderType.LIMIT -> LimitOrder(asset, size, order.limitPrice.toDouble())
            order.orderClass == OrderClass.BRACKET -> BracketOrder(
                MarketOrder(asset, size),
                LimitOrder(asset, -size, order.limitPrice.toDouble()),
                StopOrder(asset, -size, order.stopPrice.toDouble())
            )

            else -> throw UnsupportedException("unsupported order type for order $order")
        }
    }

    /**
     * Convert an alpaca order to a roboquant order.
     * This is only used during loading of existing orders at startup.
     */
    private fun toOrder(order: AlpacaOrder): Order {
        val asset = getAsset(order.symbol, order.assetClass)
        val qty = if (order.side == OrderSide.BUY) order.quantity.toBigDecimal() else -order.quantity.toBigDecimal()
        val rqOrder = when (order.type) {
            OrderType.MARKET -> toMarketOrder(order)
            OrderType.LIMIT -> LimitOrder(asset, Size(qty), order.limitPrice.toDouble())
            OrderType.STOP -> StopOrder(asset, Size(qty), order.stopPrice.toDouble())
            OrderType.STOP_LIMIT -> StopLimitOrder(
                asset,
                Size(qty),
                order.stopPrice.toDouble(),
                order.limitPrice.toDouble()
            )

            OrderType.TRAILING_STOP -> TrailOrder(asset, Size(qty), order.trailPercent.toDouble())
            else -> throw UnsupportedException("unsupported order type for order $order")
        }

        return rqOrder
    }

    /**
     * Convert an Alpaca position to a roboquant position
     */
    private fun convertPos(pos: AlpacaPosition): Position {
        val asset = getAsset(pos.symbol, pos.assetClass)
        val size = Size(pos.quantity)
        return Position(asset, size, pos.averageEntryPrice.toDouble(), pos.currentPrice.toDouble())
    }

    /**
     * Sync the trades from the Alpaca account with the roboquant internal account
     */
    private fun syncTrades() {
        val now = ZonedDateTime.now()
        val accountActivities = alpacaAPI.accountActivities().get(
            now, null, null, SortDirection.ASCENDING, 100, "", ActivityType.FILL
        )
        logger.debug { "Found ${accountActivities.size} FILL account activities" }
        for (activity in accountActivities.filterIsInstance<TradeActivity>()) {
            // Only add trades we know the order id of, ignore the rest
            logger.debug { "Found trade $activity" }
            val order = orderPlacer.findByAlapacaId(activity.orderId)
            if (order == null) {
                logger.warn { "Couldn't find order for trade=$activity" }
                continue
            }
            if (order is CreateOrder && activity.id !in handledTrades) {
                val trade = Trade(
                    activity.transactionTime.toInstant(),
                    order.asset,
                    Size(activity.quantity.toBigDecimal()),
                    activity.price.toDouble(),
                    0.0,
                    Double.NaN,
                    order.id
                )
                _account.addTrade(trade)
                handledTrades.add(activity.id)
            }
        }
    }

    /**
     * @see Broker.sync
     */
    override fun sync(event: Event) {
        syncAccount()
        syncPositions()
        syncOrders()
        syncTrades()
        account = _account.toAccount()
    }

    /**
     * Place new [orders] at this broker. After any processing, this method returns an instance of Account.
     *
     * @return the updated account that reflects the latest state
     */
    override fun place(orders: List<Order>, time: Instant) {
        val now = Instant.now()
        // Sanity-check that you don't use this broker during back testing.
        if (time < now - 1.hours) throw UnsupportedException("cannot place orders in the past")

        _account.initializeOrders(orders)
        for (order in orders) {
            when (order) {
                is SingleOrder -> orderPlacer.placeSingleOrder(order)
                is CancelOrder -> {
                    val sucess = orderPlacer.cancelOrder(order)
                    val status = if (sucess) OrderStatus.COMPLETED else OrderStatus.REJECTED
                    _account.updateOrder(order, now, status)
                }

                is BracketOrder -> orderPlacer.placeBracketOrder(order)
                else -> {
                    logger.warn { "unsupported order type order=$order" }
                    _account.updateOrder(order, now, OrderStatus.REJECTED)
                }
            }
        }

    }
}
