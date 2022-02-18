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

package org.roboquant.policies


import org.roboquant.TestData
import org.roboquant.brokers.InternalAccount
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import kotlin.test.Test
import kotlin.test.assertTrue

internal class TestPolicyTest {

    @Test
    fun order() {
        val policy = TestPolicy()
        val signals = listOf(Signal(TestData.usStock(), Rating.BUY))
        val event = TestData.event2()
        val account = InternalAccount().toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.first() is MarketOrder)
    }
}