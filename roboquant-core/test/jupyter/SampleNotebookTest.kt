package org.roboquant.jupyter

import org.jetbrains.jupyter.parser.JupyterParser
import org.jetbrains.jupyter.parser.notebook.CodeCell
import org.jetbrains.jupyter.parser.notebook.ExecuteResult
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals


abstract class RoboquantJupyterTest : JupyterReplTestCase(
    ReplProvider.forLibrariesTesting(listOf("dataframe"))
)



class SampleNotebookTest : RoboquantJupyterTest() {

    @Test
    fun testNotebooks() {
        System.getenv("TEST_NOTEBOOKS") ?: return
        // place holder to test a number of Jupyter notebooks
        // right now module kotlin-jupyter-test-kit difficult to load due dev dependencies
        // val files = listOf("/Users/peter/tmp/test.ipynb")// , "/Users/peter/tmp/visualization.ipynb")
        val files = listOf("/Users/peter/tmp/visualization.ipynb")
        for (file in files) validateNotebook(file)
    }

    /**
     * Execute the code cells in a notebook and validate the new output against the existing output in the notebook.
     * So it serves as a regression test if notebooks still produce the same output.
     *
     * All comparison is done between Strings, so make sure there is no random behavior in the output.
     */
    private fun validateNotebook(notebookPath: String) {
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