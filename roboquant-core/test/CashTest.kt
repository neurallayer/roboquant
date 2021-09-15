@file:Suppress("PrivatePropertyName", "PrivatePropertyName")

package org.roboquant

import kotlin.test.*

import org.roboquant.common.Cash
import org.roboquant.common.Currency

internal class CashTest {

    private val USD = Currency.getInstance("USD")
    private val EUR = Currency.getInstance("EUR")

    @Test
    fun getCurrencies() {
        var wallet = Cash()
        assertTrue(wallet.currencies.isEmpty())
        wallet = Cash(USD to 10.0, EUR to 20.0)
        assertEquals(2, wallet.currencies.size)
        assertTrue(wallet.isMultiCurrency())

        wallet = Cash(USD to 10.0, EUR to 0.0)
        assertEquals(1, wallet.currencies.size)
    }

    @Test
    fun deposit() {
        val wallet = Cash()
        wallet.deposit(USD, 12.0)
        assertEquals(12.0, wallet.getAmount(USD))

        wallet.deposit(USD, 13.0)
        assertEquals(25.0, wallet.getAmount(USD))

        wallet.deposit(EUR, 13.0)
        assertEquals(25.0, wallet.getAmount(USD))
        assertEquals(13.0, wallet.getAmount(EUR))

        wallet.withdraw(wallet)
        assertTrue(wallet.isEmpty())
    }

    @Test
    fun withdraw() {
        val wallet = Cash()
        wallet.withdraw(USD, 12.0)
        assertEquals(-12.0, wallet.getAmount(USD))

        val metrics = wallet.toMetrics("test.")
        assertContains(metrics, "test.USD")
    }

    @Test
    fun copy() {
        val wallet = Cash(USD to 10.0, EUR to 20.0)
        val wallet2 = wallet.copy()
        assertEquals(wallet.getAmount(USD), wallet2.getAmount(USD))
    }
}