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

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.alpaca.AlpacaBroker
import org.roboquant.common.Config
import org.roboquant.feeds.Event
import kotlin.test.Test
import kotlin.test.assertTrue

internal class OANDABrokerTestIT {


    @Test
    fun test() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val broker = AlpacaBroker()
        val account = broker.account
        account.fullSummary()
        assertTrue(account.buyingPower > 0)

        assertDoesNotThrow {
            broker.place(emptyList(), Event.empty())
        }
    }
}