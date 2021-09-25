package org.roboquant.alpaca


import org.roboquant.common.Config
import kotlin.test.*

class AlpacaBrokerTestIT {

    @Test
    fun test() {
        System.getProperty("ALPACA_TEST") ?: return
        val key = Config.getProperty("ALPACA_KEY")!!
        val secret = Config.getProperty("ALPACA_SECRET")!!
        val broker = AlpacaBroker(key, secret)
        assertTrue(broker.account.cash.isNotEmpty())
        assertFalse(broker.account.cash.isMultiCurrency())
        assertTrue(broker.assets.isNotEmpty())
    }

}