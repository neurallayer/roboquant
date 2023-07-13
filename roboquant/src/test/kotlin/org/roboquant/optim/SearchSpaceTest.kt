package org.roboquant.optim

import org.roboquant.common.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchSpaceTest {

    @Test
    fun random() {
        val space = RandomSearch(100)
        space.add("p1", listOf("a", "b", "c"))
        space.add("p2", 1..100)
        space.add("p3") {
            Config.random.nextInt()
        }

        val params = space.materialize()
        assertEquals(100, params.size)
        assertTrue(params.all { it.contains("p1") })
        assertTrue(params.all { it.contains("p2") })
        assertTrue(params.all { it.contains("p3") })
    }

    @Test
    fun grid() {
        val space = GridSearch()
        space.add("p1", listOf("a", "b", "c"))
        space.add("p2", 1..100)
        space.add("p3", 100) {
            Config.random.nextInt()
        }

        val params = space.materialize()
        assertEquals(3*100*100, params.size)
        assertEquals(100*100, params.filter{
            it.getString("p1") == "a"
        }.size)

        assertTrue(params.all { it.contains("p1") })
        assertTrue(params.all { it.contains("p2") })
        assertTrue(params.all { it.contains("p3") })
    }

    @Test
    fun empty() {
        val space = EmptySearchSpace()
        assertEquals(1, space.size)
        assertEquals(1, space.materialize().toList().size)
    }


}