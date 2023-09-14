/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.feeds

import org.junit.jupiter.api.Test
import org.roboquant.common.Asset
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class EventTest {

    @Test
    fun basic() {
        val now = Instant.now()
        val event = Event(emptyList(), now)
        assertTrue(event.prices.isEmpty())
        val asset = Asset("Dummy")
        assertTrue(event.getPrice(asset) == null)
        assertTrue(event.actions.isEmpty())
    }

    @Test
    fun comparison() {
        val now = Instant.now()
        val event = Event(emptyList(), now)
        val event2 = Event(emptyList(), now.plusMillis(1))
        assertTrue(event2 > event)
    }

    @Test
    fun empty() {
        val t = Instant.now()
        val event = Event.empty(t)
        assertTrue(event.actions.isEmpty())
    }

    @Test
    fun prices() {
        val t = Instant.now()
        val event = Event(
            listOf(TradePrice(Asset("ABC"), 100.0), TradePrice(Asset("CDE"), 50.0)),
            t
        )
        assertTrue(event.actions.isNotEmpty())
        assertEquals(2, event.prices.size)
        assertEquals(100.0, event.getPrice(Asset("ABC")))
    }


}
