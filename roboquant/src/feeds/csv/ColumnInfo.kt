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

package org.roboquant.feeds.csv

import javax.naming.ConfigurationException

/**
 * Contains the info of what the columns are in a CSV file
 *
 */
internal class ColumnInfo {
    var time: Int = -1
    var open: Int = -1
    var high: Int = -1
    var low: Int = -1
    var close: Int = -1
    var volume: Int = -1
    var adjustedClose: Int = -1

    val hasVolume
        get() = volume != -1


    fun detectColumns(header: List<String>) {
        header.forEachIndexed { index, column ->
            when (column.uppercase()) {
                "TIME" -> time = index
                "DATE" -> time = index
                "OPEN" -> open = index
                "HIGH" -> high = index
                "LOW" -> low = index
                "CLOSE" -> close = index
                "ADJ_CLOSE" -> adjustedClose = index
                "ADJ CLOSE" -> adjustedClose = index
                "VOLUME" -> volume = index
            }
        }
    }

    /**
     * Parse a column definition, like 'T?OHLCA?V'
     *
     * @param def
     */
    fun define(def: String) {
        def.forEachIndexed { index, char ->
            when (char) {
                'T' -> time = index
                'O' -> open = index
                'H' -> high = index
                'L' -> low = index
                'C' -> close = index
                'A' -> adjustedClose = index
                'V' -> volume = index
                '?', 'X', ' ', '_' -> {}
                else -> {
                    throw ConfigurationException("Found unsupported character '$char' in parse pattern $def")
                }
            }
        }
    }
}