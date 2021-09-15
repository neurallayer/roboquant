package org.roboquant


import org.roboquant.common.Config
import kotlin.test.*

internal class ConfigTest {


    @Test
    fun test() {
        val version = Config.version
        assertTrue(version.isNotBlank())
        Config.info()

        assertFalse(Config.lowMemory)
    }

}