/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.strategies

import org.roboquant.common.Currency
import org.roboquant.common.Signal
import org.roboquant.common.Stock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SignalTest {

    @Test
    fun basic() {
        val c = Stock("AAPL", Currency.USD)
        val s = Signal.buy(c)

        assertEquals(s.asset, c)

        assertEquals(1.0, s.rating)

        val s2 = Signal.sell(c)
        assertTrue(s2.conflicts(s))
        assertTrue(!s2.conflicts(s2))

    }

}
