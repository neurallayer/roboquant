package org.roboquant.jupyter

import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.ThrowableRenderer
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import org.roboquant.common.escapeHtml
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.CopyOnWriteArrayList

internal class RoboquantThrowableRenderer : ThrowableRenderer {

    override fun accepts(throwable: Throwable): Boolean {
        return true
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
        val isolated = Output.mode === Output.Mode.CLASSIC
        return HTML(result, isolated)
    }

}

/**
 * Integration with Kotlin based Jupyter notebook kernels. Main features:
 *
 * 1) Support for charts
 * 2) Default imports
 * 3) Nicer Exception handling
 *
 */
internal class JupyterCore : JupyterIntegration() {

    companion object {
        private val outputs = CopyOnWriteArrayList<Output>()
        fun render(output: Output) = outputs.add(output)
    }

    override fun Builder.onLoaded() {
        import(
            "org.roboquant.*",
            "org.roboquant.logging.*",
            "org.roboquant.common.*",
            "org.roboquant.metrics.*",
            "org.roboquant.strategies.*",
            "org.roboquant.strategies.ta.*",
            "org.roboquant.orders.*",
            "org.roboquant.feeds.*",
            "org.roboquant.feeds.csv.*",
            "org.roboquant.feeds.avro.*",
            "org.roboquant.feeds.random.*",
            "org.roboquant.brokers.sim.SimBroker",
            "org.roboquant.brokers.*",
            "org.roboquant.policies.*",
            "org.roboquant.jupyter.*",
            "java.time.Period",
            "java.time.Instant",
            "java.time.temporal.ChronoUnit"
        )


        onLoaded {
            addThrowableRenderer(RoboquantThrowableRenderer())
            execute("%logLevel warn")
        }

        /**
         * The resources that need to be loaded
         */
        resources {

            js("echarts") {
                url("https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js")
            }

        }

        beforeCellExecution {
            outputs.clear()
        }

        afterCellExecution { _, _ ->
            outputs.forEach {
                this.display(it)
            }
            outputs.clear()
        }

        repositories(
            "*mavenLocal",
            "https://roboquant.jfrog.io/artifactory/roboquant",
            "https://repo1.maven.org/maven2/",
            "https://jitpack.io"
        )

    }

}


