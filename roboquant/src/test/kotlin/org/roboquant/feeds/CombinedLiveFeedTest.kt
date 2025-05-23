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

package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.roboquant.feeds.util.LiveTestFeed
import org.roboquant.feeds.util.play
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CombinedLiveFeedTest {

    @Test
    fun testCombinedFeed() = runBlocking {
        val f1 = LiveTestFeed(10..19, delayInMillis = 1)
        val f2 = LiveTestFeed(60..69, delayInMillis = 1)
        val cf = CombinedLiveFeed(f1, f2)
        var cnt = 0
        for (step in play(cf)) {
            assertTrue(step.items.isNotEmpty())
            cnt++
        }
        assertEquals(20, cnt)
    }

    @Test
    fun testCombinedFeed3() = runBlocking {
        val f1 = LiveTestFeed(10..19, delayInMillis = 1)
        val f2 = LiveTestFeed(60..69, delayInMillis = 1)
        val cf = CombinedFeed(f1, f2)
        var cnt = 0
        for (step in play(cf)) {
            assertTrue(step.items.isNotEmpty())
            cnt++
        }
        assertEquals(20, cnt)
    }


}
