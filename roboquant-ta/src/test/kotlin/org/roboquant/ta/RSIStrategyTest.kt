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

package org.roboquant.ta

import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RSIStrategyTest {
    private val feed = RandomWalk.lastYears(1, 2)

    @Test
    fun test() = runBlocking {
        val s = RSIStrategy()
        assertEquals(30.0, s.lowThreshold)
        assertEquals(70.0, s.highThreshold)
        val roboquant = Roboquant(s)
        roboquant.run(feed)

    }

}
