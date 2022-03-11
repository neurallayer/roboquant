package org.roboquant.brokers.sim

import org.roboquant.TestData
import org.roboquant.brokers.*
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

internal class AccountModelTest {

    @Test
    fun test() {
        val account = TestData.internalAccount()
        val uc = CashAccount()
        val result = uc.calculate(account)
        assertEquals(result, account.cash.getAmount(result.currency))

        val order = MarketOrder(TestData.usStock(), 100.0)
        val state = DefaultOrderState(order, OrderStatus.ACCEPTED, Instant.now())
        account.putOrders(listOf(state))
        val result2 = uc.calculate(account)

        // Right now open orders are not taken into account
        assertEquals(result2.value, result.value)
    }


    @Test
    fun test3() {
        val account = TestData.internalAccount()
        val uc = MarginAccount()
        val result = uc.calculate(account)
        assertTrue(result.value > account.cash.getAmount(result.currency).value)
    }

    @Test
    fun test4() {
        val account = TestData.internalAccount()
        val uc = MarginAccount(20.0)
        val result = uc.calculate(account)
        assertTrue(result.value > account.cash.getAmount(result.currency).value)
    }

    private fun update(broker: Broker, asset: Asset, price: Number, orderSize: Number = 0): Account {
        val orders = if (orderSize == 0) emptyList() else listOf(MarketOrder(asset, orderSize.toDouble()))
        val action = TradePrice(asset, price.toDouble())
        val event = Event(listOf(action), Instant.now())
        return broker.place(orders, event)
    }


    private fun getSimBroker(deposit: Amount, accountModel: AccountModel): SimBroker {
        val wallet = deposit.toWallet()
        return SimBroker(wallet, accountModel = accountModel, pricingEngine = NoSlippagePricing(), feeModel = NoFeeModel())
    }

    @Test
    fun testCashAccount() {
        // Slide 1 example in code
        val initial = 10_000.EUR
        val broker = getSimBroker(initial, accountModel = CashAccount())
        val abc = Asset("ABC", currencyCode = "EUR")
        val xyz = Asset("XYZ", currencyCode = "USD")

        Config.exchangeRates = FixedExchangeRates(EUR, USD to 0.9)

        var account = update(broker, abc, 100)
        assertEquals(10_000.EUR, account.buyingPower)

        account = update(broker, abc, 100, 40)
        assertEquals(initial, account.equityAmount)
        assertEquals(4_000.EUR.toWallet(), account.portfolio.marketValue)
        assertEquals(6_000.EUR, account.buyingPower)

        account = update(broker, abc, 75)
        assertEquals(9_000.EUR, account.equityAmount)

        account = update(broker, abc, 75, -40)
        assertEquals(9_000.EUR, account.buyingPower)

        account = update(broker, xyz, 200, 25)
        assertEquals(4_500.EUR, account.buyingPower)

        account = update(broker, xyz, 240)
        assertEquals(4_500.EUR, account.buyingPower)

        account = update(broker, xyz, 240, -25)
        assertEquals(9900.EUR, account.buyingPower)

    }


    @Test
    fun testMarginAccountLong() {
        // Slide 2 example in code
        val initial = 1_000_000.JPY
        val broker = getSimBroker(initial,MarginAccount())
        val abc = Asset("ABC", currencyCode = "JPY")


        var account = update(broker, abc, 1000)
        assertEquals(2_000_000.JPY, account.buyingPower)

        account = update(broker, abc, 1000, 500)
        assertEquals(1_700_000.JPY, account.buyingPower)
        assertEquals(initial, account.equityAmount)

        account = update(broker, abc, 500)
        assertEquals(1_350_000.JPY, account.buyingPower)

        account = update(broker, abc, 500, 2000)
        assertEquals(750_000.JPY, account.buyingPower)

        account = update(broker, abc, 400)
        assertEquals(400_000.JPY, account.buyingPower)

        account = update(broker, abc, 400, -2500)
        assertEquals(1_000_000.JPY, account.buyingPower)

    }




    @Test
    fun testMarginAccountShort() {
        // Slide 3 example example in code
        val initial = 20_000.USD
        val broker = getSimBroker(initial, MarginAccount())
        val abc = Asset("ABC", currencyCode = "USD")

        var account = update(broker, abc, 200, -50)
        assertEquals(34_000.USD, account.buyingPower)
        assertEquals(initial, account.equityAmount)

        account = update(broker, abc, 300)
        assertEquals(Amount(USD, 21_000), account.buyingPower)

        account = update(broker, abc, 300, -50)
        assertEquals(12_000.USD, account.buyingPower)

        account = update(broker, abc, 300, 100)
        assertEquals(30_000.USD, account.buyingPower)
        assertEquals(15_000.USD.toWallet(), account.cash)
    }


}