/*
 * Copyright 2020-2023 Neural Layer
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

import java.io.File

/**
 * Base class for anything that can output HTML
 */
abstract class HTMLOutput {

    /**
     * Save HTML output to a file with name [filename] on the server.
     */
    fun toHTMLFile(filename: String) {
        val content = asHTMLPage()
        val f = File(filename)
        f.writeText(content)
    }

    /**
     * Generate an HTML snippet. Subclasses will need to implement this method. This is used in Jupyter-Lab environments
     * that can directly insert HTML and JavaScript content in the output of a cell.
     */
    abstract fun asHTML(): String

    /**
     * Generate a whole HTML page. Subclasses will need to implement this method. This is used in Jupyter-Notebook
     * environments that put the output of a cell in an iFrame.
     */
    abstract fun asHTMLPage(): String
}