package org.roboquant.brokers.sim

import org.roboquant.TestData
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class  BuyingPowerTest {

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
    fun test2() {
        val account = TestData.usAccount()
        val uc = RegTCalculator()
        val result = uc.calculate(account)
        assertTrue(result.value > account.cashAmount.value)
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
        val uc = ForexBuyingPower()
        val result = uc.calculate(account)
        assertTrue(result.value > account.cashAmount.value)
    }

}