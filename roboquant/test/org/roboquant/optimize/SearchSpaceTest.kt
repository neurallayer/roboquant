package org.roboquant.optimize

import org.junit.jupiter.api.assertThrows
import java.lang.Exception
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SearchSpaceTest {

    @Test
    fun randomSpace() {
        val space = RandomSearch(10)
        space.add("p1", 0..10)
        space.add("p2", 5..20)
        var n = 0
        for (param in space) {
            assertTrue(param.getInt("p1") in 0..10)
            assertTrue(param.getInt("p2") in 5..20)
            assertThrows<Exception> {
                param.getString("p1")
            }
            assertThrows<Exception> {
                param.getDouble("p1")
            }
            assertThrows<Exception> {
                param.getInt("p99")
            }
            n++
        }
        assertEquals(10, n)

    }

    @Test
    fun gridSpace() {
        val space = GridSearch()
        space.add("p1", 0..10)
        space.add("p2", 5..20)
        var n = 0
        for (param in space) {
            assertTrue(param.getInt("p1") in 0..10)
            assertTrue(param.getInt("p2") in 5..20)
            n++
        }
        assertEquals(11 * 16, n)
    }


}
