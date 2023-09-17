package org.roboquant.brokers.sim.execution

import kotlin.test.*

internal class ViewTest {

    @Test
    fun basic() {
        val l = (1..100).toMutableList()
        val v = View(l)
        assertEquals(l.size, v.size)
        l.add(101)
        assertNotEquals(l.size, v.size)


    }

    @Test
    fun index() {
        val l = (100..199).toMutableList()
        val v = View(l)
        assertEquals(50, v.lastIndexOf(150))
        assertEquals(100, v.size)
        assertContains(v, 160)
        assertTrue(v.containsAll(listOf(101, 199)))
        assertFalse(v.containsAll(listOf(99, 199)))
    }

    @Test
    fun iter() {
        val l = (0..100).toMutableList()
        val view = View(l)

        var cnt = 0
        for (elem in view) {
            assertEquals(cnt, elem)
            cnt++
        }

        assertEquals(cnt, view.size)
    }


}
