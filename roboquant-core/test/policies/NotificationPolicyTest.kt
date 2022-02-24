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
import org.roboquant.common.Notifier
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class NotificationPolicyTest {

    class MyNotifier : Notifier {
        var cnt = 0

        override fun send(topic: String, msg: String) {
            cnt++
        }

    }


    private fun act(policy: Policy): List<Order> {
        val asset = TestData.usStock()
        val signals = mutableListOf(Signal(asset, Rating.BUY))
        val event = Event(emptyList(), Instant.now())
        val account = InternalAccount().toAccount()
        return policy.act(signals, account, event)
    }

    @Test
    fun test() {
        val policy = NotificationPolicy()
        val orders = act(policy)
        assertTrue(orders.isEmpty())

    }


    @Test
    fun test2() {
        val n = MyNotifier()
        val policy = NotificationPolicy(n)
        val orders = act(policy)
        assertTrue(orders.isEmpty())
        assertEquals(1, n.cnt)
    }


}