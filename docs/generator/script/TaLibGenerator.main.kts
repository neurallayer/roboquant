/*
 * Copyright 2020-2025 Neural Layer
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

@file:Suppress("TooGenericExceptionThrown", "MaxLineLength", "ReturnCount")
@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("com.google.code.gson:gson:2.9.0")


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

operator fun StringBuffer.plusAssign(str: Any) {
    append(str.toString())
}

/**
 * Base wrapper contains the shared functionality for both TALib (streaming) and TALibBatch generators
 *
 * @property root
 * @constructor Create empty Base wrapper
 */
@Suppress("MemberVisibilityCanBePrivate", "KotlinConstantConditions", "UnusedReceiverParameter")
class TaLibGenerator(private val root: JsonObject) {

     fun JsonObject.getAttr(key: String): String = get(key).asString
     private fun String.unCapitalize() = get(0).lowercase() + substring(1)

     fun JsonObject.getVariableName(key: String): String {
        var result = get(key).asString
        result = result.replace(" ", "")
        result = result.replace("-", "")
        result = result.unCapitalize()
        result = result.replace("inReal", "data")
        return result
    }

     fun getList(key: String): List<JsonObject> {
        if (!root.has(key + "s")) return emptyList()

        val child = root.getAsJsonObject(key + "s").get(key)
        if (child is JsonArray)
            return child.filterIsInstance<JsonObject>().toList()
        else if (child is JsonObject) {
            return listOf(child)
        }
        return emptyList()
    }

    val desc = root.getAttr("ShortDescription")
    private val className: String = root.get("CamelCaseName").asString
    val fnName = className.unCapitalize()
    val fixedFnName = if (fnName == "cdlStickSandwhich") "cdlStickSandwich" else fnName
    val inputParams = getInput()
    val optional = getOptionalInput()
    val firstInput = getList("RequiredInputArgument").first().getVariableName("Name")
    val patternRecognition = root.getAttr("GroupId") == "Pattern Recognition"
    val groupId = root.getAttr("GroupId")

    private fun getOptionalInput(): String {
        val result = StringBuffer()

        val l = getList("OptionalInputArgument")
        l.forEach {
            result += it.getVariableName("Name") + ","
        }
        return result.toString()
    }

     fun callLookback(): String {
        val result = StringBuffer("${fnName}Lookback(")

        val l = getList("OptionalInputArgument")
        l.forEach {
            result += it.getVariableName("Name") + ","
        }
        result.removeSuffix(",")
        result += ")"
        return result.toString()
    }

     fun getInput(withType: Boolean = false): String {
        val result = StringBuffer()

        val l = getList("RequiredInputArgument")
        l.forEach {
            val name = it.getVariableName("Name")
            result += name
            if (withType) {
                result += " : DoubleArray"
            }

            result += ","
        }

        return result.removeSuffix(",").toString()
    }

     fun getOutputNames(): String {
        var result = ""

        val l = getList("OutputArgument")
        var cnt = 1
        repeat(l.size) {
            result += "output$cnt,"
            cnt++
        }
        return result.removeSuffix(",")
    }

     val constructor: String
        get() {
            val result = StringBuffer()
            val l = getList("OptionalInputArgument")
            l.forEach {
                val type = it.getAttr("Type")
                val default = it.getAttr("DefaultValue")
                result += it.getVariableName("Name") + ":"
                result += when (type) {
                    "MA Type" -> "MAType = MAType.Ema"
                    "Double Array" -> "DoubleArray = $default"
                    "Integer Array" -> "IntArray = $default"
                    "Double" -> "Double =  $default"
                    "Integer" -> "Int = $default"
                    else -> {
                        "Any"
                    }
                }
                result += ","
            }
            return result.toString()
        }


    companion object {
        val startCode = """
        @file:Suppress(
            "MemberVisibilityCanBePrivate",
            "unused",
            "LargeClass",
            "TooManyFunctions",
            "WildcardImport",
            "MaxLineLength",
            "LongParameterList", "GrazieInspection", "SpellCheckingInspection"
        )
        package org.roboquant.ta
        
        import com.tictactec.ta.lib.*
        import org.roboquant.common.DoesNotComputeException
        import org.roboquant.common.InsufficientDataException
        import org.roboquant.strategies.utils.PriceBarSeries
        
        /**
         * This class wraps the excellent TA-Lib library and makes it easy to use indicators provided by that library. 
         * This wrapper is optimized for usage within roboquant and supports streaming/event based updates. 
         *
         * @see TaLibMetric
         * @see TaLibStrategy
         *    
         * @property core The TaLib core library that does the actual execution of the indicactors
         */
        class TaLib(var core:Core = Core()) {
        
    """.trimIndent()
    }

