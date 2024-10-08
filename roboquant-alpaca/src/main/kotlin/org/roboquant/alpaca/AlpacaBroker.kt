/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.openapi.trader.model.AssetClass
import net.jacobpeterson.alpaca.openapi.trader.model.OrderSide
import net.jacobpeterson.alpaca.openapi.trader.model.OrderType
import net.jacobpeterson.alpaca.openapi.trader.model.OrderClass
import org.roboquant.brokers.*
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import net.jacobpeterson.alpaca.openapi.trader.model.Order as AlpacaOrder
import net.jacobpeterson.alpaca.openapi.trader.model.OrderStatus as AlpacaOrderStatus
import net.jacobpeterson.alpaca.openapi.trader.model.Position as AlpacaPosition


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


    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaBroker::class)
    private val orderPlacer: AlpaceOrderPlacer

    init {
        config.configure()

        if (config.accountType == AccountType.LIVE) {
            logger.warn { "Live accounts are not recommended, use at your own risk" }
        }

        alpacaAPI = Alpaca.getAPI(config)
        orderPlacer = AlpaceOrderPlacer(alpacaAPI, config.extendedHours)
        syncAccount()
        syncPositions()
        if (loadExistingOrders) loadExistingOrders()
    }


    private fun getAsset(symbol: String, assetClass: AssetClass?): Asset {
        val asset = when (assetClass) {
            AssetClass.US_EQUITY-> Stock(symbol)
            AssetClass.CRYPTO -> Crypto.fromSymbol(symbol)
            else -> throw RoboquantException("Unknown asset class=$assetClass")
        }
        return asset
    }

    /**
     * Sync the roboquant account with the current state from an Alpaca account. Alpaca state is always leading.
     */
    private fun syncAccount() {
        val acc = alpacaAPI.trader().accounts().account

        // Alpaca accounts are always in USD
        // _account.baseCurrency = Currency.getInstance(acc.currency)
        _account.buyingPower = Amount(_account.baseCurrency, acc.buyingPower!!.toDouble())

        _account.cash.clear()
        _account.cash.deposit(_account.baseCurrency, acc.cash!!.toDouble())
        _account.lastUpdate = Instant.now()
    }

    /**
     * Sync positions in the portfolio based on positions received from Alpaca.
     */
    private fun syncPositions() {
        _account.positions.clear()
        val positions = alpacaAPI.trader().positions().allOpenPositions
        for (openPosition in positions) {
            logger.debug { "received $openPosition" }
            val p = convertPos(openPosition)
            _account.setPosition(p)
        }
    }


    private fun updateIAccountOrder(rqOrder: Order, order: AlpacaOrder) {
        val status = toState(order)
        val time = if (status.open) {
            order.submittedAt ?: order.createdAt ?: ZonedDateTime.now().toOffsetDateTime()
        } else {
            order.filledAt ?: order.canceledAt ?: order.expiredAt ?: ZonedDateTime.now().toOffsetDateTime()
        }
        _account.updateOrder(rqOrder, time.toInstant(), status)
    }

    /**
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun syncOrders() {
        _account.orders.forEach {
            if (it.open) {
                // println("orderid=${it.order.id}")
                val orderId = UUID.fromString(it.id)
                val alpacaOrder = alpacaAPI.trader().orders().getOrderByOrderID(orderId, false)
                logger.debug { "open order id=$orderId alpaca-order=$alpacaOrder" }
                updateIAccountOrder(it, alpacaOrder)
            }
        }
    }

    /**
     * Load the open orders already at the Alpaca account when starting. This is only called once during initialization.
     * Closed orders will be ignored all together.
     */
    private fun loadExistingOrders() {
        val openOrders = alpacaAPI.trader().orders().getAllOrders(null, 500, null, null, null, false, "", "")
        for (order in openOrders) {
            logger.debug { "received open $order" }
            val rqOrder = toOrder(order)
            _account.orders.add(rqOrder)
        }
    }

    /**
     * Map Alpaca order states to roboquant order status
     */
    private fun toState(order: AlpacaOrder): OrderStatus {
        return when (order.status) {
            AlpacaOrderStatus.NEW -> OrderStatus.CREATED
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
     * Supports both regular Market Orders and Bracket Market Instruction
     */
    private fun toMarketOrder(order: AlpacaOrder): Order {
        val asset = getAsset(order.symbol, order.assetClass)
        val qty = if (order.side == OrderSide.BUY) order.qty!!.toBigDecimal() else -order.qty!!.toBigDecimal()
        val size = Size(qty)

        return when {
            order.type == OrderType.MARKET -> MarketOrder(asset, size)
            order.type == OrderType.LIMIT -> LimitOrder(asset, size, order.limitPrice!!.toDouble())
            order.orderClass == OrderClass.BRACKET -> BracketOrder(
                MarketOrder(asset, size),
                LimitOrder(asset, -size, order.limitPrice!!.toDouble()),
                StopOrder(asset, -size, order.stopPrice!!.toDouble())
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
        val qty = if (order.side == OrderSide.BUY) order.qty!!.toBigDecimal() else -order.qty!!.toBigDecimal()
        val rqOrder = when (order.type) {
            OrderType.MARKET -> toMarketOrder(order)
            OrderType.LIMIT -> LimitOrder(asset, Size(qty), order.limitPrice!!.toDouble())
            OrderType.STOP -> StopOrder(asset, Size(qty), order.stopPrice!!.toDouble())
            OrderType.STOP_LIMIT -> StopLimitOrder(
                asset,
                Size(qty),
                order.stopPrice!!.toDouble(),
                order.limitPrice!!.toDouble()
            )

            OrderType.TRAILING_STOP -> TrailOrder(asset, Size(qty), order.trailPercent!!.toDouble())
            else -> throw UnsupportedException("unsupported order type for order $order")
        }
        rqOrder.id = order.id ?: throw UnsupportedException("Unsupported order $order because no known id")
        return rqOrder
    }

    /**
     * Convert an Alpaca position to a roboquant position
     */
    private fun convertPos(pos: AlpacaPosition): Position {
        val asset = getAsset(pos.symbol, pos.assetClass)
        val size = Size(pos.qty)
        return Position(asset, size, pos.avgEntryPrice.toDouble(), pos.currentPrice.toDouble())
    }


    /**
     * @see Broker.sync
     */
    override fun sync(event: Event?): Account {
        if (event != null) {
            if (event.time < Instant.now() - 1.hours) throw UnsupportedException("cannot place orders in the past")
        }

        syncAccount()
        syncPositions()
        syncOrders()
        return _account.toAccount()
    }

    /**
     * Place new [instructions] at this broker. After any processing, this method returns an instance of Account.
     *
     * @return the updated account that reflects the latest state
     */
    override fun placeOrders(instructions: List<Instruction>) {

        for (order in instructions) {
            when (order) {
                is SingleOrder -> {
                    orderPlacer.placeSingleOrder(order)
                    _account.initializeOrders(listOf(order))
                }
                is Cancellation -> {
                    val orderId = UUID.fromString(order.orderId)
                    alpacaAPI.trader().orders().deleteOrderByOrderID(orderId)
                }
                else -> {
                    logger.warn { "ignoring unsupported order type order=$order" }
                }
            }
        }

    }
}
