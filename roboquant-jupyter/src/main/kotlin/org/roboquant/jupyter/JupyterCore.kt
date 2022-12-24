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

import org.apache.commons.text.StringEscapeUtils
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.ThrowableRenderer
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Make exceptions a bit nicer to deal with in notebooks
 */
internal class RoboquantThrowableRenderer : ThrowableRenderer {

    override fun accepts(throwable: Throwable): Boolean {
        return true
    }

    private fun String?.escapeHtml(): String = StringEscapeUtils.escapeHtml4(this ?: "")

    override fun render(throwable: Throwable): Any {
        val output = StringWriter()
        PrintWriter(output).use { throwable.printStackTrace(it) }
        val result = """
           <details class="jp-RenderedText" data-mime-type="application/vnd.jupyter.stderr">
                <summary>${throwable.message.escapeHtml()}</summary>
                <pre style="background: transparent;">${output.toString().escapeHtml()}</pre>         
           </details>      
        """.trimIndent()
        return HTML(result, Output.isolation)
    }

}

/**
 * Integration with Kotlin based Jupyter notebook kernels. Some main features:
 *
 * 1) Support for charts using the Apache ECharts library
 * 2) Default imports
 * 3) Nicer exception handling
 */
internal class JupyterCore : JupyterIntegration() {

    companion object {
        private val outputs = CopyOnWriteArrayList<Output>()
        internal fun addOutput(output: Output) = outputs.add(output)
    }

    override fun Builder.onLoaded() {

        onLoaded {
            addThrowableRenderer(RoboquantThrowableRenderer())
            execute("%logLevel warn")
        }

        /**
         * The resources that need to be loaded.
         */
        resources {
            js("echarts") {
                classPath("js/echarts.min.js")
            }
        }

        beforeCellExecution {
            outputs.clear()
        }

        afterCellExecution { _, _ ->
            outputs.forEach {
                this.display(it, null)
            }
            outputs.clear()
        }

    }

}


