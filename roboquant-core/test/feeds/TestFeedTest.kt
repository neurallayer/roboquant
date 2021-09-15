package org.roboquant.feeds

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.test.*
import org.roboquant.TestData
import org.roboquant.common.Background
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.test.TestFeed

fun play(feed:Feed, timeFrame: TimeFrame = TimeFrame.FULL): EventChannel {
    val channel = EventChannel(timeFrame = timeFrame)

    Background.ioJob {
        feed.play(channel)
        channel.close()
    }
    return channel
}

internal class TestFeedTest {

    @Test
    fun testTestFeed() = runBlocking{
        val feed = TestFeed(5..9)
        var cnt = 0
        for (step in play(feed)) {
            cnt++
        }
        assertEquals(5, cnt)
    }

    @Test
    fun testTestFeedWithItems() = runBlocking{
        val feed = TestFeed(120..130, 130 downTo 120, asset = TestData.euStock())
        var cnt = 0
        for (step in play(feed)) {
            cnt++
            assertTrue(step.actions.first() is PriceAction)
        }
        assertEquals(22, cnt)
    }

}