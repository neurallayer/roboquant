package org.roboquant.logging

import org.junit.Test
import org.roboquant.TestData

internal class ConsoleLoggerTest {

    @Test
    fun consoleLogger() {
        val logger = ConsoleLogger()

        logger.log(TestData.getMetrics(), TestData.getRunInfo())

    }

}