package org.roboquant.feeds.csv

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Asset
import org.roboquant.common.NoTradingException
import java.io.File
import kotlin.test.*


internal class CSVConfigTest {



    @Test
    fun defaultConfig() {
        val config = CSVConfig()
        assertEquals(Asset("TEMPLATE"), config.template)
        assertEquals(".csv", config.fileExtension)
        assertTrue(config.fileSkip.isEmpty())
        assertTrue(config.parsePattern.isEmpty())
        // assertEquals("defaultBuilder", config.assetBuilder.name)
        assertEquals(false, config.priceAdjust)
    }

    @Test
    fun basic() {
        val config = CSVConfig()
        config.fileExtension = ".csv"
        assertEquals(Asset("TEMPLATE"), config.template)
        assertEquals(Asset("ABN"), config.assetBuilder(config, File("ABN.csv")))
        assertFalse(config.shouldParse(File("some_non_existing_file.csv")))
        assertFalse(config.shouldParse(File("somefile.dummy_extension")))
    }


    @Test
    fun process() {
        val config = CSVConfig()
        val asset = Asset("ABC")
        config.detectColumns(listOf("TIME", "OPEN", "HIGH", "LOW", "CLOSE", "VOLUME"))

        assertDoesNotThrow {
            config.processLine(asset, listOf("2022-01-03", "10.0", "11.0", "9.00", "10.0", "100"))
        }

        assertThrows<NoTradingException> {
            config.processLine(asset, listOf("2022-01-01", "10.0", "11.0", "9.00", "10.0", "100"))
        }

    }


}