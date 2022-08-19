package org.roboquant.polygon

import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

internal class PolygonTest {

    @Test
    fun testHistoricFeed() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = PolygonHistoricFeed()
        val now = Instant.now() - 10.days
        val period = Timeframe(now - 50.days, now)
        feed.retrieve("IBM", period)
        assertTrue(feed.timeline.isNotEmpty())
        assertContains(feed.assets.map { it.symbol }, "IBM")
    }
}