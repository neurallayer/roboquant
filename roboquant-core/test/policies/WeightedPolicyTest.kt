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