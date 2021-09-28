package org.roboquant.jupyter

import org.roboquant.common.Config

/**
 * Provides current environment settings in HTML format usable for displaying in a Jupyter Notebook.
 */
class Welcome : Output() {
    /**
     * Generate an HTML snippet. Subclasses will need to implement this method. This is used in Jupyter-Lab environments
     * that can directly insert HTML and JavaScript content in the output of a cell.
     *
     * @return
     */
    override fun asHTML(): String {
        val jvmInfo = System.getProperty("java.vm.name") + " " + System.getProperty("java.version")
        val osInfo = System.getProperty("os.name") + " " + System.getProperty("os.version")
        val runtimeInfo = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        return """
            <img src="https://roboquant.org/img/avatar.png" alt="roboquant logo" align="left" style="margin-right: 20px; max-height:150px;"/>
            <span>
                <b style="color: rgb(50,150,200);"> roboquant </b> (version ${Config.version})<br>
                <b> home:</b> ${Config.home}<br>
                <b> os:</b> $osInfo<br>
                <b> jvm:</b> $jvmInfo<br>
                <b> memory:</b> $runtimeInfo MB<br>
                <b> env: </b> $mode
            </span>
            """.trimIndent()
    }

    /**
     * Generate a whole HTML page. Subclasses will need to implement this method. This is used in Jupyter-Notebook
     * environments that put the output of a cell in an iFrame.
     *
     * @param useCDN Should a CDN/link be used for static content or should the content be embedded
     * @return
     */
    override fun asHTMLPage(useCDN: Boolean): String {
       return asHTML()
    }


}