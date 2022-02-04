package org.roboquant.brokers.sim

import org.roboquant.TestData
import org.roboquant.brokers.Broker
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.common.*
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BuyingPowerModelTest {

    @Test
    fun test() {
        val account = TestData.usAccount()
        val uc = CashBuyingPower()
        val result = uc.calculate(account)
        assertEquals(result.value, account.cashAmount.value)

        val order = MarketOrder(TestData.usStock(), 100.0)
        order.status = OrderStatus.ACCEPTED
        order.placed = Instant.now()
        order.price = 10.0
        account.orders.add(order)
        val result2 = uc.calculate(account)
        assertTrue(result2.value < result.value)
    }


    @Test
    fun test3() {
        val account = TestData.usAccount()
        val uc = MarginBuyingPower()
        val result = uc.calculate(account)
        assertTrue(result.value > account.cashAmount.value)
    }

    @Test
    fun test4() {
        val account = TestData.usAccount()
        val uc = MarginBuyingPower(20.0)
        val result = uc.calculate(account)
        assertTrue(result.value > account.cashAmount.value)
    }

    private fun update(broker: Broker, asset: Asset, price: Number, orderSize: Number = 0) {
        val orders = if (orderSize == 0) listOf() else listOf(MarketOrder(asset, orderSize.toDouble()))
        val action = TradePrice(asset, price.toDouble())
        val event = Event(listOf(action), Instant.now())
        broker.place(orders, event)
        // println(broker.account.summary())
    }

    @Test
    fun testCashAccount() {
        // Slide 1 example in code
        val initial = Wallet(10_000.EUR)
        val broker = SimBroker(initial, costModel = NoCostModel())
        val abc = Asset("ABC", currencyCode = "EUR")
        val xyz = Asset("XYZ", currencyCode = "USD")

        Config.exchangeRates = FixedExchangeRates(EUR, USD to 0.9)
        val account = broker.account

        update(broker, abc, 100)
        assertEquals(10_000.EUR, account.buyingPower)

        update(broker, abc, 100, 40)
        assertEquals(initial, account.equity)
        assertEquals(4_000.EUR.toWallet(), account.portfolio.value)
        assertEquals(6_000.EUR, account.buyingPower)

        update(broker, abc, 75)
        assertEquals(9_000.EUR, account.equityAmount)

        update(broker, abc, 75, -40)
        assertEquals(9_000.EUR, account.buyingPower)

        update(broker, xyz, 200, 25)
        assertEquals(4_500.EUR, account.buyingPower)

        update(broker, xyz, 240)
        assertEquals(4_500.EUR, account.buyingPower)

        update(broker, xyz, 240, -25)
        assertEquals(9900.EUR, account.buyingPower)

    }


    @Test
    fun testMarginAccountLong() {
        // Slide 2 example in code
        val initial = Wallet(1_000_000.JPY)
        val broker = SimBroker(initial, costModel = NoCostModel(), buyingPowerModel = MarginBuyingPower())
        val abc = Asset("ABC", currencyCode = "JPY")

        val account = broker.account

        update(broker, abc, 1000)
        assertEquals(2_000_000.JPY, account.buyingPower)

        update(broker, abc, 1000, 500)
        assertEquals(1_700_000.JPY, account.buyingPower)
        assertEquals(initial, account.equity)

        update(broker, abc, 500)
        assertEquals(1_350_000.JPY, account.buyingPower)

        update(broker, abc, 500, 2000)
        assertEquals(750_000.JPY, account.buyingPower)

        update(broker, abc, 400)
        assertEquals(400_000.JPY, account.buyingPower)

        update(broker, abc, 400, -2500)
        assertEquals(1_000_000.JPY, account.buyingPower)

    }

    @Test
    fun testMarginAccountShort() {
        // Slide 3 example example in code
        val initial = Wallet(20_000.USD)
        val broker = SimBroker(initial, costModel = NoCostModel(), buyingPowerModel = MarginBuyingPower())
        val abc = Asset("ABC", currencyCode = "USD")

        val account = broker.account

        update(broker, abc, 200, -50)
        assertEquals(34_000.USD, account.buyingPower)
        assertEquals(initial, account.equity)

        update(broker, abc, 300)
        assertEquals(Amount(USD, 21_000), account.buyingPower)

        update(broker, abc, 300, -50)
        assertEquals(12_000.USD, account.buyingPower)

        update(broker, abc, 300, 100)
        assertEquals(30_000.USD, account.buyingPower)
        assertEquals(15_000.USD.toWallet(), account.cash)
    }


}