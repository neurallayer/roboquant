package org.roboquant.strategies.utils

import java.time.Instant
import kotlin.test.*

internal class MovingTimedWindowTest {

    @Test
    fun test() {
        val w = MovingTimedWindow(10)
        repeat(30) {
            w.add(1.0, Instant.now())
        }
        assertTrue(w.isAvailable())
        assertEquals(10, w.toDoubleArray().size)
        assertEquals(10, w.windowSize)
        assertEquals(10, w.toLongArray().size)
    }

}