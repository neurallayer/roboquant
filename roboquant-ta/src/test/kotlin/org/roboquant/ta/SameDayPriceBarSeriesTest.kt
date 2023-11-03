package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.days
import org.roboquant.common.millis
import org.roboquant.common.plus
import org.roboquant.feeds.PriceBar
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SameDayPriceBarSeriesTest {

    @Test
    fun sameDay() {

        val asset = Asset("DEMO")
        val pb = PriceBar(asset, 10, 11, 9, 10, 100)

        val pbs = SameDayPriceBarSeries(50)
        val now =  Instant.parse("2022-01-01T18:00:00Z")

        // Create same day events
        repeat(20) { pbs.add(pb, now + it.millis) }
        assertEquals(1, pbs.size)
        assertEquals(20*100.0, pbs.volume.last())

        // Create ne wday events
        repeat(20) {pbs.add(pb, now + (it + 1).days) }
        assertEquals(21, pbs.size)
    }


}
