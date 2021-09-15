package org.roboquant.strategies.ta

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
internal open class BaseWrapper(val root: JsonObject) {

    protected fun JsonObject.getAttr(key:String): String = get(key).asString
    private fun String.unCapitalize() = get(0).lowercase() + substring(1)

    protected fun JsonObject.getVariableName(key: String) : String {
        var result = get(key).asString
        result = result.replace(" ", "")
        result = result.replace("-", "")
        result = result.unCapitalize()
        result = result.replace("inReal", "data")
        return result
    }

    protected fun getList(key: String): List<JsonObject> {
        if (!root.has(key + "s")) return listOf()

        val child = root.getAsJsonObject(key + "s").get(key)
        if (child is JsonArray)
            return child.filterIsInstance<JsonObject>().toList()
        else if (child is JsonObject) {
            return listOf(child)
        }
        return listOf()
    }


    protected val desc = root.getAttr("ShortDescription")
    private val className: String = root.get("CamelCaseName").asString
    protected val fnName = className.unCapitalize()
    protected val inputParams = getInput()
    protected val optional = getOptionalInput()
    protected val firstInput = getList("RequiredInputArgument").first().getVariableName("Name")
    protected val patternRecognition = root.getAttr("GroupId") == "Pattern Recognition"
    protected val groupId = root.getAttr("GroupId")


    private fun getOptionalInput(): String {
        val result = StringBuffer()

        val l = getList("OptionalInputArgument")
        l.forEach {
            result += it.getVariableName("Name") + ","
        }
        return result.toString()
    }


    protected fun getInput(withType: Boolean = false): String {
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


    protected fun getOutputNames(): String {
        var result = ""

        val l = getList("OutputArgument")
        var cnt = 1
        repeat(l.size) {
            result += "output$cnt,"
            cnt++
        }
        return result.removeSuffix(",")
    }

    protected val constructor: String
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
}


/**
 * Generate a Wrapper for the TA-Lib library
 *
 * @property root
 * @constructor Create new TA builder
 */
internal class TALibBatchGenerator(root: JsonObject) : BaseWrapper(root) {

    companion object {
        val startCode =  """
        @file:Suppress("MemberVisibilityCanBePrivate", "unused")
        package org.roboquant.strategies.ta
        
        import com.tictactec.ta.lib.*
       
        /**
         * TALib wrapper that supports the standard (batch oriented) API. So when invoking a method, you typically get 
         * back an array with multiple results. 
         *
         */
        object TALibBatch {

            var core:Core = Core()
            
    """.trimIndent()
    }



    private fun getOutputDecl(): String {
        val result = StringBuffer()

        val l = getList("OutputArgument")
        var cnt = 1
        l.forEach {
            val type = it.getAttr("Type")
            result += when (type) {
                "Integer Array" -> "val output$cnt = IntArray(outputSize)\n"
                "Double Array" -> "val output$cnt = DoubleArray(outputSize)\n"
                else -> {
                    throw Exception("unexpected output type $type")
                }
            }
            cnt++
        }
        return result.toString().trimIndent()
    }


    private fun returnStatement(): String {

        val l = getList("OutputArgument")
        return when(l.size) {
            1 -> "output1.copyOfRange(0, last)"
            2 -> "Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))"
            3 -> "Triple(output1.copyOfRange(0, last), output2.copyOfRange(0, last), output3.copyOfRange(0, last))"
            else -> {
                throw Exception("unexpected return size")
            }
        }
    }


    private fun returnType(): String {

        val l = getList("OutputArgument")
        val type = if (l.first().getAttr("Type") == "Integer Array") "IntArray" else "DoubleArray"
        return when(l.size) {
            1 -> type
            2 -> "Pair<$type, $type>"
            3 -> "Triple<$type, $type, $type>"
            else -> {
                throw Exception("unexpected return size")
            }
        }
    }



    /**
     * Build method that returns all available output values.
     *
     * @return
     */
    fun genMethod(): String {
        return """
            
        /**
         * Apply the $desc on the provided input and return the result as an array.
         *
         * This indicator belongs to the group $groupId.
         *
         */
        fun $fnName(${getInput(true)}, $constructor):  ${returnType()} {
            val endIdx = $firstInput.size-1
            val outputSize = $firstInput.size
            ${getOutputDecl()}
            val startOutput = MInteger()
            val endOutput = MInteger()
            val ret = core.$fnName(0, endIdx, $inputParams, $optional startOutput, endOutput, ${getOutputNames()})
            if (ret != RetCode.Success) throw Exception(ret.toString())
            val last = endOutput.value
            if (last < 0) throw InsufficientData("Not enough data available to calculate $fnName")
            return ${returnStatement()}
        }
    """.trimIndent()
    }




}

/**
 * Generate the wrappers for the TA-Lib library
 *
 * @property root
 * @constructor Create new TA builder
 */
internal class TALibGenerator(root: JsonObject) : BaseWrapper(root) {

