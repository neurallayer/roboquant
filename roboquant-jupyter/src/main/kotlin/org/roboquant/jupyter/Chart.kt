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
import kotlin.math.roundToInt

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
 * Base class all roboquant charts in Notebooks. Subclasses should implement at least the [getOption] method.
 */
abstract class Chart : HTMLOutput() {

    /**
     * Does the generated option JSON string contain JavaScript. If true additional code will be generated to parse
     * this into a Javascript function.
     */
    private var containsJavaScript: Boolean = false

    /**
     * Height for charts, default being 500 pixels. Subclasses can override this value
     */
    var height = 500

    /**
     * Set a custom title for the chart. If not set, a default title will be generated. If you don't want a title, set
     * this property to an empty string.
     */
    var title: String? = null

    /**
     * Settings that apply to all charts
     */
    companion object {

        // Which version of echarts.min.js to use
        private const val commit = "3bf8cde65fe922f58063095186c6bef5846dbee9"

        @Suppress("MaxLineLength")
        internal const val scriptUrl =
            "https://cdn.jsdelivr.net/gh/neurallayer/roboquant@$commit/roboquant-jupyter/src/main/resources/js/echarts.min.js?version=$commit"

        /**
         * Used to ensure the output divs have a unique id. This is only used in classic notebooks since then the
         * JavaScript cannot access the DIV by document.currentScript.parentElement and will fall back to the id.
         */
        internal var counter = 0

        /**
         * Maximum number of samples to plot in a chart. Certain types of charts can be become very large and as
         * a result make your browser unresponsive. By lowering this value (default is [Int.MAX_VALUE])
         * before serializing the result to the browser, the sample size will first be reduced. A good value might
         * be 100_000, but this depends on your computer.
         */
        var maxSamples = Int.MAX_VALUE

        // Make this a variable so charts work for both Chinese and western users.

        /**
         * Color to use for positive elements, like profit
         */
        var positiveColor: String = "#0C0" // Green

        /**
         * Color to use for negative elements, like loss
         */
        var negativeColor: String = "#C00" // Red

        /**
         * Neutral Color
         */
        var neutralColor: String = "#CC0" // Yellow

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

        /**
         * Get the HTML script tag for the required JavaScript.
         */
        fun getScript(): String {
            return """<script type='text/javascript' src='$scriptUrl'></script>"""
        }

    }

    /**
     * Reduce the sample size in order to ensure the browser can display the chart while remaining responsive.
     */
    protected fun <T> reduce(data: Collection<T>): Collection<T> {
        return if (data.size > maxSamples) {
            val skip = (0.5 + data.size / maxSamples).roundToInt()
            data.filterIndexed { index, _ -> index % skip == 0 }
        } else {
            data
        }
    }

    /**
     * Generates the HTML snippet required to draw a chart. This is an HTML snippet and not a full HTML page,
     * and it is suitable to be rendered in the cell output of a Jupyter notebook.
     */
    override fun asHTML(): String {
        val fragment = getOption().renderJson().trimStart()
        val id = "roboquant-${counter++}"
        val convertor = if (containsJavaScript)
            "option.tooltip.formatter = new Function('p', option.tooltip.formatter);"
        else
            ""

        return """ 
        <div style="width:100%;height:${height}px;" class="rqcharts" id="$id">
            <script type="text/javascript">
                (function () {
                    let parentElem = document.currentScript.parentElement;
                    if (parentElem.tagName == "HEAD") {
                        parentElem = document.getElementById("$id");
                    }
                    console.log("registered new chart");
                    let option = $fragment;$convertor
                    window.call_echarts(
                        function () {
                            console.log("rendering chart");
                            window.renderEChart(parentElem, option);
                        }
                    );
                })()
            </script>
        </div>
        """.trimIndent()
    }

    /**
     * Generates a standalone HTML page for the chart. This page can be saved and for example viewed in a
     * standalone browser.
     */
    override fun asHTMLPage(): String {
        val fragment = asHTML()
        val script = getScript()

        return """
        <html>
            <head>
                $script
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
     * Return a standard toolbox that can be included in a chart
     */
    protected fun getToolbox(includeMagicType: Boolean = true): Toolbox {
        val features = mutableMapOf(
            "saveAsImage" to ToolboxSaveAsImageFeature(),
            "dataView" to ToolboxDataViewFeature().setReadOnly(true),
            "dataZoom" to ToolboxDataZoomFeature().setYAxisIndex("none"),
            "magicType" to ToolboxMagicTypeFeature().setType(arrayOf("line", "bar")),
            "restore" to ToolboxRestoreFeature()
        )
        if (!includeMagicType) features.remove("magicType")
        return Toolbox().setFeature(features)
    }

    /**
     * Return a basic toolbox hat can be included in a chart
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
     * Calling this function will ensure that a JavaScript function used in the tooltip formatter will work.
     * The provided function should only contain the body and the input parameter is `p`, for example:
     *
     * ```
     *      javascriptFunction("return p.param[0] + p.param[1]")
     * ```
     */
    protected fun javascriptFunction(function: String): String {
        containsJavaScript = true
        return function
    }

    /**
     * Subclasses will need to return the value of the option attribute.
     */
    abstract fun getOption(): Option

}

/**
 * Convert the option to a JSON string
 */
fun Option.renderJson(): String {
    // Set default transparent background so charts look better with Jupyter Notebooks
    if (backgroundColor == null) backgroundColor = "rgba(0,0,0,0)"

    // Use UTC, makes it easier to compare charts and values
    useUTC = true

    // Set the default grid if none is set already
    if (grid == null) {
        val grid = Grid().setContainLabel(true).setRight("3%").setLeft("3%")
        setGrid(grid)
    }
    return Chart.gsonBuilder.create().toJson(this)
}

