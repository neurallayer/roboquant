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

package org.roboquant.common

import java.text.DecimalFormat
import java.util.logging.Level

interface Summarizable {

    fun summary(singleCurrency: Boolean = true): Summary
}

/**
 * Summary allows to represent nested data into a tree like format. This is supported by several of the components
 * in roboquant if you invoke their summary() method.
 *
 * @property content The content for the summary
 * @constructor Create new Summary
 */
class Summary(val content: String) {

    private val children = mutableListOf<Summary>()
    private val decimalFormatter = DecimalFormat(decimalPattern)

    companion object {
        private val logger = Logging.getLogger(Summary::class)
        var decimalPattern = "#.000"
        var sep = ": "
    }

    /**
     * Add a new summary as a child
     *
     * @param child
     */
    fun add(child: Summary) = children.add(child)

    /**
     * Add a [label] with a numerical [value]. When adding a Float or Double, the decimal formatter will be used to
     * format it. Other numbers will be presented using the toString() method.
     */
    fun add(label: String, value: Number) {
        when (value) {
            is Double -> children.add(Summary("$label$sep${decimalFormatter.format(value)}"))
            is Float -> children.add(Summary("$label$sep${decimalFormatter.format(value)}"))
            else -> children.add(Summary("$label$sep$value"))
        }
    }

    /**
     * Add a [label] and [value] to this summary
     */
    fun add(label: String, value: Any?) = children.add(Summary("$label$sep$value"))

    /**
     * Add a [label] and [summary] to this summary
     */
    fun add(label: String, summary: Summary) {
        val child = Summary(label)
        child.add(summary)
        children.add(child)
    }

    /**
     * Add a [label] to this summary
     */
    fun add(label: String) = children.add(Summary(label))

    /**
     * Directly print this summary to the standard out.
     */
    fun print(maxChildren: Int = Int.MAX_VALUE) = print(toString(maxChildren))

    /**
     * Log this summary using the standard roboquant logger.
     *
     * @param level At which level this summary should be logged, default is INFO
     */
    fun log(maxChildren: Int = Int.MAX_VALUE, level: Level = Level.INFO) = logger.log(level) {
        toString(maxChildren)
    }

    /**
     * To string
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        generate(buffer, "", "")
        return buffer.toString()
    }

    /**
     * To string
     */
    fun toString(maxChildren: Int): String {
        val buffer = StringBuilder()
        generate(buffer, "", "", maxChildren)
        return buffer.toString()
    }

    private fun generate(
        buffer: StringBuilder,
        prefix: String,
        childrenPrefix: String,
        maxChildren: Int = Int.MAX_VALUE
    ) {
        buffer.append(prefix)
        // if (children.isNotEmpty()) buffer.append(Logging.blue(content)) else buffer.append(content)
        buffer.append(content)
        buffer.append('\n')
        val it = children.iterator()
        var cnt = 0
        while (it.hasNext()) {
            val next = it.next()
            if (it.hasNext()) {
                if (++cnt > maxChildren) {
                    buffer.append("$childrenPrefix└── ... ")
                    return
                }
                next.generate(buffer, "$childrenPrefix├── ", "$childrenPrefix│   ")
            } else {
                next.generate(buffer, "$childrenPrefix└── ", "$childrenPrefix    ")
            }
        }
    }
}