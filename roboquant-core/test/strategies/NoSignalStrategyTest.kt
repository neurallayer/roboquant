package org.roboquant.strategies


import kotlin.test.*
import org.roboquant.TestData

internal class NoSignalStrategyTest {

    @Test
    fun test() {
        val strategy = NoSignalStrategy()
        val event = TestData.event()
        val signals = strategy.generate(event)
        assertTrue(signals.isEmpty())
    }


}