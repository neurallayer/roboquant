package org.roboquant.ibkr


import java.time.Instant
import org.junit.Test
import kotlin.test.assertTrue

internal class IBKRBrokerTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_IBKR") ?: return
        val broker = IBKRBroker()
        val past = Instant.now()

        assertTrue(broker.account.time > past)
        broker.disconnect()

    }

}