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
@file:Suppress("WildcardImport", "MaxLineLength")

package org.roboquant.ibkr

import com.ib.client.*
import com.ib.client.Types.Action
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.Position
import org.roboquant.brokers.Trade
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.ibkr.IBKR.toAsset
import org.roboquant.ibkr.IBKR.toContract
import org.roboquant.orders.*
import org.roboquant.orders.OrderStatus
import java.time.Instant
import com.ib.client.Order as IBOrder
import com.ib.client.OrderState as IBOrderSate
import com.ib.client.OrderStatus as IBOrderStatus


/**
 * Used to store partial account updates till completed
 */
private class AccountUpdate {

    val cash = Wallet()
    val positions = mutableListOf<Position>()
    var base = Currency.USD
    var buyingPower = Double.NaN

    fun update(account: InternalAccount) {
        account.lastUpdate = Instant.now()
        account.baseCurrency = base
        account.buyingPower = Amount(base, buyingPower)

        // Fill the cash
        account.cash.clear()
        for (c in cash.currencies) account.cash.set(c, cash[c])

        // Fill the positions
        account.portfolio.clear()
        for (p in positions) account.setPosition(p)
    }

}

/**
 * Use your Interactive Brokers account for trading. Can be used with live trading or paper trading accounts of
 * Interactive Brokers. It is highly recommended to start with a paper trading account and validate your strategy and
 * policy extensively before moving to live trading.
 *
 * ## Use at your own risk, since there are no guarantees about the correct functioning of the roboquant software.
 *
 * @param configure additional configuration
 * @constructor
 */
