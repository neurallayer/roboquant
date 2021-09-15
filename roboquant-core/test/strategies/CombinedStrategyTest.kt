package org.roboquant.strategies


import org.junit.Test
import kotlin.test.assertEquals


class CombinedStrategyTest {

    @Test
    fun test() {
        val s1 = EMACrossover.shortTerm()
        val s2 = EMACrossover.midTerm()

        val s = CombinedStrategy(s1, s2)
        assertEquals(2, s.strategies.size)
    }

}