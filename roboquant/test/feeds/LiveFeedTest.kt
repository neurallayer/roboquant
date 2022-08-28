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

package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.roboquant.common.Background
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LiveFeedTest {

    private class MyLiveFeed : LiveFeed()

    @Test
    fun basic(): Unit = runBlocking {
        val feed = MyLiveFeed()
        assertFalse(feed.isActive)
        feed.heartbeatInterval = 10
        Background.ioJob {
            feed.play(EventChannel())
            assertTrue(feed.isActive)
        }
    }

    @Test
    fun combined() = runBlocking {
        val feed1 = MyLiveFeed()
        feed1.heartbeatInterval = 2
        val feed2 = MyLiveFeed()
        feed2.heartbeatInterval = 2
        val feed = CombinedFeed(feed1, feed2)
        Background.ioJob { feed.play(EventChannel()) }
        feed.close()
    }

}