package org.roboquant.logging

import org.roboquant.Phase
import org.roboquant.TestData
import java.util.logging.Level
import kotlin.test.*

internal class InfoLoggerTest {

    @Test
    fun test() {
        val metrics = TestData.getMetrics()

        val logger = InfoLogger()
        logger.log(metrics, TestData.getRunInfo())
        logger.end(Phase.VALIDATE)
        assertTrue(logger.getMetrics().isEmpty())

        val logger2 = InfoLogger(splitMetrics = true, level = Level.WARNING)
        logger2.log(metrics, TestData.getRunInfo())
        logger2.end(Phase.VALIDATE)
        assertTrue(logger2.getMetrics().isEmpty())
    }

}