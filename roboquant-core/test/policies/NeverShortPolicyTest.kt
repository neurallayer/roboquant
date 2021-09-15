package org.roboquant.policies


import kotlin.test.*
import org.roboquant.strategies.Signal
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant

internal class NeverShortPolicyTest {

    @Test
    fun order() {
        val policy = NeverShortPolicy()
        val signals = mutableListOf<Signal>()
        val event = Event(listOf(), Instant.now())
        val account = Account()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())

    }

}