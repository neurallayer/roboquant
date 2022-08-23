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
import kotlin.random.Random
import kotlin.test.assertEquals

/**
 * Runs a number of notebooks and validates if the output is equal to the output as already available in the notebook.
 */
internal class SampleNotebookTest {


    private fun test(file: String) {
        Config.getProperty("FULL_COVERAGE") ?: return

        // Make sure to remove potential random behavior
        Config.random = Random(42L)
        Config.defaultZoneId = ZoneId.of("Europe/Amsterdam")
        Order.ID = 0

        // Get the file and validate it
        val path = "./roboquant-jupyter/notebooks/$file.ipynb"
        val c = NotebookTester()
        c.validateNotebook(path)
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
    fun testMultirunCharts() {
        test("multirun_charts")
    }

}


private class NotebookTester : JupyterReplTestCase(RoboquantReplProvider) {

    /**
     * Execute the code cells in a notebook and validate the new output against the existing output in the notebook.
     * So it serves as a regression test if notebooks still produce the same output.
     *
     * All comparison is done between Strings, so make sure there is no random behavior in the output.
     */
    fun validateNotebook(notebookPath: String) {
        val notebookFile = File(notebookPath)
        val notebook = JupyterParser.parse(notebookFile)

        for (cell in notebook.cells.filterIsInstance<CodeCell>()) {
            val cellResult = exec(cell.source)
            val result = if (cellResult is MimeTypedResult) cellResult.entries.first().value else cellResult.toString()

            if (cell.outputs.isNotEmpty()) {
                val firstOutput = cell.outputs.first()
                if (firstOutput is ExecuteResult && firstOutput.data.isNotEmpty()) {
                    val value = firstOutput.data.entries.first()
                    assertEquals(value.value, result)
                }
            }
        }

    }

}