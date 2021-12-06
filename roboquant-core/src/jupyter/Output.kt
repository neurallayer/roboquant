/*
 * Copyright 2021 Neural Layer
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
        CLASSIC
    }

    companion object {
        var mode = autoDetectMode()
        var useCDN = true

        /**
         * Set the output to classic Jupyter Notebooks.
         *
         * @param useCDN
         */
        fun classic(useCDN: Boolean = true) {
            mode = Mode.CLASSIC
            Output.useCDN = useCDN
        }

        /**
         * Quick hack to Auto detect mode. Only works with JDK 9 and higher.
         *
         * @return
         */
        private fun autoDetectMode(): Mode {
            try {
                for (process in ProcessHandle.allProcesses()) {
                    val line = process.info().commandLine().toString()
                    if (line.contains("jupyter") && line.contains("lab")) return Mode.LAB
                }
            } catch (e: Exception) {
                // ignore
            }
            return Mode.CLASSIC
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
            Mode.CLASSIC -> HTML(asHTMLPage(), true)
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