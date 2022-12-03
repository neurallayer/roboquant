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
 * Contains the info of what the columns are in a CSV file. This allows for dealing with a wide variety of CSV formats
 * with different order and type of columns.
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

    /**
     * Autodetect the column types based on commonly used [header] names.
     */
    fun detectColumns(header: List<String>) {
        val notCapital = Regex("[^A-Z]")
        header.forEachIndexed { index, column ->
            when (column.uppercase().replace(notCapital, "")) {
                "TIME" -> time = index
                "DATE" -> time = index
                "DAY" -> time = index
                "DATETIME" -> time = index
                "TIMESTAMP" -> time = index
                "OPEN" -> open = index
                "HIGH" -> high = index
                "LOW" -> low = index
                "CLOSE" -> close = index
                "ADJCLOSE" -> adjustedClose = index
                "ADJUSTEDCLOSE" -> adjustedClose = index
                "VOLUME" -> volume = index
            }
        }
    }

    /**
     * Parse a column definition using the provided [def]
     */
    fun define(def: String) {
        val str = def.uppercase()
        require(str.contains('T')) { "time (T) is mandatory when providing column definitions, found $def"}
        str.forEachIndexed { index, char ->
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