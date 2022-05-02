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

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.account.AccountID
import com.oanda.v20.order.LimitOrderRequest
import com.oanda.v20.order.MarketOrderRequest
import com.oanda.v20.order.OrderCreateRequest
import com.oanda.v20.position.PositionSide
import com.oanda.v20.primitives.InstrumentName
import com.oanda.v20.transaction.OrderCancelReason
import com.oanda.v20.transaction.OrderFillTransaction
import com.oanda.v20.transaction.TransactionID
import org.roboquant.brokers.*
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant

/**
 * Implementation of the [Broker] interface that can be used for paper- en live-trading using OANDA as your broker.
 */
class OANDABroker(
    private val maxLeverage: Double = 30.0, // Used to calculate buying power
    configure: OANDAConfig.() -> Unit = {}
) : Broker {

    val config = OANDAConfig()
    private val ctx: Context
    private val accountID: AccountID

    private val _account = InternalAccount()

    override val account: Account
        get() = _account.toAccount()

    private val logger = Logging.getLogger(OANDABroker::class)
    private lateinit var lastTransactionId: TransactionID

    private val availableAssetsMap: Map<String, Asset>

    /**
     * Which assets are available to this account. For OANDA the region you are from determines which asset classes
     * you can trade.
     */
    val availableAssets
        get() = availableAssetsMap.values

    init {
        config.configure()
        if (!config.demo) throw UnsupportedException("Currently only demo account usage is supported.")
        ctx = OANDA.getContext(config)
        accountID = OANDA.getAccountID(config.account, ctx)
        availableAssetsMap = OANDA.getAvailableAssets(ctx, this.accountID)
        initAccount()
    }



    private fun getPosition(symbol: String, p: PositionSide): Position {
        val asset = availableAssetsMap[symbol]!!
        val qty = p.units.bigDecimalValue()
        val avgPrice = p.averagePrice.doubleValue()
        val spotPrice = avgPrice + p.unrealizedPL.doubleValue() / qty.toDouble()
        return Position(asset, Size(qty), avgPrice, spotPrice)
    }

    /**
     * Update portfolio
     *
     */
    private fun syncPortfolio() {
        _account.portfolio.clear()
        val positions = ctx.position.listOpen(accountID).positions
        for (p in positions) {
            logger.fine { "Received position $p" }
            val symbol = p.instrument.toString()
            if (p.long.units.doubleValue() != 0.0) {
                val position = getPosition(symbol, p.long)
                _account.setPosition(position)
            }
            if (p.short.units.doubleValue() != 0.0) {
                val position = getPosition(symbol, p.short)
                _account.setPosition(position)
            }
        }
    }

    /**
     * First time when connecting, update the state of roboquant account with broker account. Only cash and positions
     * are synced and it is assumed there are no open orders pending.
     */
    private fun initAccount() {
        val acc = ctx.account.get(accountID).account
        _account.baseCurrency = Currency.getInstance(acc.currency.toString())
        lastTransactionId = acc.lastTransactionID
        syncAccount()
        syncPortfolio()
        logger.info { "Found ${_account.portfolio.values.size} existing positions in portfolio" }
    }

    /**
     * Sync the internal roboquant account state with the account of OANDA
     */
    private fun syncAccount() {
        val acc = ctx.account.get(accountID).account

        // Cash in roboquant is excluding the margin part
        _account.cash.clear()
        val amount = Amount(_account.baseCurrency, acc.balance.doubleValue())
        _account.cash.set(amount)

        _account.buyingPower = Amount(_account.baseCurrency, acc.marginAvailable.doubleValue() * maxLeverage)
        _account.lastUpdate = Instant.now()
    }

    /**
     * Process a transaction/trade and update account accordingly
     */
    private fun processTrade(order: Order, trx: OrderFillTransaction) {
        val time = Instant.parse(trx.time)
        val trade = Trade(
            time,
            order.asset,
            Size(trx.units.bigDecimalValue()),
            trx.price.doubleValue(),
            trx.commission.doubleValue(),
            trx.pl.doubleValue(),
            order.id
        )
        _account.trades += trade
        val amount = Amount(_account.baseCurrency, trx.accountBalance.doubleValue())
        _account.cash.set(amount)
    }

    private fun createOrderRequest(order: MarketOrder): OrderCreateRequest {
        val req = OrderCreateRequest(accountID)
        val o = MarketOrderRequest()
        o.instrument = InstrumentName(order.asset.symbol)
        o.setUnits(order.size.toBigDecimal())
        req.setOrder(o)
        logger.fine { "Created OANDA order $o" }
        return req
    }

    private fun createOrderRequest(order: LimitOrder): OrderCreateRequest {
        val req = OrderCreateRequest(accountID)
        val o = LimitOrderRequest()
        o.instrument = InstrumentName(order.asset.symbol)
        o.setUnits(order.size.toBigDecimal())
        o.setPrice(order.limit)
        req.setOrder(o)
        logger.fine { "Created OANDA order $o" }
        return req
    }

    /**
     * For now only market- and limit-orders are supported, all other order types are rejected. Also orders will
     * always TIF be submitted with FOK (FillOrKill) and ignore the requested TiF.
     */
    override fun place(orders: List<Order>, event: Event): Account {
        logger.finer { "received ${orders.size} orders and ${event.actions.size} actions" }
        _account.putOrders(orders.initialOrderState)

        for (order in orders) {
            var state = OrderState(order, OrderStatus.INITIAL, event.time)
            val orderRequest = when (order) {
                is MarketOrder -> createOrderRequest(order)
                is LimitOrder -> createOrderRequest(order)
                else -> throw UnsupportedException("Unsupported order type $order")
            }

            val resp = ctx.order.create(orderRequest)
            logger.fine { "Received response with last transaction Id ${resp.lastTransactionID}" }
            if (resp.orderFillTransaction != null) {
                val trx = resp.orderFillTransaction
                state = OrderState(order, OrderStatus.COMPLETED, event.time, event.time)
                logger.fine { "Received transaction $trx" }
                processTrade(order, trx)
            } else if (resp.orderCancelTransaction != null) {
                val trx = resp.orderCancelTransaction

                state = when (trx.reason) {
                    OrderCancelReason.TIME_IN_FORCE_EXPIRED -> OrderState(
                        order,
                        OrderStatus.EXPIRED,
                        event.time,
                        event.time
                    )
                    else -> OrderState(order, OrderStatus.REJECTED, event.time, event.time)
                }
                logger.fine { "Received order cancellation for $order with reason ${trx.reason}" }
            } else {
                logger.warning { "No order cancel or fill was returned for $order" }
            }

            _account.putOrder(state)
        }

        // OONDA doesn't update positions quick enough and so they don't reflect trades just made.
        // so for now we put a sleep in here :(
        Thread.sleep(1000)
        syncAccount()
        syncPortfolio()
        return account
    }
}