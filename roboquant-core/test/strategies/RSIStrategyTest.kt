package org.roboquant.strategies

import org.junit.Test
import kotlin.test.assertEquals


internal class RSIStrategyTest {

    @Test
    fun test() {
        val s = RSIStrategy()
        assertEquals(30.0, s.lowThreshold)
        assertEquals(70.0, s.highThreshold)
    }

}