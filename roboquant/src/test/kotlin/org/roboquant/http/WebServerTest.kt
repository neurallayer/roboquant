package org.roboquant.http

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.Roboquant
import org.roboquant.common.Timeframe
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.loggers.SilentLogger
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test

class WebServerTest {

    @Test
    fun basic()  {
        val feed = RandomWalkFeed(Timeframe.fromYears(2000, 2001))
        val rq = Roboquant(EMAStrategy(), logger = SilentLogger())

        assertDoesNotThrow {
            runBlocking {
                val ws = WebServer()
                ws.runAsync(rq, feed, feed.timeframe)
                ws.start()
                ws.stop()
            }
        }
    }

}