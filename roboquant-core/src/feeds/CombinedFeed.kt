package org.roboquant.feeds

import kotlinx.coroutines.Job
import org.roboquant.common.Background

/**
 * Combines several live feeds into a single new feed. It assumes the feeds are delivering
 * the events in the right order. If any feed send an event past the timeframe as configured
 * by the channel, the channel closes for all feeds.
 *
 * @property feeds
 * @constructor Create empty Relay channel
 */
class CombinedFeed(vararg val feeds: LiveFeed) : Feed {

    override suspend fun play(channel: EventChannel) {
        val jobs = mutableListOf<Job>()
        for (feed in feeds) {
            val job = Background.ioJob {
                feed.play(channel)
            }
            jobs.add(job)
        }
        jobs.forEach { it.join() }
    }

}