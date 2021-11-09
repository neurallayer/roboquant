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

import org.junit.Test
import kotlin.test.*
import org.roboquant.TestData
import org.roboquant.common.Asset
import java.time.Instant

internal class EventTest {


    @Test
    fun basic() {
        val now = Instant.now()
        val event = Event(listOf(), now)
        assertTrue(event.prices.isEmpty())
        val asset = Asset("Dummy")
        assertTrue(event.getPrice(asset) == null)
        assertTrue(event.actions.isEmpty())
    }

    @Test
    fun merge() {
        val now = Instant.now()
        val c1 = listOf(Event(listOf(), now))
        val c2 = listOf(Event(listOf(), now.plusMillis(1)))

        val s1 = c1.merge(c1)
        assert(s1.size == 1)

        val s2 = c1.merge(c2)
        assert(s2.size == 2)
    }
}