/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.jupyter

import org.jetbrains.jupyter.parser.JupyterParser
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.ExecuteResult
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.junit.jupiter.api.Test
import org.roboquant.common.Config
import org.roboquant.orders.Order
import java.io.File
import java.time.ZoneId
import java.util.regex.Pattern
import kotlin.random.Random
import kotlin.test.assertEquals

/**
 * Runs a number of notebooks and validates if the output is equal to the output as already available in the notebook.
 */
internal class SampleNotebookTest {

    private fun test(fileName: String) {
        Config.getProperty("FULL_COVERAGE") ?: return

        // Make sure to remove potential random behavior
        Config.random = Random(42L)
        Config.defaultZoneId = ZoneId.of("Europe/Amsterdam")
        Order.ID = 0
        Chart.counter = 0

        // Get the file and validate it
        val cl = this.javaClass.classLoader
        val file = File(cl.getResource("$fileName.ipynb")!!.file)
        val c = NotebookTester()
        c.validateNotebook(file)
    }

    @Test
    fun testFeedCharts() {
        test("feed_charts")
    }

    @Test
    fun testAccountCharts() {
        test("account_charts")
    }

    @Test
    fun testMetricCharts() {
        test("metric_charts")
    }

    @Test
    fun testMultiRunCharts() {
        test("multirun_charts")
    }

}

private class NotebookTester : JupyterReplTestCase(RoboquantReplProvider) {

    private val uuidPattern =
        Pattern.compile("[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?").toRegex()

    /**
     * Remove UUID from the output, so they still can be compared. UUID as used to generate unique ID for charts.
     */
    private fun String.removeUUID(): String {
        return replace(uuidPattern, "")
    }

    /**
     * Execute the code cells in a notebook and validate the new output against the existing output in the notebook.
     * So it serves as a regression test if notebooks still produce the same output.
     *
     * All comparison is done between Strings, so make sure there is no random behavior in the output.
     */
    fun validateNotebook(notebookFile: File) {
        val notebook = JupyterParser.parse(notebookFile)

        for (cell in notebook.cells.filterIsInstance<CodeCell>()) {
            val cellResult = exec(cell.source)
            val result = if (cellResult is MimeTypedResult) cellResult.entries.first().value else cellResult.toString()

            if (cell.outputs.isNotEmpty()) {
                val firstOutput = cell.outputs.first()
                if (firstOutput is ExecuteResult && firstOutput.data.isNotEmpty()) {
                    val output = firstOutput.data.entries.first()
                    assertEquals(output.value.removeUUID(), result.removeUUID())
                }
            }
        }

    }

}