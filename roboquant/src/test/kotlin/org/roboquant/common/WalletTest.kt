/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.common

import kotlin.test.*

internal class WalletTest {

    private val usd = Currency.getInstance("USD")
    private val eur = Currency.getInstance("EUR")

    @Test
    fun getCurrencies() {
        var wallet = Wallet()
        assertTrue(wallet.currencies.isEmpty())
        wallet = Wallet(10.USD, 20.EUR)
        assertEquals(2, wallet.currencies.size)
        assertTrue(wallet.isMultiCurrency())

        wallet = Wallet(10.USD, 0.EUR)
        assertEquals(1, wallet.currencies.size)

        assertTrue { wallet.toString().contains("USD") }
    }

    @Test
    fun deposit() {
        val wallet = Wallet()
        wallet.deposit(12.USD)
        assertEquals(12.0, wallet.getValue(usd))

        wallet.deposit(13.USD)
        assertEquals(25.USD, wallet.getAmount(usd))

        wallet.deposit(13.EUR)
        assertEquals(25.USD, wallet.getAmount(usd))
        assertEquals(13.EUR, wallet.getAmount(eur))

        wallet.withdraw(wallet)
        assertTrue(wallet.isEmpty())
        assertFalse(wallet.isNotEmpty())
    }

    @Test
    fun withdraw() {
        val wallet = Wallet()
        wallet.withdraw(12.USD)
        assertEquals(-12.0, wallet.getValue(usd))

        var metrics = wallet.toMap()
        assertContains(metrics, usd)

        wallet.deposit(12.USD)
        metrics = wallet.toMap()
        assertFalse(usd in metrics)
    }

    @Test
    fun plusMinus() {
        val wallet = Wallet(10.USD, 20.EUR)
        val wallet2 = wallet + wallet - wallet
        assertNotEquals(wallet.hashCode(), wallet2.hashCode())
        assertEquals(wallet, wallet2)

        val amount = 10.USD
        val wallet3 = Wallet(amount) - amount
        assertTrue(wallet3.isEmpty())
    }

    @Test
    fun summary() {
        val wallet = Wallet(10.USD, 20.EUR)
        assertTrue(wallet.summary().content.isNotEmpty())
    }

    @Test
    fun operators() {
        val wallet = Wallet(10.USD, 20.EUR)
        val wallet2 = wallet + wallet - wallet
        assertEquals(wallet, wallet2)

        val wallet3 = wallet * 2
        assertEquals(20.USD, wallet3.getAmount(usd))

        val wallet4 = wallet / 2
        assertEquals(5.USD, wallet4.getAmount(usd))
    }

    @Test
    fun multiCurrency() {
        val cash = 100.EUR + 200.USD
        assertTrue(cash.isMultiCurrency())

        val cash2 = 100.EUR.toWallet()
        assertFalse(cash2.isMultiCurrency())
    }

}