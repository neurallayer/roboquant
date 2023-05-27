package org.roboquant.ta

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.common.Asset
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.metrics.apply
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TaLibIndicatorTest {

    private fun feed(): HistoricFeed {
        return HistoricTestFeed(90..110, 110 downTo 80, 80..125, priceBar = true, asset = Asset("TEST"))
    }

    @Test
    fun basic() {
        val ind = TaLibIndicator(15) {
            mapOf(
                "rsi" to rsi(it, timePeriod = 14),
                "max" to max(it, timePeriod = 10)
            )
        }
        val feed = feed()
        val result = feed.apply(feed.assets.first(), ind)
        assertEquals(2, result.size)
        assertContains(result, "rsi.test")
        assertContains(result, "max.test")
        assertEquals(feed.timeline.size - 14, result.values.first().size)
    }

    @Test
    fun predefined() {
        val feed = feed()
        assertDoesNotThrow {
            feed.apply(feed.assets.first(), TaLibIndicator.rsi())
        }
        assertDoesNotThrow {
            feed.apply(feed.assets.first(), TaLibIndicator.bbands())
        }
        assertDoesNotThrow {
            feed.apply(feed.assets.first(), TaLibIndicator.ema())
        }
        assertDoesNotThrow {
            feed.apply(feed.assets.first(), TaLibIndicator.sma())
        }
    }


}