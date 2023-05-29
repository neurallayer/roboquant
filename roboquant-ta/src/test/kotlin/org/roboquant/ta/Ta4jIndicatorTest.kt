package org.roboquant.ta

import org.junit.jupiter.api.Test
import org.roboquant.common.Asset
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.metrics.apply
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedUpIndicatorRule
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Ta4jIndicatorTest {

    private fun feed(): HistoricFeed {
        return HistoricTestFeed(90..110, 110 downTo 80, 80..125, priceBar = true, asset = Asset("TEST"))
    }

    @Test
    fun basic() {
        val ind = Ta4jIndicator(30) {
            val closePrice = ClosePriceIndicator(it)
            val shortSma = SMAIndicator(closePrice, 20)
            val longSma = SMAIndicator(closePrice, 30)
            val isTrue = CrossedUpIndicatorRule(shortSma, longSma).isSatisfied(it.endIndex)
            val value = if (isTrue) 1.0 else -1.0
            mapOf("crossup" to value)
        }
        val feed = feed()
        val result = feed.apply(ind, feed.assets.first())
        assertEquals(1, result.size)
        assertEquals("crossup.test", result.keys.first())
        assertEquals(feed.timeline.size, result.values.first().size)
        assertTrue(result.values.all { it.values.all { value -> value == 1.0 || value == -1.0 } })
    }


}