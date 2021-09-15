package org.roboquant.jupyter

import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Integration with Kotlin based Jupyter notebook kernels. Main features:
 *
 * 1) Support for plotting and tables
 * 2) Default imports
 *
 *
 * @constructor Create new Jupyter
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
            "java.time.Instant"
        )

        onLoaded {
            // outputs.add(Welcome())
        }

        /**
         * The resources that need to be loaded
         */
        resources {

            js("echarts") {
                url("https://cdn.jsdelivr.net/npm/echarts@5.1.2/dist/echarts.min.js")
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

