package org.roboquant.brokers.xchange

import org.knowm.xchange.Exchange
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.junit.Test
import kotlin.test.assertFails


internal class XChangeBrokerTestIT {

    private fun getExchange(): Exchange {
        val exSpec = BitstampExchange().defaultExchangeSpecification
        exSpec.userName = System.getenv("bitstamp.userName")
        exSpec.apiKey = System.getenv("bitstamp.apiKey")
        exSpec.secretKey = System.getenv("bitstamp.secretKey")

        return ExchangeFactory.INSTANCE.createExchange(exSpec)
    }


    @Test
    fun test() {
        true && return
        assertFails {
            val exchange2 = getExchange()
            XChangeBroker(exchange2)
        }
    }

}