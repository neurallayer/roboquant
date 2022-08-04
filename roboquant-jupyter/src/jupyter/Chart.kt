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
import org.icepear.echarts.Option
import org.icepear.echarts.components.grid.Grid
import org.icepear.echarts.components.toolbox.*
import org.icepear.echarts.components.visualMap.ContinousVisualMap
import org.roboquant.common.Amount
import java.lang.reflect.Type
import java.time.Instant
import java.util.*

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
 * Type adaptor for Gson that allows to use [Amount] that get serialized as a BigDecimal.
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
 * Type adaptor for Gson that allows to use [Instant] that get serialized as a Long
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
 * Type adaptor for Gson that allows to use [Triple] that get serialized as a List
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
 * Base class all roboquant charts in Notebooks. Subclasses should implemented at least the [renderOption] method.
 */
abstract class Chart : Output() {

    private var hasJavascript: Boolean = false

    /**
     * Height for charts, default being 500 pixels. Subclasses can override this value
     */
    var height = 500

    /**
     * Set a custom title for the chart. If not set, a default title will be generated. If you don't want a title, set
     * this property to an empty string.
     */
    var title : String? = null


    companion object {

        /**
         * Theme to use for plotting. When left to "auto", it will adapt to the theme set for
         * Jupyter Lab (light or dark)
         */
        var theme = "auto"

        /**
         * If set to true, a console.log(option) statement will be included so the option parameter of echarts can be
         * easily debugged
         */
        var debug = false

        /**
         * Maximum number of samples to plot in a chart. Certain types of charts can be become very large and as
         * a result make your browser unresponsive. By lowering this value (default is Int.MAX_VALUE)
         * before serializing the result to the browser, the sample size will first be reduced. A good value might
         * be 100_000, but this depends on your computer.
         */
        var maxSamples = Int.MAX_VALUE

        // Make this variable so can charts work for both Chinese and western users.
        var positiveColor : String = "#0C0" // Green
        var negativeColor : String = "#C00" // Red
        var neutralColor : String = "#CC0" // Yellow

        internal val gsonBuilder = GsonBuilder()

        init {
            // register the custom type adapters
            gsonBuilder.registerTypeAdapter(Pair::class.java, PairAdapter())
            gsonBuilder.registerTypeAdapter(Triple::class.java, TripleAdapter())
            gsonBuilder.registerTypeAdapter(Instant::class.java, InstantAdapter())
            gsonBuilder.registerTypeAdapter(Amount::class.java, AmountAdapter())

            // Swap colors for Chinese users
            if (Locale.getDefault() == Locale.CHINA) {
                positiveColor = "#C00" // Red
                negativeColor = "#0C0" // Green
            }
        }

    }

    /**
     * Reduce the sample size in order to ensure the browser can plot it and remain responsive.
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
     * Generates the HTML required to draw a chart. This is a HTML snippet and not a full HTML page and it is suitable
     * to be rendered in the cell output of a Jupyter notebook.
     */
    override fun asHTML(): String {
        val fragment = renderOption().trimStart()
        val themeDetector = if (theme == "auto") {
            "document.body.dataset.jpThemeLight == 'false' ? 'dark' : 'light'"
        } else {
            "'$theme'"
        }

        val debugStmt = if (debug) "console.log(option);" else ""

        // Transfer a string into a javascript Function for tooltip formatting
        val handleJS = if (hasJavascript)
                """option && (option.tooltip.formatter = new Function("p", option.tooltip.formatter));"""
            else
                ""

        return """
        <div style="width:100%;height:${height}px;" class="rqcharts"></div>
        
        <script type="text/javascript">
            (function () {
                let elem = document.currentScript.previousElementSibling;
                let fn = function(a) {
                    let theme = $themeDetector;
                    let myChart = echarts.init(elem, theme);
                    let option = $fragment;$handleJS;
                    option && myChart.setOption(option);
                    elem.ondblclick = function () { myChart.resize() }; $debugStmt
                }
                call_echarts(fn)        
            })()
        </script>
        """.trimIndent()
    }

    /**
     * Generates a standalone HTML page for the chart. This page can be saved and for example viewed in a
     * standalone browser.
     */
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


    /**
     * Return the toolbox
     */
    protected fun getToolbox(includeMagicType: Boolean = true): Toolbox {
        val features = mutableMapOf(
            "saveAsImage" to ToolboxSaveAsImageFeature(),
            "dataView" to ToolboxDataViewFeature().setReadOnly(true),
            "dataZoom" to ToolboxDataZoomFeature().setYAxisIndex("none"),
            "magicType" to ToolboxMagicTypeFeature().setType(arrayOf("line", "bar")),
            "restore" to ToolboxRestoreFeature()
        )
        if (! includeMagicType) features.remove("magicType")
        return Toolbox().setFeature(features)
    }

    /**
     * Return the most basic toolbox
     */
    protected fun getBasicToolbox(): Toolbox {
        val features = mutableMapOf(
            "saveAsImage" to ToolboxSaveAsImageFeature(),
            "restore" to ToolboxRestoreFeature(),
            "dataView" to ToolboxDataViewFeature().setReadOnly(true),
        )
        return Toolbox().setFeature(features)
    }

    /**
     * Get the default visual map ranging from [min] to [max]
     */
    protected fun getVisualMap(min: Number?, max: Number?): ContinousVisualMap {
        return ContinousVisualMap()
            .setMin(min ?: -1)
            .setMax(max ?: 1)
            .setCalculable(true)
            .setOrient("horizontal")
            .setTop("top")
            .setLeft("center")
            .setColor(arrayOf(positiveColor, neutralColor, negativeColor))
    }

    /**
     * Calling this function will ensure that a JavaScript function used in the tooltip formatter wil work.
     * A function should only contain the body and the input paramter is `p`, for example:
     *
     * ```
     *      return p.param[0] + p.param[1]
     * ```
     */
    protected fun javasciptFunction(function: String): String {
        hasJavascript = true
        return function
    }

    /**
     * Return JSON string representation of the [option]
     */
    protected fun renderJson(option: Option) : String {
        option.backgroundColor = "rgba(0,0,0,0)"
        if (option.grid == null) {
            val grid = Grid().setContainLabel(true).setRight("3%").setLeft("3%")
            option.setGrid(grid)
        }
        return gsonBuilder.create().toJson(option)
    }

    /**
     * Subclasses will need to return the value of the option attribute. Although is can be a plain JSON string, this is
     * not required, and it can be any valid JavaScript object including any JavaScript code.
     */
    abstract fun renderOption(): String

}

