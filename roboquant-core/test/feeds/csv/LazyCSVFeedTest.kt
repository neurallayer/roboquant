package org.roboquant.feeds.csv

import kotlinx.coroutines.runBlocking
import java.time.Instant
import org.junit.Test
import kotlin.test.assertTrue


internal class LazyCSVFeedTest {

    @Test
    fun play() {
        val feed = LazyCSVFeed("../data/US")
        var past = Instant.MIN
        runBlocking {
            for (event in org.roboquant.feeds.play(feed)) {
                assertTrue(event.now > past)
                past = event.now
            }
        }
    }

}