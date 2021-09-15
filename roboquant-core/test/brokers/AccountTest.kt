package org.roboquant.brokers


import org.roboquant.common.Asset
import org.junit.Test
import kotlin.test.*
import org.roboquant.common.Currency
import java.time.Instant

internal class AccountTest {

    @Test
    fun basis() {
        val account = Account()
        val wallet = account.getValue()
        val amount = account.convertToCurrency(wallet, now=Instant.now())
        assertEquals(0.00, amount)
        assertEquals(Currency.getInstance("USD"), account.baseCurrency)
        assertTrue(account.portfolio.isEmpty())

        assertTrue(account.trades.isEmpty())
        assertTrue(account.trades.realizedPnL().isEmpty())

        val asset= Asset("Dummy")
        assertTrue(account.trades.realizedPnL(asset) == 0.0)

        val s = account.summary()
        assertTrue(s.toString().isNotEmpty())

        val o = account.orders
        assertTrue(o.summary().toString().isNotEmpty())
    }


}