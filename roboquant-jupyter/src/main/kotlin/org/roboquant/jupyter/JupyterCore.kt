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

    @Suppress("ComplexCondition")
    private fun String?.escapeHtml(): String {
        if (this == null) return ""
        val str = this
        return buildString {
            for (c in str) {
                if (c.code > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                    append("&#")
                    append(c.code)
                    append(';')
                } else {
                    append(c)
                }
            }
        }
    }

    override fun render(throwable: Throwable): Any {
        val output = StringWriter()
        PrintWriter(output).use { throwable.printStackTrace(it) }
        val result = """
           <details class="jp-RenderedText" data-mime-type="application/vnd.jupyter.stderr">
                <summary>${throwable.message.escapeHtml()}</summary>
                <pre style="background: transparent;">${output.toString().escapeHtml()}</pre>         
           </details>      
        """.trimIndent()
        return HTML(result, JupyterCore.isolation)
    }

}

/**
 * Should all HTML output be rendered in legacy mode (iFrames), default is false
 */
var legacyNotebookMode: Boolean
    get() = JupyterCore.isolation
    set(value) { JupyterCore.isolation = value}

/**
 * Integration with Kotlin based Jupyter notebook kernels. Some main features:
 *
 * 1) Support for charts using the Apache ECharts library
 * 2) Default imports
 * 3) Nicer exception handling
 */
internal class JupyterCore : JupyterIntegration() {

    companion object {
        private val HTMLOutputs = CopyOnWriteArrayList<HTMLOutput>()
        internal fun addOutput(htmlOutput: HTMLOutput) = HTMLOutputs.add(htmlOutput)
        internal var isolation = false
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

        render<HTMLOutput> {
            if (isolation) HTML(it.asHTMLPage(), true) else HTML(it.asHTML(), false)
        }

        beforeCellExecution {
            HTMLOutputs.clear()
        }

        afterCellExecution { _, _ ->
            HTMLOutputs.forEach {
                this.display(it, null)
            }
            HTMLOutputs.clear()
        }

    }

}

/**
 * Render the output in a Notebook cell. When a [HTMLOutput] result is not the last statement in a Notebook cell, you
 * can use this to make sure it is still rendered.
 */
fun HTMLOutput.render() {
    JupyterCore.addOutput(this)
}
