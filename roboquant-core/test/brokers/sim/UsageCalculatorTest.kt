package org.roboquant.brokers.sim

import kotlin.test.Test
import kotlin.test.assertTrue

internal class  UsageCalculatorTest {

    @Test
    fun test() {
        val uc = BasicUsageCalculator()
        val result = uc.calculate(listOf())
        assertTrue(result.isEmpty())
    }

    @Test
    fun test2() {
        val uc = RegTCalculator()
        val result = uc.calculate(listOf())
        assertTrue(result.isEmpty())
    }

    @Test
    fun test3() {
        val uc = LeveragedUsageCalculator()
        val result = uc.calculate(listOf())
        assertTrue(result.isEmpty())
    }
}