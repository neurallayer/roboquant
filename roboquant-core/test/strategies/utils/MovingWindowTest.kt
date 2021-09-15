package org.roboquant.strategies.utils


import kotlin.test.*

internal class MovingWindowTest {

    @Test
    fun test() {
        val buffer = MovingWindow(100)
        repeat(90) { buffer.add(1.0) }
        assertFalse(buffer.isAvailable())

        repeat(10) { buffer.add(1.0) }
        assertFalse(buffer.isAvailable())

        val d = buffer.toDoubleArray()
        assertEquals(100, d.size)
    }

}