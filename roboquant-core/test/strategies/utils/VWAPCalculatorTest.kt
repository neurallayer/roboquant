package org.roboquant.strategies.utils

import org.roboquant.TestData.priceBar
import kotlin.test.*

internal class VWAPCalculatorTest {

    @Test
    fun test() {
        val c = VWAPCalculator(20)
        assertFalse(c.isReady())

        for (i in 1..30) c.add(priceBar())
        assertTrue(c.isReady())

        assertEquals(priceBar().getPrice("TYPICAL"), c.calc())
    }

}