package org.roboquant.common

import org.junit.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun testDouble() {
        val a = doubleArrayOf(1.0, 2.0, 3.0, 2.0)
        assertEquals(1.5, a.kurtosis())
        assertEquals(2.0, a.mean())
        assertEquals(0.816496580927726, a.std())
        assertEquals(0.0, a.skewness())
        assertEquals(0.6666666666666666, a.variance())

        assertEquals(1.0, (a/2.0).mean())
    }


    @Test
    fun testInstant() {

    }



}