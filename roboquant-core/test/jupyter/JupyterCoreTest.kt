package org.roboquant.jupyter

import org.junit.Test
import org.roboquant.common.Logging
import java.util.logging.Level

internal class JupyterCoreTest {

    @Test
    fun test() {
        JupyterCore()
        Output.classic(true)
        Output.lab()
    }

    @Test
    fun logger() {
        val jupyterLogger = JupyterLogger()
        jupyterLogger.level = Level.WARNING
        Logging.resetHandler(jupyterLogger)

        val logger = Logging.getLogger("test")
        logger.info("Should not show up")
        logger.warning("Should show up")

    }

}