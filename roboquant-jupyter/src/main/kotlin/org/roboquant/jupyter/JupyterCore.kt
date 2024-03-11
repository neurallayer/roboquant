/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.jupyter

import org.jetbrains.kotlinx.jupyter.api.*
import org.jetbrains.kotlinx.jupyter.api.libraries.ColorScheme
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import org.roboquant.charts.Chart
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.common.Size
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Present exceptions a bit nicer in notebooks
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
        return HTML(result, NotebookConfig.isolation)
    }

}

/**
 * Holds configuration for notebooks
 */
object NotebookConfig {
    /**
     * Should all HTML output be rendered in iFrames, default is false
     */
    var isolation: Boolean = false

    /**
     * Theme to use for rendering. Default is "auto"
     */
    var theme = "auto"

}

/**
 * Integration with Kotlin-based Jupyter notebook kernels.
 * Some of the main features include:
 *
 * 1. Support for charts using Apache ECharts library
 * 2. Default imports
 * 3. Nicer exception handling
 */
internal class JupyterCore(
    private val notebook: Notebook?,
    private val options: MutableMap<String, String?>
) : JupyterIntegration() {


    init {
        logger.debug { options.toMap().toString() }
        logger.debug { notebook.toString() }
    }

    companion object {
        private val logger = Logging.getLogger(JupyterCore::class)
        internal var host: KotlinKernelHost? = null
    }

    @Suppress("LongMethod")
    override fun Builder.onLoaded() {
        val version = options["version"] ?: Config.info.version
        logger.debug { "version=$version" }
        val deps = mutableSetOf(
            "org.roboquant:roboquant-ta:$version"
        )
        val load = options["modules"] ?: ""
        logger.debug { "modules=$load" }
        load.split(':').forEach {
            if (it.isNotBlank()) deps.add("org.roboquant:roboquant-$it:$version")
        }

        @Suppress("SpreadOperator")
        dependencies(*deps.toTypedArray())

        // Applies to notebooks in Datalore
        if (notebook.jupyterClientType == JupyterClientType.DATALORE) {
            NotebookConfig.isolation = true
        }

        import(
            "org.roboquant.*",
            "org.roboquant.loggers.*",
            "org.roboquant.journals.*",
            "org.roboquant.common.*",
            "org.roboquant.metrics.*",
            "org.roboquant.strategies.*",
            "org.roboquant.orders.*",
            "org.roboquant.feeds.*",
            "org.roboquant.feeds.csv.*",
            "org.roboquant.feeds.random.*",
            "org.roboquant.brokers.sim.*",
            "org.roboquant.brokers.*",
            "org.roboquant.policies.*",
            "org.roboquant.jupyter.*",
            "org.roboquant.charts.*",
            "java.time.Instant",
            "java.time.temporal.ChronoUnit",
            "org.roboquant.ta.*",
            "org.roboquant.avro.*",
        )

        // Improve output of exceptions
        addThrowableRenderer(RoboquantThrowableRenderer())

        onLoaded {
            host = this
            execute("%logLevel warn")
        }

        resources {
            js("echarts") {
                url(Chart.JSURL)
            }
        }

        addRenderer(
            createRendererByCompileTimeType<Size> {
                val value = it.value as Long
                Size.fromUnderlyingValue(value).toString()
            }
        )

        render<Welcome> {
            if (NotebookConfig.isolation) HTML(it.asHTMLPage(), true) else HTML(it.asHTML(), false)
        }

        render<Chart> {
            var theme = NotebookConfig.theme
            if (theme == "auto" && notebook.jupyterClientType == JupyterClientType.KOTLIN_NOTEBOOK) {
                theme = if (notebook.currentColorScheme == ColorScheme.DARK) "dark" else "light"
            }
            if (NotebookConfig.isolation) HTML(it.asHTMLPage(theme), true) else HTML(it.asHTML(theme), false)
        }

    }

}

/**
 * Render a [Chart] in a Notebook cell.
 * When a chart is not the last statement in a Notebook cell, you can use this method to make sure it still
 * gets rendered.
 */
@Suppress("unused")
fun Chart.render() {
    JupyterCore.host?.display(this, null)
}
