package org.roboquant.logging

import kotlin.test.*
import org.roboquant.TestData
import kotlin.test.assertEquals

internal class SilentLoggerTest {

    @Test
    fun silentLogger() {
        val logger = SilentLogger()
        logger.log(TestData.getMetrics(), TestData.getRunInfo())
        assertEquals(1, logger.events)

        logger.log(TestData.getMetrics(), TestData.getRunInfo())
        assertEquals(2, logger.events)

        logger.reset()
        assertEquals(0L,logger.events)
    }
}