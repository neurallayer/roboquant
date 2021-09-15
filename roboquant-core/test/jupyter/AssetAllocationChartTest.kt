package org.roboquant.jupyter

import org.junit.Test
import org.roboquant.brokers.Account
import kotlin.test.*

internal class AssetAllocationChartTest {

    @Test
    fun test() {
        val account = Account()
        val chart = AssetAllocationChart(account)
        val str = chart.toString()
        assertTrue(str.isNotEmpty())
    }

}