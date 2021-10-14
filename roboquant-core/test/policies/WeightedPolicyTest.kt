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


import kotlin.test.*
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import org.roboquant.strategies.Signal
import java.time.Instant

internal class WeightedPolicyTest {

    @Test
    fun test() {
        assertFails {
            val policy = WeightedPolicy()
            val signals = mutableListOf<Signal>()
            val event = Event(listOf(), Instant.now())
            val account = Account()
            val orders = policy.act(signals, account, event)
            assertTrue(orders.isEmpty())
        }
    }


}