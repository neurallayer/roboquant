package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import org.roboquant.Phase
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class BinanceBrokerTestIT {

    @Test
    fun test() {
        true && return
        val client = BinanceApiClientFactory.newInstance().newRestClient()
        val broker = BinanceBroker(client)
        assertTrue(broker.toString().isNotEmpty())
        assertFails { broker.start(Phase.MAIN) }
    }

}