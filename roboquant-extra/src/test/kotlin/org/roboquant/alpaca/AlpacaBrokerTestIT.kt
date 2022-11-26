/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.alpaca

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.common.Config
import org.roboquant.feeds.Event
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlpacaBrokerTestIT {

    @Test
    fun test() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val broker = AlpacaBroker()
        val cash = broker.account.cash

        assertTrue(cash.isNotEmpty())
        assertFalse(cash.isMultiCurrency())
        assertTrue(broker.availableAssets.isNotEmpty())
        println(broker.account.fullSummary())

        assertDoesNotThrow {
            broker.place(emptyList(), Event.empty())
        }

        assertDoesNotThrow {
            broker.sync()
            val account = broker.account
            broker.account.fullSummary()
            assertTrue(account.equity.isNotEmpty())
        }

    }

}