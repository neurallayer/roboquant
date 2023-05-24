/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.feeds.csv

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Asset
import org.roboquant.common.NoTradingException
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class CSVConfigTest {


    @Test
    fun defaultConfig() {
        val config = CSVConfig()
        assertEquals(Asset("TEMPLATE"), config.template)
        assertEquals(".csv", config.fileExtension)
        assertTrue(config.fileSkip.isEmpty())
        assertTrue(config.parsePattern.isEmpty())
        assertEquals(true, config.hasHeader)
        assertEquals(',', config.separator)
    }


    @Test
    fun basic() {
        val config = CSVConfig()
        config.fileExtension = ".csv"
        assertEquals(Asset("TEMPLATE"), config.template)
        assertEquals(Asset("ABN"), config.assetBuilder(File("ABN.csv")))
        assertFalse(config.shouldParse(File("some_non_existing_file.csv")))
        assertFalse(config.shouldParse(File("somefile.dummy_extension")))
    }


    @Test
    fun process() {
        val config = CSVConfig()
        val asset = Asset("ABC")
        config.configure(listOf("TIME", "OPEN", "HIGH", "LOW", "CLOSE", "VOLUME"))

        assertDoesNotThrow {
            config.processLine(listOf("2022-01-03", "10.0", "11.0", "9.00", "10.0", "100"), asset)
        }

        assertThrows<NoTradingException> {
            config.processLine(listOf("2022-01-01", "10.0", "11.0", "9.00", "10.0", "100"), asset)
        }

    }


}