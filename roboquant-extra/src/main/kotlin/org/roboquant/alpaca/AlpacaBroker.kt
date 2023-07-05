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
import net.jacobpeterson.alpaca.model.endpoint.common.enums.SortDirection
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.*
import net.jacobpeterson.alpaca.rest.AlpacaClientException
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
 * @property extendedHours enable extended hours for trading, default is false
 * @param configure additional configuration parameters to connecting to the Alpaca API
 * @constructor Create a new instance of the AlpacaBroker
 */
class AlpacaBroker(
    private val extendedHours: Boolean = false,
    configure: AlpacaConfig.() -> Unit = {}
) : Broker {

    private val _account = InternalAccount(Currency.USD)
    private val config = AlpacaConfig()

    /**
     * @see Broker.account
     */
    override val account: Account
        get() = _account.toAccount()

    private val alpacaAPI: AlpacaAPI
    private val handledTrades = mutableSetOf<String>()
    private val logger = Logging.getLogger(AlpacaOrder::class)
    private val orderMapping = mutableMapOf<Order, String>()
    private val availableStocks: Map<String, Asset>
    private val availableCrypto: Map<String, Asset>

    init {
        config.configure()
        alpacaAPI = Alpaca.getAPI(config)
        availableStocks = Alpaca.getAvailableStocks(alpacaAPI)
        availableCrypto = Alpaca.getAvailableCrypto(alpacaAPI)
        syncAccount()
        syncPortfolio()
        loadInitialOrders()
    }

    /**
     * Get all available assets to trade (both stocks and cryptocurrencies)
     */
    val availableAssets: SortedSet<Asset>
        get() = (availableStocks.values + availableCrypto.values).toSortedSet()

    private fun getAsset(symbol: String) = availableStocks[symbol] ?: availableCrypto.getValue(symbol)

    /**
     * Sync the roboquant account with the current state from an Alpaca account. Alpaca state is always leading.
     */
    private fun syncAccount() {
        val acc = alpacaAPI.account().get()

        _account.baseCurrency = Currency.getInstance(acc.currency)
        _account.buyingPower = Amount(_account.baseCurrency, acc.buyingPower.toDouble())

        _account.cash.clear()
        _account.cash.set(_account.baseCurrency, acc.cash.toDouble())
        _account.lastUpdate = Instant.now()
    }

    /**
     * Sync positions in the portfolio based on positions received from Alpaca.
     */
    private fun syncPortfolio() {
        _account.portfolio.clear()
        val positions = alpacaAPI.positions().get()
        for (openPosition in positions) {
            logger.debug { "received $openPosition" }
            val p = convertPos(openPosition)
            _account.setPosition(p)
        }
    }


    private fun updateIAccountOrder(rqOrder: Order, order: AlpacaOrder) {
        orderMapping[rqOrder] = order.id
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
            val aOrderId = orderMapping[it.order]
            if (aOrderId != null) {
                val order = alpacaAPI.orders().get(aOrderId, false)
                updateIAccountOrder(it.order, order)
            } else {
                logger.warn("cannot find order ${it.order} in orderMap")
            }
        }
    }

    /**
     * Load the open orders already at the Alpaca account when starting. This is only called once during initialization.
     * Closed orders will be ignored all together.
     */
    private fun loadInitialOrders() {
        val openOrders = alpacaAPI.orders().get(CurrentOrderStatus.OPEN, null, null, null, null, false, null)
        for (order in openOrders) {
            logger.debug { "received open $order" }
            val rqOrder = toOrder(order)
            _account.initializeOrders(listOf(rqOrder))
            updateIAccountOrder(rqOrder, order)
        }
    }

    /**
     * Map Alpaca order states to roboquant order status
     */
    private fun toState(order: AlpacaOrder): OrderStatus {
        return when (order.status) {
            AlpacaOrderStatus.CANCELED -> OrderStatus.CANCELLED
            AlpacaOrderStatus.EXPIRED -> OrderStatus.EXPIRED
            AlpacaOrderStatus.FILLED -> OrderStatus.COMPLETED
            AlpacaOrderStatus.REJECTED -> OrderStatus.REJECTED
            AlpacaOrderStatus.ACCEPTED -> OrderStatus.ACCEPTED
            else -> {
                logger.info { "received unsupported order status ${order.status}" }
                OrderStatus.ACCEPTED
            }
        }
    }

    /**
     * Supports both regular Market Orders and Bracket Market Order
     */
    private fun toMarketOrder(order: AlpacaOrder): Order {
        val asset = getAsset(order.symbol)
        val qty = if (order.side == OrderSide.BUY) order.quantity.toBigDecimal() else -order.quantity.toBigDecimal()
        val size = Size(qty)

        if (order.orderClass == OrderClass.SIMPLE) return MarketOrder(asset, size)
        if (order.orderClass == OrderClass.BRACKET) return BracketOrder(
            MarketOrder(asset, size),
            LimitOrder(asset, -size, order.limitPrice.toDouble()),
            StopLimitOrder(asset, -size, order.stopPrice.toDouble(), order.limitPrice.toDouble())
        )

        throw UnsupportedException("unsupported order type for order $order")
    }

    /**
     * Convert an alpaca order to a roboquant order. This should only be called during loading of initial orders
     */
    private fun toOrder(order: AlpacaOrder): Order {
        val asset = getAsset(order.symbol)
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
        val asset = getAsset(pos.symbol)
        val size = Size(pos.quantity)
        return Position(asset, size, pos.averageEntryPrice.toDouble(), pos.currentPrice.toDouble())
    }

    /**
     * Sync the trades from the Alpaca account with the roboquant internal account
     */
    private fun syncTrades() {
        val now = ZonedDateTime.now()
        val trades = alpacaAPI.accountActivities().get(
            now, null, null, SortDirection.ASCENDING, 100, "", ActivityType.FILL
        )
        logger.debug { "Found ${trades.size} fill activities" }
        for (activity in trades.filterIsInstance<TradeActivity>()) {
            // Only add trades we know the order id of, ignore the rest
            logger.debug { "Found trade $activity" }
            val order = orderMapping.filterValues { it == activity.orderId }.keys.firstOrNull()
            if (order != null && order is CreateOrder && activity.id !in handledTrades) {
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

    /**
     * Sync the state of the Alpaca broker account with roboquant account state.
     */
    fun sync() {
        syncAccount()
        syncPortfolio()
        syncOrders()
        syncTrades()
    }

    /**
     * @see Broker.sync
     */
    override fun sync(event: Event) {
        sync()
    }

    /**
     * Cancel an order
     */
    private fun cancelOrder(cancellation: CancelOrder) {
        val now = Instant.now()
        try {
            val orderId = orderMapping[cancellation.order]
            alpacaAPI.orders().cancel(orderId)
            _account.updateOrder(cancellation, now, OrderStatus.COMPLETED)
        } catch (exception: AlpacaClientException) {
            _account.updateOrder(cancellation, now, OrderStatus.REJECTED)
            logger.trace(exception) { "cancellation failed for order=$cancellation" }
        }
    }


    private fun placeBracketOrder(order: BracketOrder) {

        val api = alpacaAPI.orders()
        val side = if (order.entry.buy) OrderSide.BUY else OrderSide.SELL
        val entry = order.entry as MarketOrder
        val tp = order.takeProfit as LimitOrder
        val sl = order.stopLoss as StopLimitOrder

        val tif = when (entry.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> throw UnsupportedException("unsupported tif=${entry.tif} for order=$order")
        }

        require(!entry.size.isFractional) { "fractional orders are not supported for barcket orders" }
        val qty = entry.size.toBigDecimal().abs().toInt()

        val alpacaOrder = api.requestMarketBracketOrder(
            entry.asset.symbol,
            qty,
            side,
            tif,
            tp.limit,
            sl.stop,
            sl.limit
        )

        orderMapping[order] = alpacaOrder.id

    }

    /**
     * Place an order of type [SingleOrder] at Alpaca.
     *
     * @param order
     */
    private fun placeOrder(order: SingleOrder) {
        val asset = order.asset
        require(asset.type in setOf(AssetType.STOCK, AssetType.CRYPTO)) {
            "only stocks and crypto supported, received ${asset.type}"
        }

        val tif = when (order.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> throw UnsupportedException("unsupported tif=${order.tif} for order=$order")
        }

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL
        require(order.size.isFractional && order !is LimitOrder) {
            "fractional orders only supported for limit orders"
        }

        val qty = order.size.toBigDecimal().abs()
        val api = alpacaAPI.orders()

        val alpacaOrder = when (order) {
            is MarketOrder -> api.requestMarketOrder(asset.symbol, qty.toInt(), side, tif)
            is LimitOrder -> api.requestLimitOrder(asset.symbol, qty.toDouble(), side, tif, order.limit, extendedHours)
            is StopOrder -> api.requestStopOrder(asset.symbol, qty.toInt(), side, tif, order.stop, extendedHours)
            is StopLimitOrder -> api.requestStopLimitOrder(
                asset.symbol, qty.toInt(), side, tif, order.limit, order.stop, extendedHours
            )

            is TrailOrder -> api.requestTrailingStopPercentOrder(
                asset.symbol, qty.toInt(), side, tif, order.trailPercentage, extendedHours
            )

            else -> throw UnsupportedException("unsupported single order type order=$order")
        }
        orderMapping[order] = alpacaOrder.id
    }

    /**
     * Place new [orders] at this broker. After any processing, this method returns an instance of Account.
     *
     * @return the updated account that reflects the latest state
     */
    override fun place(orders: List<Order>, time: Instant) {
        if (time < Instant.now() - 1.hours) throw UnsupportedException("cannot place orders in the past")

        _account.initializeOrders(orders)
        for (order in orders) {
            when (order) {
                is SingleOrder -> placeOrder(order)
                is CancelOrder -> cancelOrder(order)
                is BracketOrder -> placeBracketOrder(order)
                else -> throw UnsupportedException("unsupported order type order=$order")
            }
        }

    }
}
