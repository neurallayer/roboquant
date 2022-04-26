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

// @file:Suppress("MemberVisibilityCanBePrivate")

package org.roboquant.jupyter

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.roboquant.common.Amount
import java.lang.reflect.Type
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
 * Type adaptor for Gson that allows to use Amount that get serialized as a big decimal
 *
 * @constructor Create new Pair adapter
 */
private class AmountAdapter : JsonSerializer<Amount> {

    override fun serialize(
        jsonElement: Amount,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        return jsonSerializationContext.serialize(jsonElement.toBigDecimal())
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
        // return jsonSerializationContext.serialize(jsonElement.toString())
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
 * @constructor Create empty E chart
 */
abstract class Chart : Output() {


    /**
     * Height for charts, default being 500 pixels. Subclasses can override this value
     */
    var height = 500

    companion object {

        /**
         * Theme to use for plotting
         */
        var theme = "auto"

        /**
         * Maximum number of samples to plot in a chart. Certain types of charts can be become very large and as
         * a result make your browser unresposive. By lowering this value (default is Int.MAX_VALUE)
         * before serializing the result to the browser, the sample size will first be reduced. A good value might
         * be 100_000, but this depends on your computer.
         */
        var maxSamples = Int.MAX_VALUE

        val gsonBuilder = GsonBuilder()

        init {
            // register the three builders
            gsonBuilder.registerTypeAdapter(Pair::class.java, PairAdapter())
            gsonBuilder.registerTypeAdapter(Triple::class.java, TripleAdapter())
            gsonBuilder.registerTypeAdapter(Instant::class.java, InstantAdapter())
            gsonBuilder.registerTypeAdapter(Amount::class.java, AmountAdapter())
        }

    }

    /**
     * Reduce the sample size in order to ensure the browser can still plot it.
     */
    protected fun <T>reduce(data: Collection<T>): Collection<T> {
        return if (data.size > maxSamples) {
            val skip = data.size / maxSamples
            data.filterIndexed {  index, _ -> index % skip == 1 }
        } else {
            data
        }
    }

    /**
     * Generates the HTML required to draw a chart.
     */
    override fun asHTML(): String {
        val fragment = renderOption().trimStart()
        val themeDetector = if (theme == "auto") {
            "document.body.dataset.jpThemeLight == 'false' ? 'dark' : 'light'"
        } else {
            "'$theme'"
        }

        return """
        <div style="width:100%;height:${height}px;" class="rqcharts"></div>
        
        <script type="text/javascript">
            (function () {
                let elem = document.currentScript.previousElementSibling;
                let fn = function(a) {
                    var theme = $themeDetector;
                    var myChart = echarts.init(elem, theme);
                    var option = $fragment;
                    option && (option.backgroundColor = 'rgba(0,0,0,0)');
                    option && myChart.setOption(option);
                    elem.ondblclick = function () { myChart.resize() };
                    console.log('rendered new chart');     
                }
                call_echarts(fn)        
            })()
        </script>
        """.trimIndent()
    }


    override fun asHTMLPage(): String {
        val fragment = asHTML()
        val script = """<script src='https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js'></script>"""

        return """
        <html>
            <head>
                $script
                <script type='text/javascript'>
                    window["call_echarts"] = function(f) {f();};
                </script>
                <style type='text/css' media='screen'>
                    html { margin: 0px; padding: 0px; min-height: ${height}px;}
                    body { margin: 0px; padding: 10px; min-height: ${height}px;}
                </style>
            </head>
            <body>
                $fragment
            </body>
        </html>
        """.trimIndent()
    }

    @Suppress("FunctionOnlyReturningConstant")
    protected fun renderGrid(): String {
        return """grid: { left: '3%', right: '3%', containLabel: true }"""
    }

    @Suppress("FunctionOnlyReturningConstant")
    protected fun renderDataZoom(): String {
        return """dataZoom: [{ type: 'inside'}, {}]"""
    }

    protected fun renderToolbox(includeMagicType: Boolean = true): String {
        val mt = if (includeMagicType) "magicType: {type: ['line', 'bar']}," else ""
        return """
                toolbox: { feature: {
                    dataZoom: { yAxisIndex: 'none'},
                    dataView: {readOnly: true}, $mt
                    restore: {},
                    saveAsImage: {}
                }}""".trimStart()
    }

    /**
     * Subclasses will need to return the value of the option attribute. Although is can be a plain JSON string, this is
     * not required, and it can be any valid JavaScript object including any JavaScript code.
     */
    abstract fun renderOption(): String

}

