package org.roboquant.jupyter

import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.Renderable
import java.io.File

/**
 * Takes care of difference between Notebook and Lab behavior
 *
 */
abstract class Output : Renderable {

    enum class Mode {
        LAB,
        NOTEBOOK
    }

    companion object {
        var mode = autoDetectMode()
        var useCDN = true

        /**
         * Set the output to classic Jupyter Notebooks.
         *
         * @param useCDN
         */
        fun notebook(useCDN: Boolean = true) {
            mode = Mode.NOTEBOOK
            Output.useCDN = useCDN
        }

        /**
         * Quick hack to Auto detect mode. Only works with JDK 9 and higher.
         *
         * @return
         */
        private fun autoDetectMode() : Mode {
            try {
                for (process in ProcessHandle.allProcesses()) {
                    val line = process.info().commandLine().toString()
                    if (line.contains("jupyter-lab")) return Mode.LAB
                }
            } catch (e:Exception) {
                // ignore
            }
            return Mode.NOTEBOOK
        }


        /**
         * Set the output to Jupyter Lab format.
         *
         */
        fun lab() {
            mode = Mode.LAB
        }
    }

    override fun render(notebook: Notebook): DisplayResult {
        return when (mode) {
            Mode.LAB -> HTML(asHTML())
            Mode.NOTEBOOK -> HTML(asHTMLPage(), true)
        }
    }

    fun render() {
        JupyterCore.render(this)
    }

    /**
     * Save output to an HTML file.
     *
     * @param filename
     */
    fun toHTMLFile(filename: String, useCDN: Boolean = false) {
        val content = asHTMLPage(useCDN)
        val f = File(filename)
        f.writeText(content)
    }


    /**
     * Generate an HTML snippet. Subclasses will need to implement this method. This is used in Jupyter-Lab environments
     * that can directly insert HTML and JavaScript content in the output of a cell.
     *
     * @return
     */
    abstract fun asHTML(): String

    /**
     * Generate a whole HTML page. Subclasses will need to implement this method. This is used in Jupyter-Notebook
     * environments that put the output of a cell in an iFrame.
     *
     * @param useCDN Should a CDN/link be used for static content or should the content be embedded
     * @return
     */
    abstract fun asHTMLPage(useCDN: Boolean = Output.useCDN): String
}