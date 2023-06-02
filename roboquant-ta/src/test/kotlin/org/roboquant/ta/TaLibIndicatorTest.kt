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
        val ind = TaLibIndicator() {
            mapOf(
                "rsi" to rsi(it, timePeriod = 14),
                "max" to max(it, timePeriod = 10)
            )
        }
        val feed = feed()
        val result = feed.apply(ind, feed.assets.first())
        assertEquals(2, result.size)
        assertContains(result, "rsi.test")
        assertContains(result, "max.test")
        assertEquals(feed.timeline.size - 14, result.values.first().size)
    }

    @Test
    fun predefined() {
        val feed = feed()
        val asset = feed.assets.first()
        assertDoesNotThrow {
            val map = feed.apply(TaLibIndicator.rsi(), asset)
            assertEquals(88, map.values.first().size)
        }
        assertDoesNotThrow {
            feed.apply(TaLibIndicator.bbands(), asset)
        }
        assertDoesNotThrow {
            feed.apply(TaLibIndicator.ema(), asset)
        }
        assertDoesNotThrow {
            feed.apply(TaLibIndicator.sma(), asset)
        }

        assertDoesNotThrow {
            feed.apply(TaLibIndicator.mfi(), asset)
        }

        assertDoesNotThrow {
            feed.apply(TaLibIndicator.stochastic(), asset)
        }

    }


}