    companion object {
        val startCode = """
        @file:Suppress("MemberVisibilityCanBePrivate", "unused")
        package org.roboquant.strategies.ta
        
        import com.tictactec.ta.lib.*
        import org.roboquant.strategies.utils.PriceBarBuffer
        
        /**
         * TALib wrapper that supports the API in a streaming/online context. Calling a method will only return a single
         * value, by default the most recent one, but this can be changed by setting the "previous" argument.
         *
         * For accessing the regular access, see TALibBatch
         */
        object TALib {
        
            var core:Core = Core()
       
    """.trimIndent()
    }

    private fun getOutputDecl(): String {
        val result = StringBuffer()

        val l = getList("OutputArgument")
        var cnt = 1
        l.forEach {
            val type = it.getAttr("Type")
            result += when (type) {
                "Integer Array" -> "val output$cnt = IntArray(outputSize)\n"
                "Double Array" -> "val output$cnt = DoubleArray(outputSize)\n"
                else -> {
                    throw Exception("unexpected output type $type")
                }
            }
            cnt++
        }
        return result.toString().trimIndent()
    }


    private fun returnStatement(): String {
        if (patternRecognition) return "output1[last] != 0"

        val l = getList("OutputArgument")
        return when(l.size) {
            1 -> "output1[last]"
            2 -> "Pair(output1[last], output2[last])"
            3 -> "Triple(output1[last], output2[last], output3[last])"
            else -> {
                throw Exception("unexpected return size")
            }
        }
    }


    private fun returnType(): String {
        if (patternRecognition) return "Boolean"

        val l = getList("OutputArgument")
        val type = if (l.first().getAttr("Type") == "Integer Array") "Int" else "Double"
        return when(l.size) {
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
         * Apply $desc on the provided input and return the most recent output only.
         *
         * This indicator belongs to the group $groupId.
         *
         */
        fun $fnName(${getInput(true)}, $constructor previous:Int=0): ${returnType()} {
            val endIdx = $firstInput.size - 1 - previous
            val outputSize = 1
            ${getOutputDecl()}
            val startOutput = MInteger()
            val endOutput = MInteger()
            val ret = core.$fnName(endIdx, endIdx, $inputParams, $optional startOutput, endOutput, ${getOutputNames()})
            if (ret != RetCode.Success) throw Exception(ret.toString())
            val last = endOutput.value - 1
            if (last < 0) throw InsufficientData("Not enough data available to calculate $fnName")
            return ${returnStatement()}
        }

        ${genPriceBufferMethod()}

    """.trimIndent()
    }



    /**
     * Generate small util method for easy using price bar buffers as input variables.
     *
     * @return
     */
    private fun genPriceBufferMethod(): String {
        val result = StringBuffer()
        val l = getList("RequiredInputArgument")
        val name = l.first().getVariableName("Name")
        if (name in setOf("open", "close", "high", "low", "volume")) {
            result += "fun $fnName(buffer: PriceBarBuffer, $constructor previous:Int = 0) = $fnName("
            l.forEach {
                val attr = it.getVariableName("Name")
                result += "buffer.$attr,"
            }
            result += "$optional previous)"
        }
        return result.toString()
    }

}

/**
 * Generate the two wrappers for the TA-Lib indicators. It works of the ta_func_api.json file that contains the required
 * metadata for these indicators.
 *
 * This ideally could be a Kotlin script, but it doesn't compile due to some compatibility issues with GSON library.
 * So for now just a plain Kotlin file with a main method.
 *
 */
fun main() {
    val f = File("utils/ta_func_api.json")
    require(f.isFile)

    val jsonTree = JsonParser.parseString(f.readText())
    val obj = jsonTree.asJsonObject

    val l = obj.getAsJsonObject("FinancialFunctions").getAsJsonArray("FinancialFunction")

    // First create the TALib object
    val sb1 = StringBuffer()
    sb1 += TALibGenerator.startCode
    l.forEach {
        val el = it as JsonObject
        val b = TALibGenerator(el)
        sb1 += b.genMethod()
    }
    sb1 += "}\n\n"
    val file1 = File("/tmp/TALib.kt")
    file1.writeText(sb1.toString())

    // And now the TALibBatch object
    val sb2 = StringBuffer()
    sb2 += TALibBatchGenerator.startCode
    l.forEach {
        val el = it as JsonObject
        val b = TALibBatchGenerator(el)
        sb2 += b.genMethod()
    }
    sb2 += "}\n\n"
    val file2 = File("/tmp/TALibBatch.kt")
    file2.writeText(sb2.toString())
}



