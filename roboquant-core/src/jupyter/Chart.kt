@file:Suppress("MemberVisibilityCanBePrivate")

package org.roboquant.jupyter

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.time.Instant


/**
 * Type adaptor for Gson that allows to use Pair that get serialized as a list
 *
 * @constructor Create new Pair adapter
 */
private class PairAdapter : JsonSerializer<Pair<*, *>> {

    override fun serialize(
        jsonElement: Pair<*, *>,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        val list = listOf(jsonElement.first, jsonElement.second)
        return jsonSerializationContext.serialize(list)
    }

}

/**
* Type adaptor for Gson that allows to use Instant that get serialized as a Long
*
* @constructor Create new Pair adapter
*/
private class InstantAdapter : JsonSerializer<Instant> {

    override fun serialize(
        jsonElement: Instant,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        return jsonSerializationContext.serialize(jsonElement.toEpochMilli())
    }

}


/**
 * Type adaptor for Gson that allows to use Triple that get serialized as a list
 *
 * @constructor Create new Pair adapter
 */
private class TripleAdapter : JsonSerializer<Triple<*, *, *>> {

    override fun serialize(
        jsonElement: Triple<*, *, *>,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        val list = listOf(jsonElement.first, jsonElement.second, jsonElement.third)
        return jsonSerializationContext.serialize(list)
    }

}

/**
 * Base class for ECharts based charts
 *
 * TODO: use better approach to create the options for an e-chart
 *
 * @constructor Create empty E chart
 */
abstract class Chart : Output() {

    /**
     * Default height for charts. Subclasses can override this value
     */
    var height = 500

    companion object {
        var theme = "light"

        val gsonBuilder = GsonBuilder()

        init {
            // register the three builders
            gsonBuilder.registerTypeAdapter(Pair::class.java, PairAdapter())
            gsonBuilder.registerTypeAdapter(Triple::class.java, TripleAdapter())
            gsonBuilder.registerTypeAdapter(Instant::class.java, InstantAdapter())
        }

        fun loadScript(): String {
            val classloader = Thread.currentThread().contextClassLoader
            val stream = classloader.getResourceAsStream("echarts.min.js")!!
            return String(stream.readAllBytes(), StandardCharsets.UTF_8)
        }
    }

    /**
     * Generates the HTML required to draw a chart.
     */
    override fun asHTML(): String {
        val fragment = renderOption()

        return """
        <div style="width:100%;height:${height}px;" class="rqcharts"></div>

        <script type="text/javascript">
            (function () {
                let elem = document.currentScript.previousElementSibling;
                let fn = function(a) {
                    var myChart = echarts.init(elem, '$theme');
                    var option = $fragment
                    option && myChart.setOption(option);
                    elem.ondblclick = function () { myChart.resize() };
                    console.log('rendered new chart');     
                }
                call_echarts(fn)        
            })()
        </script>
        """.trimIndent()
    }



    override fun asHTMLPage(useCDN: Boolean): String {
        val fragment = asHTML()
        val script = if (useCDN)
            """<script src='https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js'></script>"""
        else
            """<script type='text/javascript'>${loadScript()}</script>"""

        return """
        <html>
            <head>
                $script
                <script type='text/javascript'>
                    window["call_echarts"] = function(f) {f();};
                </script>
                <style type='text/css' media='screen'>
                        html {  margin: 0px; padding: 0px; min-height: ${height}px;}
                        body { margin: 0px; padding: 10px; min-height: ${height}px;}
                </style>
            </head>
            <body>
                $fragment
            </body>
        </html>
        """
    }


    protected fun renderGrid(): String {
        return """
            grid: {
                left: '3%',
                right: '3%',
                containLabel: true
            }
        """
    }

    protected fun renderDataZoom(): String {
        return """
             dataZoom: [{ type: 'inside'}, {}]
        """.trimIndent()
    }

    protected fun renderLegend(): String {
        return """
            legend: {
                type: 'scroll',
                orient: 'vertical',
                right: 10,
                top: 20,
                bottom: 20
            }
        """.trimIndent()
    }

    protected fun renderToolbox(): String {
        return """
            toolbox: {
                feature: {
                    dataZoom: {
                        yAxisIndex: 'none'
                    },
                    dataView: {readOnly: true},
                    magicType: {type: ['line', 'bar']},
                    restore: {},
                    saveAsImage: {}
                }
            }
        """.trimIndent()
    }


    protected abstract fun renderOption(): String

}

