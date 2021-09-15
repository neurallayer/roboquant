package org.roboquant.common

import org.junit.Test
import java.util.logging.Level
import kotlin.test.*

class LoggingTest {


    @Test
    fun test() {
        val logger = Logging.getLogger("test")
        assertEquals("test", logger.name)
        assertEquals(Level.INFO, logger.level)

        Logging.setLevel(Level.WARNING)
        assertEquals(Level.WARNING, logger.level)
        Logging.setLevel(Level.WARNING)

        Logging.setDefaultLevel(Level.FINE)
        val logger2 = Logging.getLogger("test")
        assertEquals(Level.FINE, logger2.level)
        Logging.setDefaultLevel(Level.INFO)
    }

}