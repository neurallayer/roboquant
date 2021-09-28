package org.roboquant.binance

import org.junit.Test
import org.roboquant.Phase
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class BinanceBrokerTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_BINANCE") ?: return
        val broker = BinanceBroker()
        assertTrue(broker.toString().isNotEmpty())
        assertFails { broker.start(Phase.MAIN) }
    }

}