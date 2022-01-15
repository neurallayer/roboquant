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
import org.roboquant.common.plusAssign
import org.roboquant.common.round
import org.roboquant.logging.MetricsEntry
import org.roboquant.logging.getName
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.*
import kotlin.math.absoluteValue

class CalendarChart(
    private val metricsData: List<MetricsEntry>,
    private val fractionDigits: Int = 2,
    private val zoneId: ZoneId = Config.defaultZoneId
) : Chart() {

    private var max: Double = 1.0
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zoneId)

    private fun prepData(): Map<Int, List<Any>> {

        max = metricsData.map { it.value }.maxOfOrNull { it.absoluteValue } ?: 1.0
        val perYear = metricsData.groupBy { it.info.time.atZone(zoneId).year }
        val result = mutableMapOf<Int, List<Pair<String, BigDecimal>>>()
        perYear.forEach { (t, u) ->
            result[t] = u.map {
                Pair(timeFormatter.format(it.info.time), it.value.round(fractionDigits))
            }
        }
        return result.toSortedMap()
    }

    private fun genCalendar(data: Map<Int, List<Any>>): String {
        var top = 100
        val result = StringBuffer()
        data.keys.forEach {
            result += """
            {
                top: $top,
                range: '$it',
                cellSize: ['auto', 20],
                right: 10
            },
            """
            top += 200
        }

        // Ensure the height of the div is enough to show all calendars
        height = top
        return result.toString()
    }

    private fun genSeries(data: Map<Int, List<Any>>): String {
        val result = StringBuffer()
        val gson = gsonBuilder.create()
        data.values.forEachIndexed { index, list ->
            val d = gson.toJson(list)
            result += """
            {
               type: 'heatmap',
               coordinateSystem: 'calendar',
               calendarIndex: $index,
               data: $d
            },
            """
        }
        return result.toString()
    }

    /** @suppress */
    override fun renderOption(): String {
        val data = prepData()
        val calendar = genCalendar(data)
        val series = genSeries(data)
        return """
           {
               tooltip: {
                   position: 'top',
                   formatter: function (p) {
                        var format = echarts.format.formatTime('yyyy-MM-dd', p.data[0]);
                        return format + ': ' + p.data[1];
                    }
               },
                toolbox: {
                    feature: {
                        restore: {},
                        saveAsImage: {}
                    }
                },
                title: {
                    text: 'Daily results ${metricsData.getName()}'
                },
               visualMap: {
                   min: -$max,
                   max: $max,
                   calculable: true,
                   orient: 'horizontal',
                   left: 'center',
                   top: 'top',
                   inRange : {   
                         color: ['#FF0000', '#00FF00']
                     }
               },

               calendar: [$calendar],
               series: [$series]
           };
       """.trimStart()
    }
}