class IBKRBroker(
    configure: IBKRConfig.() -> Unit = {}
) : Broker {

    private val config = IBKRConfig()

    private val accountId: String?
    private var client: EClientSocket
    private var _account = InternalAccount(Currency.USD)
    private var accountUpdate = AccountUpdate()
    private var accountUpdateLock = Object()

    /**
     * @see Broker.account
     */
    override val account: Account
        get() = _account.toAccount()

    private val logger = Logging.getLogger(IBKRBroker::class)
    private var orderId = 0

    // Track IB orders ids with roboquant orders
    private val orderMap = mutableMapOf<Int, Order>()

    // Track IB Trades and Feed ids with roboquant trades
    private val tradeMap = mutableMapOf<String, Trade>()

    init {
        config.configure()
        require(config.account.isBlank() || config.account.startsWith('D')) { "only paper trading is supported" }
        accountId = config.account.ifBlank { null }
        logger.info { "using account=$accountId" }
        val wrapper = Wrapper(logger)
        client = IBKR.connect(wrapper, config)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, accountId)
        // client.reqAccountSummary(9004, "All", "\$LEDGER:ALL")

        // Open orders already send as part of connection
        // Only request orders created with this client, so don't use: client.reqAllOpenOrders()
        // client.reqOpenOrders()
        updateAccount()
    }


    private fun updateAccount() {
        client.reqAccountUpdates(true, accountId)
        synchronized(accountUpdateLock) {
            accountUpdateLock.wait(IBKR.MAX_RESPONSE_TIME)
        }
    }

    /**
     * Disconnect roboquant from TWS or IB Gateway
     */
    fun disconnect() = IBKR.disconnect(client)


    private fun IBOrder.log(contract: Contract) {
        logger.info {
            "placing order id=${orderId()} size=${totalQuantity()} type=${orderType()} contract=$contract"
        }
    }

    /**
     * Cancel an order
     */
    private fun cancelOrder(cancellation: CancelOrder) {
        val id = cancellation.id
        val ibID = orderMap.filter { it.value.id == id }.keys.first()
        logger.info("cancelling order with id $ibID")
        client.cancelOrder(ibID, cancellation.tag)

        // There is no easy way to check for the status of a cancellation order.
        // So for we set it always to status completed.
        val now = Instant.now()
        _account.completeOrder(cancellation, now)
    }

    /**
     * Place an order of type [SingleOrder]
     */
    private fun placeSingleOrder(order: SingleOrder) {
        val contract = order.asset.toContract()
        val ibOrder = createIBOrder(order)
        ibOrder.log(contract)
        client.placeOrder(ibOrder.orderId(), contract, ibOrder)
    }

    override fun sync(event: Event) {
        // For IBKR, the account update happens asynchronously in the background.
        // NOP
    }

    /**
     * Place zero or more [orders]
     *
     * @param orders
     */
    override fun place(orders: List<Order>, time: Instant) {
        if (time < Instant.now() - 1.hours) throw UnsupportedException("cannot place orders in the past")

        // Make sure we store all orders
        _account.initializeOrders(orders)

        for (order in orders) {
            logger.info("received order=$order")
            when (order) {
                is CancelOrder -> cancelOrder(order)
                is SingleOrder -> placeSingleOrder(order)
                is BracketOrder -> placeBracketOrder(order)
                else -> {
                    throw UnsupportedException("unsupported order type order=$order")
                }
            }

        }
    }

    /**
     * convert a roboquant [order] to an IBKR order.
     */
    private fun createIBOrder(order: SingleOrder): IBOrder {
        val result = IBOrder()
        with(result) {
            when (order) {
                is MarketOrder -> orderType(OrderType.MKT)
                is LimitOrder -> {
                    orderType(OrderType.LMT); lmtPrice(order.limit)
                }

                is StopOrder -> {
                    orderType(OrderType.STP); auxPrice(order.stop)
                }

                is StopLimitOrder -> {
                    orderType(OrderType.STP_LMT); lmtPrice(order.limit); auxPrice(order.stop)
                }

                is TrailOrder -> {
                    orderType(OrderType.TRAIL); trailingPercent(order.trailPercentage)
                }

                else -> throw UnsupportedException("unsupported order type $order")
            }
        }

        val action = if (order.buy) Action.BUY else Action.SELL
        result.action(action)
        val qty = Decimal.get(order.size.toBigDecimal().abs())
        result.totalQuantity(qty)

        if (accountId != null) result.account(accountId)
        result.orderId(++orderId)
        orderMap[orderId] = order

        return result
    }


    private fun placeBracketOrder(order: BracketOrder) {
        val entry = createIBOrder(order.entry)
        val profit = createIBOrder(order.takeProfit)
        profit.parentId(entry.orderId())
        val loss = createIBOrder(order.stopLoss)
        loss.parentId(entry.orderId())
        loss.transmit(true)
        val orders = listOf(entry, profit, loss)
        val contract = order.entry.asset.toContract()

        for (o in orders) {
            o.log(contract)
            client.placeOrder(o.orderId(), contract, o)
        }
    }

    /**
     * Overwrite the default wrapper
     */
    private inner class Wrapper(logger: Logging.Logger) : BaseWrapper(logger) {

        /**
         * Convert an IBOrder to a roboquant Order. This is only used during initial connect when retrieving any open
         * orders linked to the account.
         */
        private fun toOrder(order: IBOrder, contract: Contract): Order {
            val asset = contract.toAsset()
            val qty = if (order.action() == Action.BUY) order.totalQuantity() else order.totalQuantity().negate()
            val size = Size(qty.value())
            return when (order.orderType()) {
                OrderType.MKT -> MarketOrder(asset, size)
                OrderType.LMT -> LimitOrder(asset, size, order.lmtPrice())
                OrderType.STP -> StopOrder(asset, size, order.auxPrice())
                OrderType.TRAIL -> TrailOrder(asset, size, order.trailingPercent())
                OrderType.STP_LMT -> StopLimitOrder(asset, size, order.auxPrice(), order.lmtPrice())
                else -> throw UnsupportedException("$order")
            }
        }

        private fun toStatus(status: String): OrderStatus {
            return when (IBOrderStatus.valueOf(status)) {
                IBOrderStatus.Submitted -> OrderStatus.ACCEPTED
                IBOrderStatus.Cancelled -> OrderStatus.CANCELLED
                IBOrderStatus.Filled -> OrderStatus.COMPLETED
                else -> OrderStatus.INITIAL
            }
        }

        /**
         * What is the next valid IBKR orderID we can use
         */
        override fun nextValidId(orderId: Int) {
            this@IBKRBroker.orderId = orderId
            logger.debug("$orderId")
        }


        override fun openOrder(orderId: Int, contract: Contract, order: IBOrder, orderState: IBOrderSate) {
            logger.debug { "orderId=$orderId asset=${contract.symbol()} qty=${order.totalQuantity()} status=${orderState.status}" }
            logger.trace { "$orderId $contract $order $orderState" }
            val openOrder = orderMap[orderId]
            if (openOrder != null) {
                println("existing order orderId=$orderId status=${orderState.status}")
                if (orderState.completedStatus() == "true") {
                    val o = _account.getOrder(openOrder.id)
                    if (o != null) {
                        _account.updateOrder(o, Instant.parse(orderState.completedTime()), OrderStatus.COMPLETED)
                    }
                }
            } else {
                println("new order orderId=$orderId parentId=${order.parentId()} status=${orderState.status}")
                // if (order.parentId() > 0) return // right now no support for open bracket orders
                val newOrder = toOrder(order, contract)
                orderMap[orderId] = newOrder
                _account.initializeOrders(listOf(newOrder))
                val newStatus = toStatus(orderState.status)
                _account.updateOrder(newOrder, Instant.now(), newStatus)
            }
        }


        override fun orderStatus(
            orderId: Int, status: String, filled: Decimal?,
            remaining: Decimal?, avgFillPrice: Double, permId: Int, parentId: Int,
            lastFillPrice: Double, clientId: Int, whyHeld: String?, mktCapPrice: Double
        ) {
            logger.debug { "orderstatus oderId=$orderId status=$status filled=$filled" }
            val order = orderMap[orderId]
            if (order != null) {
                val newStatus = toStatus(status)
                _account.updateOrder(order, Instant.now(), newStatus)
            } else {
                logger.warn { "Received unknown open order with orderId $orderId" }
            }
        }

        /**
         * This is called with fee and pnl of a trade.
         */
        override fun commissionReport(commissionReport: CommissionReport) {
            logger.debug { "commissionReport execId=${commissionReport.execId()} currency=${commissionReport.currency()} fee=${commissionReport.commission()} pnl=${commissionReport.realizedPNL()}" }
            val id = commissionReport.execId().substringBeforeLast('.')
            val trade = tradeMap[id]
            if (trade != null) {
                val i = account.trades.indexOf(trade)
                val newTrade = trade.copy(
                    feeValue = commissionReport.commission(), pnlValue = commissionReport.realizedPNL()
                )
                _account.trades[i] = newTrade
            } else {
                logger.warn("Commission for none existing trade ${commissionReport.execId()}")
            }
        }


        override fun execDetails(reqId: Int, contract: Contract, execution: Execution) {
            logger.debug { "execDetails execId: ${execution.execId()} asset: ${contract.symbol()} side: ${execution.side()} qty: ${execution.cumQty()} price: ${execution.avgPrice()}" }

            // The last number is to correct an existing execution, so not a new execution
            val id = execution.execId().substringBeforeLast('.')

            if (id in tradeMap) {
                logger.info("trade already handled, no support for corrections currently")
                return
            }

            // Possible values BOT and SLD
            val size = if (execution.side() == "SLD") -execution.cumQty().value() else execution.cumQty().value()
            val order = orderMap[execution.orderId()]

            if (order != null) {
                val trade = Trade(
                    Instant.now(),
                    contract.toAsset(),
                    Size(size),
                    execution.avgPrice(),
                    Double.NaN,
                    Double.NaN,
                    order.id
                )
                tradeMap[id] = trade
                _account.addTrade(trade)
            }
        }

        override fun openOrderEnd() {
            println("openOrderEnd")
            logger.debug("openOrderEnd")
        }

        override fun accountDownloadEnd(accountName: String?) {
            logger.debug("accountDownloadEnd accountName=$accountName")
            accountUpdate.update(_account)

            // Reset to clean state
            accountUpdate = AccountUpdate()
            // println("waiting")
            synchronized(accountUpdateLock) {
                // println("notify")
                accountUpdateLock.notify()
            }
        }

        override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
            logger.debug { "updateAccountValue key=$key value=$value currency=$currency account=$accountName" }

            if (key == "AccountCode") require(value.startsWith('D')) {
                "currently only paper trading account is supported, found $value"
            }

            if (currency != null && "BASE" != currency) {
                val c = Currency.getInstance(currency)
                when (key) {
                    "BuyingPower" -> {
                        accountUpdate.base = c
                        accountUpdate.buyingPower = value.toDouble()
                    }

                    "CashBalance" -> accountUpdate.cash.set(c, value.toDouble())
                }
            }
        }

        override fun updatePortfolio(
            contract: Contract,
            position: Decimal,
            marketPrice: Double,
            marketValue: Double,
            averageCost: Double,
            unrealizedPNL: Double,
            realizedPNL: Double,
            accountName: String?
        ) {
            logger.debug { "updatePortfolio asset=${contract.symbol()} position=$position price=$marketPrice cost=$averageCost" }
            logger.trace { "updatePortfolio $contract $position $marketPrice $averageCost" }
            val asset = contract.toAsset()
            val size = Size(position.value())
            val p = Position(asset, size, averageCost, marketPrice, Instant.now())
            accountUpdate.positions.add(p)
        }

        override fun position(account: String?, contract: Contract?, pos: Decimal?, avgCost: Double) {
            logger.trace { "$account, $contract, $pos, $avgCost" }
        }

        override fun positionEnd() {
            logger.trace { "positionEnd" }
        }

        override fun updateAccountTime(timeStamp: String?) {
            logger.trace("updateAccountTime timestamp=$timeStamp")
        }


    }
}

