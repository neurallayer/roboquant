/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.roboquant.brokers.sim.execution


import kotlinx.coroutines.launch
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.brokers.Trade
import org.roboquant.common.Asset
import org.roboquant.common.Currency
import org.roboquant.common.ParallelJobs
import org.roboquant.common.Size
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class InternalAccountTest {

    @Test
    fun internalAccountOrders() {
        val account = InternalAccount(Currency.USD)
        val order = MarketOrder(Asset("Test"), 100)
        account.initializeOrders(listOf(order))

        assertEquals(OrderStatus.INITIAL, account.orders.first().status)
        assertEquals(1, account.orders.size)

        val opened = Instant.now()
        account.updateOrder(order, opened, OrderStatus.ACCEPTED)
        assertEquals(OrderStatus.ACCEPTED, account.orders.first().status)
        assertEquals(1, account.orders.size)

        val closed = Instant.now()
        account.updateOrder(order, closed, OrderStatus.COMPLETED)
        assertEquals(0, account.orders.size)
        assertEquals(OrderStatus.COMPLETED, account.toAccount().closedOrders.first().status)
    }



    /**
     * Run several methods concurrent to detect possible issues
     */
    @Test
    fun runConcurrent() {
        val nThreads = 10
        val loop = 100
        val jobs = ParallelJobs()
        val iAccount = InternalAccount(Currency.USD)
        repeat(nThreads) {
            jobs.add {
                launch {
                    assertDoesNotThrow {
                        repeat(loop) {
                            iAccount.initializeOrders(
                                listOf(LimitOrder(Asset("ABC"), Size(3), 40000.0, tag = "my-tag"))
                            )
                            val trade = Trade(Instant.now(), Asset("ABC$it"), Size(10), 100.0, 0.0, 0.0, it.toString())
                            iAccount.addTrade(trade)
                            Thread.sleep(1)
                        }
                        Thread.sleep(1)
                        repeat(10) {
                            val account = iAccount.toAccount()
                            for (trade in account.trades) assertEquals(Size(10), trade.size)
                            val result = account.openOrders.firstOrNull { it.tag == "doesnt-exist" }
                            assertTrue(result == null)
                        }
                    }
                }
            }
        }
        assertEquals(nThreads, jobs.size)
        jobs.joinAllBlocking()

    }


}
