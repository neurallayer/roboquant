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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.openapi.trader.model.AssetClass
import net.jacobpeterson.alpaca.openapi.trader.model.OrderSide
import net.jacobpeterson.alpaca.openapi.trader.model.OrderType
import org.roboquant.brokers.*
import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.common.Event
import java.time.Instant
import java.util.*
import net.jacobpeterson.alpaca.openapi.trader.model.Order as AlpacaOrder
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
    configure: AlpacaConfig.() -> Unit = {}
) : Broker {

    private val config = AlpacaConfig()
    var baseCurrency = Currency.USD
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaBroker::class)
    private val orderPlacer: AlpacaOrderPlacer

    init {
        config.configure()

        if (config.accountType == AccountType.LIVE) {
            logger.warn { "Live accounts are not recommended, use at your own risk" }
        }

        alpacaAPI = Alpaca.getAPI(config)
        orderPlacer = AlpacaOrderPlacer(alpacaAPI, config.extendedHours)
    }

    private fun getAsset(symbol: String, assetClass: AssetClass?): Asset {
        val asset = when (assetClass) {
            AssetClass.US_EQUITY -> Stock(symbol)
            AssetClass.CRYPTO -> Crypto.fromSymbol(symbol)
            else -> throw RoboquantException("Unknown asset class=$assetClass")
        }
        return asset
    }


    /**
     * Sync positions in the portfolio based on positions received from Alpaca.
     */
    private fun syncPositions(): List<Position> {
        val result = mutableListOf<Position>()
        val positions = alpacaAPI.trader().positions().allOpenPositions
        for (openPosition in positions) {
            logger.debug { "received $openPosition" }
            val p = convertPos(openPosition)
            result.add(p)
        }
        return result
    }


    /**
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun syncOrders(): List<Order> {
        val result = mutableListOf<Order>()
        val openOrders = alpacaAPI.trader().orders().getAllOrders("open", 500, null, null, null, false, "", "")
        for (order in openOrders) {
            logger.debug { "received open $order" }
            val rqOrder = toOrder(order)
            result.add(rqOrder)
        }
        return result
    }




    /**
     * Convert an alpaca order to a roboquant order.
     * This is only used during loading of existing orders at startup.
     */
    private fun toOrder(order: AlpacaOrder): Order {
        val asset = getAsset(order.symbol, order.assetClass)
        val qty = if (order.side == OrderSide.BUY) order.qty!!.toBigDecimal() else -order.qty!!.toBigDecimal()
        val rqOrder = when (order.type) {
            OrderType.LIMIT -> Order(asset, Size(qty), order.limitPrice!!.toDouble())
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

        val acc = alpacaAPI.trader().accounts().account

        // Alpaca accounts are always in USD
        // _account.baseCurrency = Currency.getInstance(acc.currency)
        val buyingPower = Amount(baseCurrency, acc.buyingPower!!.toDouble())
        val cash = Wallet()
        cash.deposit(baseCurrency, acc.cash!!.toDouble())
        val pos = syncPositions()
        val orders = syncOrders()
        return Account(
            buyingPower = buyingPower,
            cash = cash,
            lastUpdate = Instant.now(),
            orders = orders,
            positions = pos,
            trades = emptyList(),
        )
    }

    /**
     * Place new [orders] at this broker. After any processing, this method returns an instance of Account.
     *
     * @return the updated account that reflects the latest state
     */
    override fun placeOrders(orders: List<Order>) {

        for (order in orders) {
            when {
                order.isCancellation() -> {
                    val orderId = UUID.fromString(order.id)
                    alpacaAPI.trader().orders().deleteOrderByOrderID(orderId)
                }

                else -> {
                    orderPlacer.placeSingleOrder(order)
                }
            }
        }

    }
}
