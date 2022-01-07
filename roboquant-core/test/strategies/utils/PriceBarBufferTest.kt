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

package org.roboquant.strategies.utils

import org.junit.Test
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceBarBufferTest {

    @Test
    fun test() {
        val pb = PriceBarBuffer(20)
        assertFalse(pb.isAvailable())
        val feed = RandomWalk.lastYears()
        val asset = feed.assets.first()
        val data = feed.filter<PriceBar> { it.asset === asset }
        for (entry in data) {
            pb.update(entry.second, entry.first)
        }
        assertTrue(pb.isAvailable())
        assertTrue(pb.typical.isNotEmpty())
    }

}