    private fun getOutputDecl(): String {
        val result = StringBuffer()

        val l = getList("OutputArgument")
        var cnt = 1
        l.forEach {
            val type = it.getAttr("Type")
            result += when (type) {
                "Integer Array" -> "val output$cnt = IntArray(1)\n"
                "Double Array" -> "val output$cnt = DoubleArray(1)\n"
                else -> {
                    throw Exception("unexpected output type $type")
                }
            }
            cnt++
        }
        return result.toString().trimIndent()
    }


    private fun returnStatement(): String {
        if (patternRecognition) return "output1[0] != 0"

        val l = getList("OutputArgument")
        return when (l.size) {
            1 -> "output1[0]"
            2 -> "Pair(output1[0], output2[0])"
            3 -> "Triple(output1[0], output2[0], output3[0])"
            else -> {
                throw Exception("unexpected return size")
            }
        }
    }

    private fun returnType(): String {
        if (patternRecognition) return "Boolean"

        val l = getList("OutputArgument")
        val type = if (l.first().getAttr("Type") == "Integer Array") "Int" else "Double"
        return when (l.size) {
            1 -> type
            2 -> "Pair<$type, $type>"
            3 -> "Triple<$type, $type, $type>"
            else -> {
                throw Exception("unexpected return size")
            }
        }
    }

    /**
     * Build method that only returns a single value.
     *
     * @return
     */
    fun genMethod(): String {

        return """
            
        /**
         * Calculate **$desc** using the provided input data and by default return the most recent result. 
         * You can set [previous] if you don't want the most recent result.
         * If there is insufficient data to calculate the indicators, an [InsufficientDataException] will be thrown.
         *
         * This indicator belongs to the group **$groupId**.
         */
        fun $fixedFnName(${getInput(true)}, $constructor previous:Int=0): ${returnType()} {
            val endIdx = $firstInput.lastIndex - previous
            ${getOutputDecl()}
            val startOutput = MInteger()
            val endOutput = MInteger()
            val ret = core.$fnName(endIdx, endIdx, $inputParams, $optional startOutput, endOutput, ${getOutputNames()})
            if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
            val last = endOutput.value - 1
            if (last < 0) {
                val lookback = core.${callLookback()} + previous
                throw InsufficientDataException("Not enough data to calculate $fixedFnName, minimal lookback period is ${'$'}lookback")
            }
            return ${returnStatement()}
        }

        ${genPriceBarSeriesMethod()}

    """.trimIndent()
    }

    /**
     * Generate small util method for easy using price bar series as input variables.
     *
     * @return
     */
    private fun genPriceBarSeriesMethod(): String {
        val result = StringBuffer()
        val l = getList("RequiredInputArgument")
        val name = l.first().getVariableName("Name")
        if (name in setOf("data") && l.size == 1) {
            result += "fun $fixedFnName(series: PriceBarSeries, $constructor previous:Int = 0) = $fixedFnName(series.close,"
            result += "$optional previous)"
        }

        if (name in setOf("open", "close", "high", "low", "volume")) {
            result += "fun $fixedFnName(series: PriceBarSeries, $constructor previous:Int = 0) = $fixedFnName("
            l.forEach {
                val attr = it.getVariableName("Name")
                result += "series.$attr,"
            }
            result += "$optional previous)"
        }
        return if (result.isNotEmpty()) {
            """
                
            /**
             * Convencience method that allows to use a price-bar [series] as input.
             * @see [$fixedFnName]
             */ 
             """.trimIndent() + "\n$result"
        } else ""
    }

}

/**
 * Generate the two wrappers for the TA-Lib indicators. It works of the ta_func_api.json file that contains the required
 * metadata for these indicators.
 *
 * This ideally could be a Kotlin script, but it doesn't compile due to some compatibility issues with the GSON library.
 * So for now just a plain Kotlin file with a main method.
 */
fun main() {
    println("starting")
    val f = File("ta_func_api.json")
    require(f.isFile) { "ta_func_api.json file not found, run: kotlinc -script script/TaLibGenerator.main.kts" }

    val jsonTree = JsonParser.parseString(f.readText())
    val obj = jsonTree.asJsonObject

    val l = obj.getAsJsonObject("FinancialFunctions").getAsJsonArray("FinancialFunction")
    println("found ${l.size()} functions")

    val sb1 = StringBuffer()
    sb1 += TaLibGenerator.startCode
    l.forEach {
        val el = it as JsonObject
        val b = TaLibGenerator(el)
        sb1 += b.genMethod()
    }
    sb1 += "}\n\n"
    println("Generated ${sb1.lines().size} lines of code")
    val file1 = File("TaLib.kt")
    file1.writeText(sb1.toString())
}

main()

