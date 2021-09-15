package org.roboquant.feeds.csv

import javax.naming.ConfigurationException

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
                '?', 'X', ' ' -> {}
                else -> {
                    throw ConfigurationException("Found unsupported character '$char' in parse pattern $def")
                }
            }
        }
    }
}