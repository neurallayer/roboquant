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

package org.roboquant.feeds

import org.junit.jupiter.api.Test
import org.roboquant.common.Asset
import java.time.Instant
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
    fun order() {
        val now = Instant.now()
        val event = Event(emptyList(), now)
        val event2 = Event(emptyList(), now.plusMillis(1))
        assertTrue(event2 > event)
    }

}