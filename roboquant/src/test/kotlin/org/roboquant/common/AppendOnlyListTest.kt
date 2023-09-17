package org.roboquant.common

import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppendOnlyListTest {

    @Test
    fun basic() {
        val list = AppendOnlyList<String>()
        repeat(10) {
            list.add("$it")
        }
        assertEquals(10, list.size)

    }

    @Test
    fun capacity() {
        val list = AppendOnlyList<String>(100)
        repeat(10) {
            list.add("$it")
        }
        assertEquals(10, list.size)
    }

    @Test
    fun threadSafe() {
        val list = AppendOnlyList<String>()
        val threads = mutableListOf<Thread>()
        val nThreads = 50
        val loops = 25
        repeat(nThreads) {
            val t = thread(true) {
                assertDoesNotThrow {
                    repeat(loops) {
                        list.add("test")
                        Thread.sleep(1)
                        val view = list.view()
                        list.addAll(listOf("test", "test"))
                        assertTrue(list.size > view.size)
                        Thread.sleep(1)

                        // Validate the iterator is thread safe
                        for (elem in list) {
                            assertEquals("test", elem)
                        }
                        Thread.sleep(1)

                        // Validate the sublist is thread safe
                        if (list.size > 10) {
                            val subList = list.subList(0, 10)
                            for (elem in subList) {
                                assertEquals("test", elem)
                            }
                            assertEquals(10, subList.size)
                        }
                        Thread.sleep(1)
                    }
                }
            }
            threads.add(t)
        }
        assertEquals(nThreads, threads.size)
        for (t in threads) t.join()
        assertEquals(nThreads * loops * 3, list.size)
    }


}
