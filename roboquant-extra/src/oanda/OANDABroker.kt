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
import com.oanda.v20.order.MarketOrderRequest
import com.oanda.v20.order.OrderCreateRequest
import com.oanda.v20.position.PositionSide
import com.oanda.v20.primitives.InstrumentName
import com.oanda.v20.transaction.OrderCancelReason
import com.oanda.v20.transaction.OrderFillTransaction
import com.oanda.v20.transaction.TransactionID
import org.roboquant.brokers.*
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant

/**
 * Implementation of the [Broker] interface that can be used for paper- en live-trading using OANDA as your broker.
 */
class OANDABroker(
    accountID: String? = null,
    token: String? = null,
    demoAccount: Boolean = true,
    val enableOrders: Boolean = true,
    private val maxLeverage: Double = 30.0 // Used to calculate buying power
) : Broker {

    private val ctx: Context = OANDA.getContext(token, demoAccount)
    private val accountID = AccountID(OANDA.getAccountID(accountID, ctx))

    private val _account = InternalAccount()

    override val account: Account
        get() = _account.toAccount()
    private val logger = Logging.getLogger(OANDABroker::class)
    private lateinit var lastTransactionId: TransactionID


    private val availableAssetsMap by lazy {
        OANDA.getAvailableAssets(ctx, this.accountID)
    }

    /**
     * Which assets are available to this account. For OANDA the region you are from determines which asset classes
     * you can trade.
     */
    val availableAssets
        get() = availableAssetsMap.values

    init {
        logger.info("Using account with id ${this.accountID}")
        if (! demoAccount) throw Exception("Currently only demo account usage is supported.")
        initAccount()
    }


    private fun getPosition(symbol: String, p: PositionSide): Position {
        val asset = availableAssetsMap[symbol]!!
        val qty = p.units.doubleValue()
        val avgPrice = p.averagePrice.doubleValue()
        val spotPrice = avgPrice + p.unrealizedPL.doubleValue() / qty
        return Position(asset, qty, avgPrice, spotPrice)
    }


    private fun updatePositions() {
        _account.portfolio.clear()
        val positions = ctx.position.listOpen(accountID).positions
        for (p in positions) {
            logger.fine { "Received position $p"}
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
        account.cash.clear()

        // Cash in roboquant is excluding the margin part
        val amount = Amount(account.baseCurrency, acc.balance.doubleValue())
        account.cash.set(amount)

        _account.buyingPower = Amount(account.baseCurrency,acc.marginAvailable.doubleValue() * maxLeverage)
        _account.lastUpdate = Instant.now()
        lastTransactionId = acc.lastTransactionID
        _account.portfolio.clear()
        updatePositions()
        logger.info {"Found ${_account.portfolio.values.size} existing positions in portfolio"}
    }

    /**
     * First time when connecting, update the state of roboquant account with broker account. Only cash and positions
     * are synced and it is assumed there are no open orders pending.
     */
    private fun updateAccount() {
        val acc = ctx.account.get(accountID).account


        // Cash in roboquant is excluding the margin part
        account.cash.clear()
        val amount = Amount(account.baseCurrency, acc.balance.doubleValue())
        account.cash.set(amount)

        _account.buyingPower = Amount(account.baseCurrency,acc.marginAvailable.doubleValue() * maxLeverage)
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
            trx.units.doubleValue(),
            trx.price.doubleValue(),
            trx.commission.doubleValue(),
            trx.pl.doubleValue(),
            order.id
        )
        _account.trades.add(trade)
        val amount = Amount(account.baseCurrency, trx.accountBalance.doubleValue())
        _account.cash.set(amount)
    }


    private fun createOrderRequest(order: MarketOrder): OrderCreateRequest {
        val req = OrderCreateRequest(accountID)
        val o = MarketOrderRequest()
        o.instrument = InstrumentName(order.asset.symbol)
        o.setUnits(order.quantity)
        req.setOrder(o)
        logger.fine { "Created OANDA order $o" }
        return req
    }

    /**
     * For now only market orders are supported, all other order types are rejected. Also orders will always TIF be
     * submitted with FOK (FillOrKill) and ignore the requested TiF.
     */
    override fun place(orders: List<Order>, event: Event): Account {
        logger.finer {"received ${orders.size} orders and ${event.actions.size} actions"}
        _account.putOrders(orders.initialOrderSlips)

       if (! enableOrders) {
            val states = orders.map { OrderState(it, OrderStatus.REJECTED, event.time, event.time) }
            _account.putOrders(states)
        } else {

            for (order in orders) {
                var state =  OrderState(order, OrderStatus.INITIAL, event.time)
                if (order is MarketOrder) {
                    if (order.tif !is FOK) logger.fine("Received order $order, using tif=FOK instead")
                    val req = createOrderRequest(order)
                    val resp = ctx.order.create(req)
                    logger.fine { "Received response with last transaction Id ${resp.lastTransactionID}" }
                    if (resp.orderFillTransaction != null) {
                        val trx = resp.orderFillTransaction
                        state = OrderState(order, OrderStatus.COMPLETED, event.time)
                        logger.fine { "Received transaction $trx" }
                        processTrade(order, trx)
                    } else if (resp.orderCancelTransaction != null){
                        val trx = resp.orderCancelTransaction

                        state = when(trx.reason) {
                            OrderCancelReason.TIME_IN_FORCE_EXPIRED -> OrderState(order, OrderStatus.EXPIRED, event.time, event.time)
                            else -> OrderState(order, OrderStatus.REJECTED, event.time, event.time)
                        }
                        logger.fine {"Received order cancellation for $order with reason ${trx.reason}"}
                    } else {
                        logger.warning {"No order cancel or fill was returned for $order"}
                    }
                } else {
                    logger.warning { "Rejecting unsupported order type $order" }
                    state =  OrderState(order, OrderStatus.REJECTED, event.time, event.time)
                }
                _account.orders[order.id] = state
            }
        }

        // OONDA doesn't update positions quick enough and so they don't reflect trades just made
        Thread.sleep(1000)
        updateAccount()
        updatePositions()

        // No need to return a copy of the account since is only modified during this method and not concurrently with
        // the strategy or policy execution
        return account
    }
}