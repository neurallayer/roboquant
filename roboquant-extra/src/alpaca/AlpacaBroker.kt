package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.order.enums.OrderSide
import net.jacobpeterson.alpaca.model.endpoint.order.enums.OrderTimeInForce
import net.jacobpeterson.alpaca.rest.AlpacaClientException
import org.roboquant.brokers.*
import org.roboquant.common.AssetType
import org.roboquant.common.Currency
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant
import java.util.logging.Logger
import net.jacobpeterson.alpaca.model.endpoint.order.enums.OrderStatus as AlpacaOrderStatus
import net.jacobpeterson.alpaca.model.endpoint.position.Position as AlpacaPosition

/**
 * Broker implementation for Alpaca. This implementation allows using the Alpaca live- and paper-trading capabilities
 * in combination with roboquant.
 *
 * See also the Alpaca feed components if you want to use Alpaca also for retrieving market data.
 *
 * @constructor Create new Alpaca broker
 *
 * @sample org.roboquant.samples.alpacaBroker
 */
class AlpacaBroker(
    apiKey: String? = null,
    apiSecret: String? = null,
    accountType: AccountType = AccountType.PAPER,
    dataType: DataType = DataType.IEX
) : Broker {

    override val account: Account = Account()
    private val alpacaAPI: AlpacaAPI = AlpacaConnection.getAPI(apiKey, apiSecret, accountType, dataType)
    private val logger: Logger = Logger.getLogger("AlpacaBroker")
    private val enableTrading = false

    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI)
    }

    private val assetsMap by lazy {
        availableAssets.associateBy { it.symbol }
    }

    init {
        updateAccount()
        updatePositions()
        updateOpenOrders()
    }

    /**
     * Update the roboquant account with the details from Alpaca account
     */
    private fun updateAccount() {
        try {
            val acc = alpacaAPI.account().get()
            account.buyingPower = acc.buyingPower.toDouble()
            account.baseCurrency = Currency.getInstance(acc.currency)

            account.cash.clear()
            account.cash.deposit(account.baseCurrency, acc.cash.toDouble())
            account.time = Instant.now()
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    /**
     * Update positions in the portfolio based on positions received from Alpaca.
     *
     */
    private fun updatePositions() {
        try {
            val portfolio = Portfolio()
            for (openPosition in alpacaAPI.positions().get()) {
                logger.fine { "received $openPosition" }
                val p = convertPos(openPosition)
                portfolio.setPosition(p)
            }
            // If there was an exception we don't reach this part and keep the old state of the portfolio.
            account.portfolio.clear()
            account.portfolio.put(portfolio)
        } catch (e: AlpacaClientException) {
            logger.severe(e.stackTraceToString())
        }
    }

    /**
     * Convert an Alpaca position to a roboquant position
     *
     * @param pos the Alpaca position
     * @return
     */
    private fun convertPos(pos: AlpacaPosition): Position {
        assert(pos.assetClass == "us_equity") { "Unsupported asset class found ${pos.assetClass} for position $pos" }
        val asset = assetsMap[pos.symbol]!!
        val qty = if (pos.side == "BUY") pos.qty.toDouble() else -(pos.qty.toDouble())
        return Position(asset, qty, pos.avgEntryPrice.toDouble(), pos.currentPrice.toDouble())
    }

    /**
     * Update the status of the open orders in the account with the latest order status from Alpaca
     */
    private fun updateOpenOrders() {
        account.orders.open.filterIsInstance<SingleOrder>().forEach {
            val order = alpacaAPI.orders().get(it.id, false)
            it.fill = order.filledQty.toDouble()

            when (order.status) {
                AlpacaOrderStatus.CANCELED -> it.status = OrderStatus.CANCELLED
                AlpacaOrderStatus.EXPIRED -> it.status = OrderStatus.EXPIRED
                AlpacaOrderStatus.FILLED -> it.status = OrderStatus.COMPLETED
                AlpacaOrderStatus.REJECTED -> it.status = OrderStatus.REJECTED
                else -> {
                    logger.info { "Received order status ${order.status}" }
                }
            }
        }
    }

    /**
     * Place an order at Alpaca.
     *
     * @param order
     */
    private fun placeOrder(order: SingleOrder) {
        val asset = order.asset
        require(asset.type == AssetType.STOCK) { "Only stocks supported, received ${asset.type}" }
        require(asset.currencyCode == "USD") { "Only USD supported, received ${asset.currencyCode}" }

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL
        val tif = when (order.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> {
                throw Exception("Unsupported TIF ${order.tif} for order $order")
            }
        }

        if (!enableTrading) {
            logger.info { "Trading not enabled, skipping $order" }
            return
        }

        val alpacaOrder = when (order) {
            is MarketOrder -> alpacaAPI.orders().requestMarketOrder(asset.symbol, order.absInt, side, tif)
            is LimitOrder -> alpacaAPI.orders()
                .requestLimitOrder(asset.symbol, order.absInt, side, tif, order.limit, false)
            else -> {
                throw Exception("Unsupported order type $order. Right now only Market and Limit orders are mapped")
            }
        }

        // val action = OrderTicket(order, alpacaOrder.id)
        when (alpacaOrder.status) {
            AlpacaOrderStatus.CANCELED -> order.status = OrderStatus.CANCELLED
            AlpacaOrderStatus.EXPIRED -> order.status = OrderStatus.EXPIRED
            AlpacaOrderStatus.FILLED -> order.status = OrderStatus.COMPLETED
            AlpacaOrderStatus.REJECTED -> order.status = OrderStatus.REJECTED
            else -> {
                logger.info { "Received order status ${order.status}" }
            }
        }
    }

    /**
     * Place new instructions at this broker, the most common instruction being an order. After any processing,
     * it returns an instance of Account.
     *
     * Right now only Order type instructions are supported
     *
     * See also [Order]
     *
     * @param orders list of action to be placed at the broker
     * @return the updated account that reflects the latest state
     */
    override fun place(orders: List<Order>, event: Event): Account {
        for (order in orders) {
            if (order is SingleOrder) {
                placeOrder(order)
            } else {
                throw Exception("Unsupported order type $order")
            }
        }
        account.time = event.now
        return account
    }
}
