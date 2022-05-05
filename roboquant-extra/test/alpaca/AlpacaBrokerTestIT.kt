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

package org.roboquant.alpaca


import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.brokers.summary
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import java.util.logging.Level
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlpacaBrokerTestIT {


    @Test
    fun test() {
        Config.getProperty("FULL_COVERAGE") ?: return
        Logging.setLevel(Level.FINE)
        val broker = AlpacaBroker()

        assertTrue(broker.account.cash.isNotEmpty())
        assertFalse(broker.account.cash.isMultiCurrency())
        assertTrue(broker.availableAssets.isNotEmpty())

        assertDoesNotThrow {
            broker.place(emptyList(), Event.empty())
        }
        broker.account.trades.summary().print()
    }

}