/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ibkr

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Config
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Currency
import org.roboquant.common.EUR
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

internal class IBKRBrokerTestIT {

    private val broker by lazy { IBKRBroker() }

    @Test
    fun wrongConfig() {
        Config.getProperty("test.ibkr") ?: return
        assertThrows<ConfigurationException> {
            IBKRBroker {
                port = 87654
            }
        }

    }

    @Test
    fun test() {
        Config.getProperty("test.ibkr") ?: return
        val past = Instant.now()
        val account = broker.sync()
        println(account)
        println(account.positions)
        assertTrue(account.lastUpdate >= past)
        broker.disconnect()
    }

    @Test
    fun exchangeRates() {
        Config.getProperty("test.ibkr") ?: return
        val er = broker.exchangeRates
        val amount = 1.EUR
        assertDoesNotThrow {
            er.convert(amount, Currency.USD, Instant.now())
        }
    }

}
