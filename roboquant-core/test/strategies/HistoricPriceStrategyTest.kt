package org.roboquant.strategies

import org.roboquant.common.Asset
import kotlin.test.*


internal class HistoricPriceStrategyTest {

    private class SubClass : HistoricPriceStrategy(10) {
        override fun generate(asset: Asset, data: DoubleArray): Signal? {
            return null
        }

    }

    @Test
    fun test() {
        val c = SubClass()
        assertEquals(10, c.period)
        assertFalse(c.useReturns)
    }

}