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

package org.roboquant.feeds

import org.roboquant.common.ParallelJobs

/**
 * Combines several live feeds into a single new feed. It assumes the feeds are delivering
 * the events in the right order. If any feed sends an event past the timeframe as configured
 * by the channel, the channel closes for all feeds.
 *
 * @property feeds
 * @constructor Create a new Combined Live Feed
 */
class CombinedLiveFeed(vararg val feeds: LiveFeed) : Feed {

    /**
     * @see Feed.play
     */
    override suspend fun play(channel: EventChannel) {
        val jobs = ParallelJobs()
        for (feed in feeds) {
            jobs.add {
                feed.play(channel)
            }
        }
        jobs.joinAll()
    }

    /**
     * Close all underlying [feeds]
     */
    override fun close() {
        feeds.forEach { it.close() }
    }

}

