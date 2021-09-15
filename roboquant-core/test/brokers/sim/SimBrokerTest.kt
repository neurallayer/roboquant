package org.roboquant.brokers.sim


import org.junit.Test
import org.roboquant.TestData
import org.roboquant.common.Currency
import org.roboquant.orders.OrderStatus
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SimBrokerTest {

    @Test
    fun basicSimBrokerTest() {
        val broker = SimBroker()
        val event = TestData.event()
        val account = broker.place(listOf(),event)
        assertTrue(account.orders.isEmpty())

        broker.place(listOf(),event)

        val broker2 = SimBroker.withDeposit(100_000.00)
        assertEquals(Currency.USD, broker2.account.baseCurrency)

        val metrics = broker.getMetrics()
        assertTrue(metrics.isEmpty())
    }

    @Test
    fun basicPlaceOrder() {
        val broker = SimBroker()
        val event = TestData.event()
        var account = broker.place(listOf(TestData.euMarketOrder(), TestData.usMarketOrder()), event)
        assertEquals(2, account.orders.size)
        assertEquals(account.orders.closed.size, account.trades.size)


        assertEquals(1, account.orders.open.size)
        account = broker.place(listOf(), TestData.event2())
        assertEquals(0, account.orders.open.size)

    }

    @Test
    fun advancedPlaceOrder() {
        val broker = SimBroker(validateBuyingPower = true)
        val event = TestData.event()
        val account = broker.place(listOf(TestData.euMarketOrder(), TestData.usMarketOrder()), event)
        assertEquals(2, account.orders.size)
        assertEquals(1, account.orders.closed.size)
        assertEquals(1, account.orders.filter { it.status === OrderStatus.INITIAL }.size)

    }


}