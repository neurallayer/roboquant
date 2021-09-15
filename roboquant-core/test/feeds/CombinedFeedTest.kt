package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.roboquant.feeds.test.TestFeed
import org.junit.Test
import kotlin.test.assertEquals

internal class CombinedFeedTest {

    @Test
    fun testCombinedFeed() = runBlocking{
        val f1 = TestFeed(10..19)
        val f2 = TestFeed(60..69)
        val cf = CombinedFeed(f1, f2)
        var cnt = 0
        for (step in play(cf)) {
            cnt++
        }
        assertEquals(20, cnt)
    }


}