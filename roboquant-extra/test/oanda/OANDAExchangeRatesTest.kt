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

package org.roboquant.oanda

import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Logging
import org.roboquant.common.USD
import java.time.Instant
import java.util.logging.Level
import kotlin.test.Test
import kotlin.test.assertTrue

internal class OANDAExchangeRatesTest {

    @Test
    fun test() {
        System.getProperty("TEST_OANDA") ?: return
        Logging.setLevel(Level.FINER)
        val c = OANDAExchangeRates.allAvailableAssets()
        var r = c.getRate(100.USD, EUR, Instant.now())
        assertTrue(r.isFinite())

        c.refresh()
        r = c.getRate(100.USD, EUR, Instant.now())
        assertTrue(r.isFinite())
    }
}