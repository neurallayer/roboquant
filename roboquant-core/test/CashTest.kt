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

        assertTrue { wallet.toString().contains("USD") }
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

        var metrics = wallet.toMap(true)
        assertContains(metrics, USD)

        wallet.deposit(USD, 12.0)
        metrics = wallet.toMap(true)
        assertContains(metrics, USD)

        metrics = wallet.toMap(false)
        assertFalse(USD in metrics)


    }

    @Test
    fun copy() {
        val wallet = Cash(USD to 10.0, EUR to 20.0)
        val wallet2 = wallet.copy()
        assertEquals(wallet.getAmount(USD), wallet2.getAmount(USD))
    }


    @Test
    fun operators() {
        val wallet = Cash(USD to 10.0, EUR to 20.0)
        val wallet2 = wallet + wallet - wallet
        assertEquals(wallet, wallet2)
    }
}