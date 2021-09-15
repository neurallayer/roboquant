package org.roboquant.strategies.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PairBufferTest {

    @Test
    fun test() {
        val pb = PairBuffer(20)
        for (i in 1..25) {
            val d = i.toDouble()
            pb.add(d * 2.0 , d)
        }
        assertTrue(pb.isAvailable())
        // assertEquals(1.0, pb.covariance().absoluteValue)
        assertEquals(1.0, pb.beta())
    }

}