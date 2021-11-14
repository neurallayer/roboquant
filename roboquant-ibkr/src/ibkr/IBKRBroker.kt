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
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.CurrencyConverter
import org.roboquant.brokers.Position
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import org.roboquant.orders.OrderStatus
import java.lang.Thread.sleep
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    currencyConverter: CurrencyConverter? = null,
    private val accountId: String? = null,
    private val enableOrders: Boolean = false,
) : Broker {

    private var client: EClientSocket
    override val account: Account = Account(currencyConverter = currencyConverter)
    private var orderId = generateOrderId()
    private val placedOrders = mutableMapOf<Int, SingleOrder>()
    val logger = Logging.getLogger("IBKRBroker")

    init {
        if (enableOrders) logger.warning { "Enabled real orders, use at your own risk!!!" }
        val wrapper = Wrapper()
        client = IBKRConnection.connect(wrapper, host, port, clientId)
        client.reqCurrentTime()
        client.reqAccountUpdates(true, accountId)
        client.reqOpenOrders()
        waitTillSynced()
    }

    fun disconnect() = IBKRConnection.disconnect(client)


    /**
     * Generate a starting orderId that most likely won't conflict with other orderIds already
     * in the system. Should be ok for roughly 68 year before an integer overflow. If generating on average more
     * than 1 order per second with a quick restart also a conflict could arise.
     *
     * @return
     */
    private fun generateOrderId(): Int {
        val l1 = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        val l2 = LocalDateTime.parse("2020-01-01T00:00:00").toEpochSecond(ZoneOffset.UTC)
        return (l1 - l2).toInt()
    }


    /**
     * Wait till IBKR account is synchronized so roboquant has the correct assets and cash balance available.
     *
     * TODO: replace sleep with real check
     */
    private fun waitTillSynced() {
        sleep(5_000)
    }


    /**
     * Place a set of instruction
     *
     * @param orders
     * @param event
     * @return
     */
    override fun place(orders: List<Order>, event: Event): Account {
        if (! enableOrders) return account.clone()


        // First we place the cancellation orders
        for (cancellation in orders.filterIsInstance<CancellationOrder>()) {
            client.cancelOrder(cancellation.order.id.toInt())
        }

        // And now the regular new orders
        for (order in orders.filterIsInstance<SingleOrder>()) {
            orderId += 1
            val ibOrder = getIBOrder(order)
            logger.fine("placing order $order")
            placedOrders[orderId] = order
            val contract = IBKRConnection.getContract(order.asset)
            client.placeOrder(orderId, contract, ibOrder)
            account.orders.add(order)
        }

        // We always return a clone so changes to account while running strategies don't cause inconsistencies.
        return account.clone()
    }

    /**
     * convert roboquant order into IBKR order
     *
     * @param order
     * @return
     */
    private fun getIBOrder(order: SingleOrder): IBOrder {
        val result = IBOrder()
        when (order) {
            is MarketOrder -> result.orderType("MKT")
            is LimitOrder -> { result.orderType("LMT"); result.lmtPrice(order.limit) }
            is StopOrder -> { result.orderType("STP"); result.lmtPrice(order.stop)}
            else -> {
                throw Exception("unsupported order type $order")
            }
        }

        val action = if (order.quantity > 0) "BUY" else "SELL"
        result.action(action)
        result.totalQuantity(order.quantity.absoluteValue)
        if (accountId != null) result.account(accountId)
        return result
    }

    inner class Wrapper : DefaultEWrapper() {


        override fun openOrder(orderId: Int, contract: Contract, order: IBOrder, orderState: OrderState) {
            val openOrder = account.orders.open.filterIsInstance<SingleOrder>().find { it.id.toInt() == orderId }
            if (openOrder != null) {
                logger.info("OpenOrder: $orderId $contract $order $orderState")
                openOrder.fill = order.filledQuantity()
                if (orderState.completedStatus() == "true") {
                    openOrder.status = OrderStatus.COMPLETED
                }
            } else {
                logger.warning { "Received unknown open order with orderId $orderId" }
            }
        }

        override fun orderStatus(
            orderId: Int, status: String?, filled: Double,
            remaining: Double, avgFillPrice: Double, permId: Int, parentId: Int,
            lastFillPrice: Double, clientId: Int, whyHeld: String?, mktCapPrice: Double
        ) {
            logger.info { "orderStatus: $orderId $status $filled $remaining" }
            val id = orderId.toString()
            val openOrder = account.orders.open.filterIsInstance<SingleOrder>().find { it.id == id }
            if (openOrder == null)
                    logger.warning { "Received unknown open order with orderId $orderId" }
        }

        override fun openOrderEnd() {
            logger.fine("Open order ended")
        }

        private fun deposit(currencyCode: String, value: String) {
            if ("BASE" != currencyCode) {
                val currency = Currency.getInstance(currencyCode)
                val amount = value.toDouble()
                account.cash.deposit(currency, amount)
            }
        }

        override fun updateAccountValue(key: String, value: String, currency: String?, accountName: String?) {
            logger.fine { "account update $key $value $currency $accountName" }
            if (currency != null)
                when (key) {
                    "BuyingPower" -> {
                        account.buyingPower = value.toDouble()
                        account.baseCurrency = Currency.getInstance(currency)
                    }
                    "CashBalance" -> deposit(currency, value)
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
            logger.fine {"portfolio update $contract $position $marketPrice $averageCost" }
            val asset = contract.getAsset()
            val p = Position(asset, position, averageCost, marketPrice)
            account.portfolio.setPosition(p)
        }

        override fun currentTime(time: Long) {
            logger.fine { EWrapperMsgGenerator.currentTime(time).toString() }
            val now = Instant.ofEpochSecond(time)
            account.time = now
        }

        override fun updateAccountTime(timeStamp: String?) {
            logger.fine {"Account time $timeStamp" }
            account.time = Instant.now()
        }

        private fun Contract.getAsset(): Asset {
            val type = when(secType()) {
                Types.SecType.STK -> AssetType.STOCK
                Types.SecType.BOND -> AssetType.BOND
                Types.SecType.OPT -> AssetType.OPTION
                else -> throw Exception("unknown type ${secType()}")
            }
            val id = conid().toString()
            return Asset(
                symbol = symbol(),
                currencyCode = currency(),
                exchangeCode = exchange() ?: primaryExch() ?: "",
                type = type,
                id = id
            )
        }

        override fun error(var1: Exception) {
            logger.warning { "$var1" }
        }

        override fun error(var1: String?) {
            logger.warning { "$var1" }
        }

        override fun error(var1: Int, var2: Int, var3: String?) {
            if (var1 == -1)
                logger.info { "$var1 $var2 $var3" }
            else
                logger.warning { "$var1 $var2 $var3" }
        }


    }
}

