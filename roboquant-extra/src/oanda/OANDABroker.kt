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
import com.oanda.v20.transaction.OrderFillTransaction
import com.oanda.v20.transaction.TransactionID
import org.roboquant.brokers.*
import org.roboquant.common.Amount
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.FOK
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Implementation of the [Broker] API for OANDA that can be used for paper- en live-trading.
 */
class OANDABroker(
    accountID: String? = null,
    token: String? = null,
    demoAccount: Boolean = true,
    private val exchangeRates: ExchangeRates = OANDAExchangeRates(),
    val enableOrders: Boolean = false,
    private val maxLeverage: Double = 30.0
) : Broker {

    private val ctx: Context = OANDA.getContext(token, demoAccount)
    private val accountID = AccountID(OANDA.getAccountID(accountID, ctx))
    override val account: Account = Account(exchangeRates = exchangeRates)
    private val logger = Logging.getLogger("OANDABroker")
    private lateinit var lastTransactionId: TransactionID


    val availableAssets by lazy {
        OANDA.getAvailableAssets(ctx, this.accountID)
    }

    init {
        if (! demoAccount) logger.warning("Using real account, use at your own risk!!!")
        initAccount()
        logger.info("Retrieved account with id $accountID")
    }


    private fun getPosition(symbol: String, p: PositionSide): Position {
        val asset = availableAssets[symbol]!!
        val qty = p.units.doubleValue()
        val avgPrice = p.averagePrice.doubleValue()
        val spotPrice = avgPrice + p.unrealizedPL.doubleValue() / qty
        return Position(asset, qty, avgPrice, spotPrice)
    }


    private fun updatePositions() {
        account.portfolio.clear()
        val positions = ctx.position.listOpen(accountID).positions
        for (p in positions) {
            logger.fine { "Received position $p"}
            val symbol = p.instrument.toString()
            if (p.long.units.doubleValue() != 0.0) {
                val position = getPosition(symbol, p.long)
                account.portfolio.setPosition(position)
            }
            if (p.short.units.doubleValue() != 0.0) {
                // TODO does the short side use negative value for units
                val position = getPosition(symbol, p.short)
                account.portfolio.setPosition(position)
            }
        }
    }


    /**
     * First time when connecting, update the state of roboquant account with broker account. Only cash and positions
     * are synced and it is assumed there are no open orders pending.
     */
    private fun initAccount() {
        val acc = ctx.account.get(accountID).account
        account.baseCurrency = Currency.getInstance(acc.currency.toString())
        if (exchangeRates is OANDAExchangeRates) exchangeRates.baseCurrency = account.baseCurrency
        account.cash.clear()

        // Cash in roboquant is excluding the margin part
        val amount = Amount(account.baseCurrency, acc.balance.doubleValue())
        account.cash.set(amount)
        account.buyingPower = Amount(account.baseCurrency,acc.marginAvailable.doubleValue() * maxLeverage)
        account.time = Instant.now()
        lastTransactionId = acc.lastTransactionID
        account.portfolio.clear()
        updatePositions()
    }

    /**
     * First time when connecting, update the state of roboquant account with broker account. Only cash and positions
     * are synced and it is assumed there are no open orders pending.
     */
    private fun updateAccount() {
        val acc = ctx.account.get(accountID).account
        account.cash.clear()

        // Cash in roboquant is excluding the margin part
        val amount = Amount(account.baseCurrency, acc.balance.doubleValue())
        account.cash.set(amount)
        account.buyingPower = Amount(account.baseCurrency,acc.marginAvailable.doubleValue() * maxLeverage)
        account.time = Instant.now()
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
        account.trades.add(trade)
        val amount = Amount(account.baseCurrency, trx.accountBalance.doubleValue())
        account.cash.set(amount)
    }

    private fun updateExchangeRates(event: Event) {
        if (exchangeRates !is OANDAExchangeRates) return
        for (price in event.prices) {
            val asset = price.key
            if (asset.type == AssetType.FOREX) {
                exchangeRates.setRate(asset.symbol, price.value)
            }
        }
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
     * For now only market orders are supported, all other order types are rejected. Also TIF be submitted with the
     * FOK (FillOrKill) and ignore the requested TiF.
     */
    override fun place(orders: List<Order>, event: Event): Account {
        logger.finer {"received ${orders.size} orders and ${event.actions.size} actions"}
        updateExchangeRates(event)

        account.orders.addAll(orders)

        if (! enableOrders) {
            for (order in orders) order.status = OrderStatus.REJECTED
        } else {
            for (order in orders) {
                if (order is MarketOrder) {
                    if (order.tif !is FOK) logger.fine("Received ${order.tif}, using FOK instead")
                    val req = createOrderRequest(order)
                    val resp = ctx.order.create(req)
                    logger.fine { "Received response $resp" }
                    if (resp.orderFillTransaction != null) {
                        val trx = resp.orderFillTransaction
                        order.status = OrderStatus.COMPLETED
                        logger.fine { "Received transaction $trx" }
                        processTrade(order, trx)
                    } else if (resp.orderCancelTransaction != null){
                        val trx = resp.orderCancelTransaction
                        // In roboquant a TIF results in expired and not cancelled.
                        order.status = OrderStatus.EXPIRED
                        logger.fine {"Received order cancellation for $order $trx"}
                    } else {
                        logger.warning {"No cancel or fill was returned for $order"}
                    }
                } else {
                    logger.warning { "Rejecting unsupported order type $order" }
                    order.status = OrderStatus.REJECTED
                }
            }
        }

        // OONDA doesn't update positions quick enough and so don't reflect the trade yet.
        Thread.sleep(1000)
        updateAccount()
        updatePositions()
        // No need to create a copy since an account only modified during this method
        return account
    }
}