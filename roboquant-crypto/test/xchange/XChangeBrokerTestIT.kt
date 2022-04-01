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

package org.roboquant.xchange

import org.knowm.xchange.Exchange
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.junit.jupiter.api.Test
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
        System.getProperty("TEST_XCHANGE") ?: return
        assertFails {
            val exchange2 = getExchange()
            XChangeBroker(exchange2)
        }
    }

}