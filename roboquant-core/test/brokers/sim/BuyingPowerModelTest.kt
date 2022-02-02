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

internal class  BuyingPowerModelTest {

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
        println(broker.account.summary())
    }

    @Test
    fun testCashAccount() {
        val initial = Wallet(10_000.EUR)
        val broker = SimBroker(initial, costModel = NoCostModel())
        val abc = Asset("ABC", currencyCode = "EUR")
        val xyz = Asset("XYZ", currencyCode = "USD")

        Config.exchangeRates = FixedExchangeRates(EUR, USD to 0.9)
        val account = broker.account

        update(broker, abc, 100)
        assertEquals(Amount(EUR,10_000), account.buyingPower )
        update(broker,abc, 100, 40)
        assertEquals(initial, account.equity)
        update(broker,abc, 75)
        update(broker,abc, 75, -40)
        update(broker,xyz, 200, 25)
        update(broker,xyz, 240)
        update(broker,xyz, 240, -25)
        assertEquals(Amount(EUR,9900), account.buyingPower )

    }

    @Test
    fun testMarginAccount() {
        val initial = Wallet(10_000.USD)
        val broker = SimBroker(initial, costModel = NoCostModel(), buyingPowerModel = MarginBuyingPower())
        val abc = Asset("ABC", currencyCode = "USD")

        val account = broker.account

        update(broker, abc, 100)
        assertEquals(Amount(USD,10_000) * 2, account.buyingPower )

        update(broker,abc, 100, 50)
        assertEquals(Amount(USD,17_000), account.buyingPower )
        assertEquals(initial, account.equity)

        update(broker,abc, 60)
        assertEquals(Amount(USD,14_200), account.buyingPower )

        update(broker,abc, 60, -50)
        assertEquals(Amount(USD,16_000), account.buyingPower )
        assertEquals(account.cash.getAmount(USD) * 2.0, account.buyingPower )
    }


}