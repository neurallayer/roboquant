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
import com.oanda.v20.primitives.InstrumentName
import com.oanda.v20.transaction.OrderFillTransaction
import org.roboquant.brokers.*
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.FOK
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant

class OANDABroker(
    accountID: String? = null,
    token: String? = null,
    demoAccount: Boolean = true,
    private val currencyConverter: CurrencyConverter = OANDACurrencyConverter(),
    val enableOrders: Boolean = false
) : Broker {

    private val ctx: Context = OANDA.getContext(token, demoAccount)
    override val account: Account = Account(currencyConverter = currencyConverter)
    private val logger = Logging.getLogger("OANDABroker")

    private val accountID = AccountID(OANDA.getAccountID(accountID, ctx))
    val availableAssets by lazy {
        OANDA.getAvailableAssets(ctx, this.accountID)
    }

    init {
        if (enableOrders) logger.warning("Ordering enabled, use at your own risk!!!")
        initAccount()
        logger.info("Retrieved account with id $accountID")
    }


    private fun updatePositions() {
        account.portfolio.clear()
        val positions = ctx.position.listOpen(accountID).positions
        for (p in positions) {
            val symbol = p.instrument.toString()
            val asset = availableAssets[symbol]!!
            if (p.long.units.doubleValue() != 0.0) {
                val pos = Position(asset, p.long.units.doubleValue(), p.long.averagePrice.doubleValue())
                account.portfolio.setPosition(pos)
            }
            if (p.short.units.doubleValue() != 0.0) {
                val pos = Position(asset, p.short.units.doubleValue(), p.short.averagePrice.doubleValue())
                account.portfolio.setPosition(pos)
            }
        }
    }

    /**
     * First time when connecting, update the state of roboquant account with broker account
     */
    private fun initAccount() {
        val summary = ctx.account.summary(accountID).account
        account.baseCurrency = Currency.getInstance(summary.currency.toString())
        if (currencyConverter is OANDACurrencyConverter) currencyConverter.baseCurrency = account.baseCurrency
        account.cash.set(account.baseCurrency, summary.balance.doubleValue())
        updatePositions()
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
        account.cash.set(account.baseCurrency, trx.accountBalance.doubleValue())
    }

    private fun updateExchangeRates(event: Event) {
        if (currencyConverter !is OANDACurrencyConverter) return
        for (price in event.prices) {
            val asset = price.key
            if (asset.type == AssetType.FOREX) {
                currencyConverter.setRate(asset.symbol, price.value)
            }
        }
    }


    /**
     * For now only market orders are supported, all other order types are rejected. Also it wil be placed with the
     * default TiF FillOrKill and ignore the requested TiF.
     */
    override fun place(orders: List<Order>, event: Event): Account {
        logger.finer {"received ${orders.size} orders and ${event.actions.size} actions"}
        updateExchangeRates(event)

        account.orders.addAll(orders)

        if (! enableOrders) {
            for (order in orders) order.status = OrderStatus.REJECTED
        } else {
            for (order in orders) {
                if (order is org.roboquant.orders.MarketOrder) {
                    if (order.tif !is FOK) logger.fine("Received ${order.tif}, using FOK instead (Fill or Kill)")
                    val req = OrderCreateRequest(accountID)
                    val o = MarketOrderRequest()
                    o.instrument = InstrumentName(order.asset.symbol)
                    o.setUnits(order.quantity)
                    req.setOrder(o)
                    val resp = ctx.order.create(req)
                    logger.fine { "Placed order $o" }
                    order.status = OrderStatus.COMPLETED
                    val trx = resp.orderFillTransaction
                    logger.fine { "Received transaction $trx" }
                    processTrade(order, trx)
                } else {
                    logger.warning { "Received unsupported order type $order" }
                    order.status = OrderStatus.REJECTED
                }
            }
        }

        Thread.sleep(1000)
        updatePositions()
        // No need to create a copy since an account only modified during this method
        return account
    }
}