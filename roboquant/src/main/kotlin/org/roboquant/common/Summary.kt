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

package org.roboquant.common

import java.text.DecimalFormat


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

    /**
     * @suppress
     */
    private companion object {
        private const val decimalPattern = "#.###"
        private const val sep = ": "
    }

    /**
     * Add a new summary as a [child]
     */
    fun add(child: Summary) = children.add(child)


    /**
     * Add a [label] with an int [value]. When adding a Float or Double, the decimal formatter will be used to
     * format it. Other numbers will be presented using the toString() method.
     */
    fun add(label: String, value: Int) {
        children.add(Summary("$label$sep$value"))
    }

    /**
     * Add a [label] with a double [value]. When adding a Float or Double, the decimal formatter will be used to
     * format it. Other numbers will be presented using the toString() method.
     */
    fun add(label: String, value: Double) {
        children.add(Summary("$label$sep${decimalFormatter.format(value)}"))
    }

    /**
     * Add a [label] and [value] to this summary
     */
    fun add(label: String, value: Any?) = children.add(Summary("$label$sep$value"))


    /**
     * Add a [label] to this summary
     */
    fun add(label: String) = children.add(Summary(label))


    /**
     * To string
     */
    override fun toString(): String {
        val buffer = StringBuilder()
        generate(buffer, "", "")
        return buffer.toString()
    }

    private fun generate(
        buffer: StringBuilder,
        prefix: String,
        childrenPrefix: String,
    ) {
        buffer.append(prefix)
        buffer.append(content)
        buffer.append('\n')
        val it = children.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (it.hasNext()) {
                next.generate(buffer, "$childrenPrefix├── ", "$childrenPrefix│   ")
            } else {
                next.generate(buffer, "$childrenPrefix└── ", "$childrenPrefix    ")
            }
        }
    }
}


/**
 * Create a summary for a collection of rows in which each row contains 1 or more columns
 */
fun Collection<List<Any>>.summary(name: String): Summary {
    val maxSizes = mutableMapOf<Int, Int>()
    for (line in this) {
        for (column in line.withIndex()) {
            val maxSize = maxSizes.getOrDefault(column.index, Int.MIN_VALUE)
            val len = column.value.toString().length
            if (len > maxSize) maxSizes[column.index] = len
        }
    }

    val summary = Summary(name)
    for (line in this) {
        val result = StringBuffer()
        for (column in line.withIndex()) {
            val maxSize = maxSizes.getOrDefault(column.index, 9) + 1
            val str = "%${maxSize}s│".format(column.value)
            result.append(str)
        }
        summary.add(result.toString())
    }
    return summary
}