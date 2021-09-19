package org.roboquant.jupyter

import org.roboquant.common.plusAssign
import org.roboquant.logging.MetricsEntry
import org.roboquant.logging.getName
import java.text.DecimalFormat
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.*
import kotlin.math.absoluteValue

class CalendarChart(private val metricsData: List<MetricsEntry>, val format: String = "#.00") : Chart() {

    private var max: Double = 1.0
    private val decimalFormatter = DecimalFormat(format)
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

    private fun prepData(): MutableMap<Int, List<Any>> {

        max = metricsData.map { it.value }.map { it.toDouble().absoluteValue }.maxOrNull() ?: 1.0
        val perYear = metricsData.groupBy { it.info.time.atOffset(ZoneOffset.UTC).year }
        val result = mutableMapOf<Int, List<Pair<String, String>>>()
        perYear.forEach { (t, u) ->
            result[t] = u.map { Pair(timeFormatter.format(it.info.time), decimalFormatter.format(it.value)) }
        }
        return result.toSortedMap()
    }


    private fun genCalendar(data: MutableMap<Int, List<Any>>): String {
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

    private fun genSeries(data: MutableMap<Int, List<Any>>): String {
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


