/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.charts

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
 * Base class all roboquant charts. Subclasses should implement at least the [getOption] method.
 */
abstract class Chart {

    /**
     * Does the generated option JSON string contain JavaScript.
     * If true, additional client-side code might be generated to parse the JSON part into a Javascript function.
     */
    var containsJavaScript: Boolean = false
        protected set

    /**
     * Allow for customization of the chart.
     * This is invoked by [renderJson] before the json is rendered.
     * Default is no customization.
     *
     * Please note, the [getOption] method returns the non-customized option.
     */
    var customize: Option.() -> Unit = {}

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

        // Which commit of echarts.min.js to use
        private const val COMMIT = "fddcad9e93c1c15495c70f358f1ccbb595f0964f"

        /**
         * The URL of the ECHARTS javascript library to use.
         * This script includes some roboquant specific functions and is hosted on GitHub
         */
        const val JSURL =
            "https://cdn.jsdelivr.net/gh/neurallayer/roboquant-jupyter-js@$COMMIT/echarts.min.js?version=$COMMIT"

        /**
         * Maximum number of samples to plot in a chart. Certain types of charts can become large and as
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

        private val gsonBuilder = GsonBuilder()

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
         * Get the HTML script tag to include the required JavaScript.
         */
        fun getScript(): String {
            return """<script type='text/javascript' src='$JSURL'></script>"""
        }

    }

    /**
     * Reduce the sample size to ensure the browser can display the chart while remaining responsive.
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
     * Return a standard toolbox that can be included in a chart
     */
    protected fun getToolbox(includeMagicType: Boolean = true): Toolbox {
        val features = mutableMapOf(
            "saveAsImage" to ToolboxSaveAsImageFeature(),
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

    /**
     * Convert the option of this chart to a JSON string.
     * Is there is [customize] defined, this will be invoked before the json rendering happens.
     */
    fun renderJson(): String {
        val option = getOption()
        option.customize()
        // Set as default, a transparent background. Charts look better with Jupyter Notebooks themes.
        if (option.backgroundColor == null) option.backgroundColor = "rgba(0,0,0,0)"

        // Use UTC, makes it easier to compare charts and values
        option.useUTC = true

        // Set the default grid if none is set already
        if (option.grid == null) {
            val grid = Grid().setContainLabel(true).setRight("3%").setLeft("3%")
            option.setGrid(grid)
        }
        return gsonBuilder.create().toJson(option)
    }


}
