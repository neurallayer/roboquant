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

import org.roboquant.common.Config

/**
 * Provides current environment settings in HTML format suitable for displaying in a Jupyter Notebook.
 */
class Welcome : Output() {

    /**
     * Generate an HTML snippet. Subclasses will need to implement this method. This is used in Jupyter-Lab environments
     * that can directly insert HTML and JavaScript content in the output of a cell.
     *
     * @return
     */
    @Suppress("MaxLineLength")
    override fun asHTML(): String {
        val jvmInfo = Config.info["jvm"]
        val osInfo = Config.info["os"]
        val memoryInfo = Config.info["memory"]
        val date = Config.info["build"]
        val version =  Config.info["version"]

        return """
            <img src="https://roboquant.org/img/avatar.png" alt="roboquant logo" align="left" style="margin-right: 20px; max-height:160px;"/>
            <span>
                <b style="color: rgb(50,150,200);font-size: 150%;"> roboquant </b> $version<br>
                <b> build:</b> $date<br>
                <b> home:</b> ${Config.home}<br>
                <b> os:</b> $osInfo<br>
                <b> jvm:</b> $jvmInfo<br>
                <b> memory:</b> $memoryInfo MB<br>
                <b> env: </b> $mode
            </span>
            """.trimIndent()
    }

    /**
     * Generate a whole HTML page. Subclasses will need to implement this method. This is used in Jupyter-Notebook
     * environments that put the output of a cell in an iFrame.
     *
     * @return
     */
    override fun asHTMLPage(): String {
        return asHTML()
    }


}