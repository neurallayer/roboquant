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

package org.roboquant.ibkr

import com.ib.client.*
import ibkr.IBKRExchangeRates
import org.roboquant.brokers.*
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import org.roboquant.orders.OrderStatus
import java.lang.Thread.sleep
import java.time.Instant
import kotlin.math.absoluteValue
import com.ib.client.Order as IBOrder

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
    host: String = "127.0.0.1",
    port: Int = 4002,
    clientId: Int = 1,
    private val exchangeRates: ExchangeRates? = IBKRExchangeRates(),
    private val accountId: String? = null,
    private val enableOrders: Boolean = false,
) : Broker {

    private var client: EClientSocket
    override val account: Account = Account()
    val logger = Logging.getLogger(IBKRBroker::class)
    private var orderId = 0

    // Track IB orders ids with roboquant orders
    private val orderMap = mutableMapOf<Int, Order>()

    // Track IB Trades and Feed ids with roboquant trades
    private val tradeMap = mutableMapOf<String, Trade>()

    // Holds mapping between contract Id and an asset.
    private val assetMap = mutableMapOf<Int, Asset>()

    init {
        if (enableOrders) logger.warning { "Enabling sending orders, use it at your own risk!!!" }
        val wrapper = Wrapper()
        client = IBKRConnection.connect(wrapper, host, port, clientId)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, accountId)

        // Only request orders created with this client, aka roboquant
        // don't use client.reqAllOpenOrders()
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
     * TODO: replace sleep with real check
     */
    private fun waitTillSynced() {
        sleep(5_000)
    }


    /**
     * Place on or more orders
     *
     * @param orders
     * @param event
     * @return
     */
    override fun place(orders: List<Order>, event: Event): Account {
        account.orders.addAll(orders)

        if (!enableOrders) return account.clone()

        // First we place the cancellation orders
        for (cancellation in orders.filterIsInstance<CancellationOrder>()) {
            logger.fine("received order $cancellation")
            val id = orderMap.filterValues { it == cancellation.order }.keys.first()
            client.cancelOrder(id)
        }

        // And now the regular new orders
        for (order in orders.filterIsInstance<SingleOrder>()) {
            logger.fine("received order $order")
            val ibOrder = createIBOrder(order)
            logger.fine("placing IBKR order with orderId ${ibOrder.orderId()}")
            val contract = IBKRConnection.getContract(order.asset)
            client.placeOrder(ibOrder.orderId(), contract, ibOrder)
        }

        // Return a clone so changes to account while running policies don't cause inconsistencies.
        return account.clone()
    }

    /**
     * convert roboquant order into IBKR order
     *
     * @param order
     * @return
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
                throw Exception("unsupported order type $order")
            }
        }

        val action = if (order.quantity > 0) "BUY" else "SELL"
        result.action(action)
        result.totalQuantity(order.quantity.absoluteValue)
        if (accountId != null) result.account(accountId)

        orderMap[orderId] = order
        result.orderId(orderId++)
        return result
    }


    /**
     * Overwrite the default wrapper
     */
    inner class Wrapper : DefaultEWrapper() {

        /**
         * Convert an IBOrder to a roboquant Order. This is only used during initial connect when retrieving any open
         * orders linked to the account.
         */
        private fun toOrder(order: IBOrder, contract: Contract): Order {
            val asset = contract.getAsset()
            val qty = if (order.action == "BUY") order.totalQuantity() else -order.totalQuantity()
            val result = MarketOrder(asset, qty)
            result.status = OrderStatus.ACCEPTED
            return result
        }

        /**
         * What is the next valid IBKR orderID we can use
         */
        override fun nextValidId(id: Int) {
            orderId = id
            logger.fine("$id")
        }

        override fun openOrder(orderId: Int, contract: Contract, order: IBOrder, orderState: OrderState) {
            logger.fine { "orderId: $orderId asset: ${contract.symbol()} qty: ${order.totalQuantity()} status: ${orderState.status}" }
            logger.finer { "$orderId $contract $order $orderState" }
            val openOrder = orderMap[orderId]
            if (openOrder != null) {
                if (orderState.completedStatus() == "true") {
                    openOrder.status = OrderStatus.COMPLETED
                }
            } else {
                val newOrder = toOrder(order, contract)
                orderMap[orderId] = newOrder
                account.orders.add(newOrder)
            }
        }

        override fun orderStatus(
            orderId: Int, status: String?, filled: Double,
            remaining: Double, avgFillPrice: Double, permId: Int, parentId: Int,
            lastFillPrice: Double, clientId: Int, whyHeld: String?, mktCapPrice: Double
        ) {
            logger.fine { "oderId: $orderId status: $status filled: $filled" }
            val openOrder = orderMap[orderId]
            if (openOrder == null)
                logger.warning { "Received unknown open order with orderId $orderId" }
            else if (openOrder is SingleOrder) {
                // openOrder.fill = filled
                // openOrder.price = lastFillPrice
                when (status) {
                    "PreSubmitted" -> openOrder.status = OrderStatus.INITIAL
                    "Submitted" -> openOrder.status = OrderStatus.ACCEPTED
                    "Filled" -> openOrder.status = OrderStatus.COMPLETED
                }
            }
        }

        /**
         * This is called with fee and pnl of a trade.
         */
        override fun commissionReport(report: CommissionReport) {
            logger.fine { "execId=${report.execId()} currency=${report.currency()} fee=${report.commission()} pnl=${report.realizedPNL()}" }
            val id = report.execId().substringBeforeLast('.')
            val trade = tradeMap[id]
            if (trade != null) {
                val i = account.trades.indexOf(trade)
                val newTrade = trade.copy(
                    feeValue = report.commission(), pnlValue = report.realizedPNL()
                )
                account.trades[i] = newTrade
            } else {
                logger.warning("Commision for none existing trade ${report.execId()}")
            }

        }

        override fun execDetails(reqId: Int, contract: Contract, execution: Execution) {
            logger.fine { "execId: ${execution.execId()} asset: ${contract.symbol()} side: ${execution.side()} qty: ${execution.cumQty()} price: ${execution.avgPrice()}" }

            // The last number is to correct an existing execution, so not a new execution
            val id = execution.execId().substringBeforeLast('.')
            if (id in tradeMap) logger.info("Overwrite of existing trade")

            // Possible values BOT and SLD
            val direction = if (execution.side() == "SLD") -1.0 else 1.0
            val orderId = orderMap[execution.orderId()]?.id ?: "UNKOWN" // Should not happen
            val trade = Trade(
                Instant.now(),
                contract.getAsset(),
                execution.cumQty() * direction,
                execution.avgPrice(),
                Double.NaN,
                Double.NaN,
                orderId
            )
            tradeMap[id] = trade
            account.trades.add(trade)
        }

        override fun openOrderEnd() {
            logger.fine("Open order ended")
        }

        private fun setCash(currencyCode: String, value: String) {
            if ("BASE" != currencyCode) {
                val amount = Amount(Currency.getInstance(currencyCode),  value.toDouble())
                account.cash.set(amount)
            }
        }

        override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
            logger.fine { "$key $value $currency $accountName" }
            if (currency != null && "BASE" != currency) {
                when (key) {
                    "BuyingPower" -> {
                        account.baseCurrency = Currency.getInstance(currency)
                        account.buyingPower = Amount(account.baseCurrency, value.toDouble())
                        if (exchangeRates is IBKRExchangeRates) exchangeRates.baseCurrency =
                            account.baseCurrency
                    }
                    "CashBalance" -> setCash(currency, value)
                    "ExchangeRate" -> {
                        if (exchangeRates is IBKRExchangeRates) {
                            val c = Currency.getInstance(currency)
                            exchangeRates.exchangeRates[c] = value.toDouble()
                        }
                    }
                }
            }
        }

        override fun updatePortfolio(
            contract: Contract,
            position: Double,
            marketPrice: Double,
            marketValue: Double,
            averageCost: Double,
            unrealizedPNL: Double,
            realizedPNL: Double,
            accountName: String
        ) {
            logger.fine { "asset: ${contract.symbol()} position: $position price: $marketPrice cost: $averageCost" }
            logger.finer { "$contract $position $marketPrice $averageCost" }
            val asset = contract.getAsset()
            val p = Position(asset, position, averageCost, marketPrice, Instant.now())
            account.portfolio.setPosition(p)
        }

        override fun currentTime(time: Long) {
            logger.fine { EWrapperMsgGenerator.currentTime(time).toString() }

            // If more than 60 seconds difference, give a warning
            val diff = Instant.now().epochSecond - time
            if (diff.absoluteValue > 60) logger.warning("Time clocks out of sync by $diff seconds")
        }

        override fun updateAccountTime(timeStamp: String) {
            logger.fine(timeStamp)
            account.lastUpdate = Instant.now()
        }

        /**
         * Convert an IBKR contract to an asset
         */
        private fun Contract.getAsset(): Asset {
            if (!assetMap.containsKey(conid())) {
                val type = when (secType()) {
                    Types.SecType.STK -> AssetType.STOCK
                    Types.SecType.BOND -> AssetType.BOND
                    Types.SecType.OPT -> AssetType.OPTION
                    else -> throw Exception("Unsupported asset type ${secType()}")
                }
                val asset = Asset(
                    symbol = symbol(),
                    currencyCode = currency(),
                    exchangeCode = exchange() ?: primaryExch() ?: "",
                    type = type,
                    id = conid().toString()
                )
                assetMap[conid()] = asset
            }

            return assetMap[conid()]!!
        }

        override fun error(var1: Exception) {
            logger.warning { "$var1" }
        }

        override fun error(var1: String?) {
            logger.warning { "$var1" }
        }

        override fun error(var1: Int, var2: Int, var3: String?) {
            if (var1 == -1)
                logger.finer { "$var1 $var2 $var3" }
            else
                logger.warning { "$var1 $var2 $var3" }
        }


    }
}

