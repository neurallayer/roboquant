package org.roboquant.strategies.utils


import kotlin.test.*
import kotlin.random.Random

internal class RSITest {


    @Test
    fun test() {
        val r = Random(42)
        repeat(10) {
            val rsi = RSI(100.0)
            assertFalse(rsi.isReady())
            repeat(50) {
                rsi.add(100.0 + r.nextDouble() - 0.5)
            }
            assertTrue(rsi.isReady())
            val v = rsi.calculate()
            assertTrue( v >= 0.0)
            assertTrue( v <= 100.0)
        }

    }
}