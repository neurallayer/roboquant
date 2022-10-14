/*
 * Copyright 2020-2022 Neural Layer
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
import org.roboquant.brokers.*
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import org.roboquant.orders.OrderStatus
import java.lang.Thread.sleep
import java.time.Instant
import com.ib.client.Order as IBOrder
import com.ib.client.OrderState as IBOrderSate
import com.ib.client.OrderStatus as IBOrderStatus

/**
 * Use your Interactive Brokers account for trading. Can be used with live trading or paper trading accounts of
 * Interactive Brokers. It is highly recommend to start with a paper trading account and validate your strategy and
 * policy extensively before moving to live trading.
 *
 * ## Use at your own risk, since there are no guarantees about the correct functioning of the roboquant software.
 *
 * @property accountId
 * @constructor
 *
 */
class IBKRBroker(
    configure: IBKRConfig.() -> Unit = {}
) : Broker {

    private val config = IBKRConfig()

    private val accountId: String?
    private var client: EClientSocket
    private var _account = InternalAccount()

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
        val wrapper = Wrapper(logger)
        client = IBKRConnection.connect(wrapper, config)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, accountId)
        // client.reqAccountSummary(9004, "All", "\$LEDGER:ALL")

        // Only request orders created with this client, so roboquant
        // doesn't use client.reqAllOpenOrders()
        client.reqOpenOrders()
        waitTillSynced()
    }

    /**
     * Disconnect roboquant from TWS or IB Gateway
     */
    fun disconnect() = IBKRConnection.disconnect(client)

    /**
     * Wait till IBKR account is synchronized so roboquant has the correct assets and cash balance available.
     *
     * @TODO: replace sleep with real check
     */
    private fun waitTillSynced() {
        @Suppress("MagicNumber")
        sleep(5_000)
    }

    /**
     * Place zero or more [orders]
     *
     * @param orders
     * @param event
     * @return
     */
    override fun place(orders: List<Order>, event: Event): Account {
        _account.putOrders(orders.initialOrderState)

        // First we place the cancellation orders
        for (cancellation in orders.filterIsInstance<CancelOrder>()) {
            logger.debug("received order $cancellation")
            val id = cancellation.id
            val ibID = orderMap.filter { it.value.id == id }.keys.first()
            client.cancelOrder(ibID, cancellation.tag)
        }

        // And now the regular new orders
        for (order in orders.filterIsInstance<SingleOrder>()) {
            logger.debug("received order $order")
            val ibOrder = createIBOrder(order)
            logger.debug("placing IBKR order with orderId ${ibOrder.orderId()}")
            val contract = IBKRConnection.getContract(order.asset)
            client.placeOrder(ibOrder.orderId(), contract, ibOrder)
        }

        // Return a clone so changes to account while running policies don't cause inconsistencies.
        return _account.toAccount()
    }

    /**
     * convert roboquant [order] into IBKR order. Right now only support for few single type of orders
     */
    private fun createIBOrder(order: SingleOrder): IBOrder {
        val result = IBOrder()
        when (order) {
            is MarketOrder -> result.orderType("MKT")
            is LimitOrder -> {
                result.orderType("LMT"); result.lmtPrice(order.limit)
            }

            is StopOrder -> {
                result.orderType("STP"); result.lmtPrice(order.stop)
            }

            else -> {
                throw UnsupportedException("unsupported order type $order")
            }
        }

        val action = if (order.size > 0) "BUY" else "SELL"
        result.action(action)
        result.totalQuantity(Decimal.get(order.size.toBigDecimal().abs()))
        if (accountId != null) result.account(accountId)

        orderMap[orderId] = order
        result.orderId(orderId++)
        return result
    }

    /**
     * Overwrite the default wrapper
     */
    inner class Wrapper(logger: Logging.Logger) : BaseWrapper(logger) {

        /**
         * Convert an IBOrder to a roboquant Order. This is only used during initial connect when retrieving any open
         * orders linked to the account.
         */
        private fun toOrder(order: IBOrder, contract: Contract): Order {
            val asset = contract.getAsset()
            val qty = if (order.action() == Types.Action.BUY) order.totalQuantity() else order.totalQuantity().negate()
            return MarketOrder(asset, Size(qty.value()))
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
        override fun nextValidId(id: Int) {
            orderId = id
            logger.debug("$id")
        }

        override fun openOrder(orderId: Int, contract: Contract, order: IBOrder, orderState: IBOrderSate) {
            logger.debug { "orderId=$orderId asset=${contract.symbol()} qty=${order.totalQuantity()} status=${orderState.status}" }
            logger.trace { "$orderId $contract $order $orderState" }
            val openOrder = orderMap[orderId]
            if (openOrder != null) {
                if (orderState.completedStatus() == "true") {
                    val slip = _account.openOrders[openOrder.id]
                    if (slip != null) _account.putOrder(
                        OrderState(
                            slip.order,
                            OrderStatus.COMPLETED,
                            closedAt = Instant.parse(orderState.completedTime())
                        )
                    )
                }
            } else {
                val newOrder = toOrder(order, contract)
                orderMap[orderId] = newOrder
                _account.putOrder(OrderState(newOrder))
            }
        }

        override fun orderStatus(
            orderId: Int, status: String?, filled: Decimal,
            remaining: Decimal, avgFillPrice: Double, permId: Int, parentId: Int,
            lastFillPrice: Double, clientId: Int, whyHeld: String?, mktCapPrice: Double
        ) {
            logger.debug { "oderId: $orderId status: $status filled: $filled" }
            val order = orderMap[orderId]
            if (order == null)
                logger.warn { "Received unknown open order with orderId $orderId" }
            else {
                val slip = _account.openOrders[order.id]
                if (slip != null) {
                    val newStatus = toStatus(status!!)
                    val orderState = slip.copy(Instant.now(), newStatus)
                    _account.putOrder(orderState)
                }
            }
        }

        override fun accountSummary(p0: Int, p1: String?, p2: String?, p3: String?, p4: String?) {
            logger.debug { "$p0, $p1, $p2, $p3, $p4" }
        }

        override fun accountSummaryEnd(p0: Int) {
            logger.debug { "$p0" }
        }

        /**
         * This is called with fee and pnl of a trade.
         */
        override fun commissionReport(report: CommissionReport) {
            logger.debug { "commissionReport execId=${report.execId()} currency=${report.currency()} fee=${report.commission()} pnl=${report.realizedPNL()}" }
            val id = report.execId().substringBeforeLast('.')
            val trade = tradeMap[id]
            if (trade != null) {
                val i = account.trades.indexOf(trade)
                val newTrade = trade.copy(
                    feeValue = report.commission(), pnlValue = report.realizedPNL()
                )
                _account.trades[i] = newTrade
            } else {
                logger.warn("Commission for none existing trade ${report.execId()}")
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
                    contract.getAsset(),
                    Size(size),
                    execution.avgPrice(),
                    Double.NaN,
                    Double.NaN,
                    order.id
                )
                tradeMap[id] = trade
                _account.trades += trade
            }
        }

        override fun openOrderEnd() {
            logger.debug("openOrderEnd")
        }

        override fun accountDownloadEnd(p0: String?) {
            logger.debug("accountDownloadEnd $p0")
        }

        override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
            logger.debug { "updateAccountValue $key $value $currency $accountName" }

            if (key == "AccountCode") require(value.startsWith('D')) {
                "currently only paper trading account is supported, found $value"
            }

            if (currency != null && "BASE" != currency) {
                when (key) {
                    "BuyingPower" -> {
                        _account.baseCurrency = Currency.getInstance(currency)
                        _account.buyingPower = Amount(account.baseCurrency, value.toDouble())
                    }

                    "CashBalance" -> _account.cash.set(Currency.getInstance(currency), value.toDouble())
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
            accountName: String
        ) {
            logger.debug { "updatePortfolio asset: ${contract.symbol()} position: $position price: $marketPrice cost: $averageCost" }
            logger.trace { "updatePortfolio $contract $position $marketPrice $averageCost" }
            val asset = contract.getAsset()
            val size = Size(position.value())
            val p = Position(asset, size, averageCost, marketPrice, Instant.now())
            _account.setPosition(p)
        }

        override fun updateAccountTime(timeStamp: String) {
            logger.debug(timeStamp)
            _account.lastUpdate = Instant.now()
        }

        /**
         * Convert an IBKR contract to a roboquant asset
         */
        private fun Contract.getAsset(): Asset {
            val result = IBKRConnection.assetMap[conid()]
            result != null && return result

            val exchangeCode = exchange() ?: primaryExch() ?: ""

            val asset = when (secType()) {
                Types.SecType.STK -> Asset(symbol(), AssetType.STOCK, currency(), exchangeCode)
                Types.SecType.BOND -> Asset(symbol(), AssetType.BOND, currency(), exchangeCode)
                else -> throw UnsupportedException("Unsupported asset type ${secType()}")
            }

            IBKRConnection.assetMap[conid()] = asset
            return asset
        }

    }
}

