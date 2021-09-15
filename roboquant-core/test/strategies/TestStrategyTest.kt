package org.roboquant.strategies


import kotlin.test.*
import org.roboquant.TestData

internal class TestStrategyTest {


    @Test
    fun test() {
        val strategy = TestStrategy(5)
        val event = TestData.event()
        var signals = strategy.generate(event)
        assertFalse(signals.isEmpty())
        signals = strategy.generate(event)
        assertTrue(signals.isEmpty())
    }
}