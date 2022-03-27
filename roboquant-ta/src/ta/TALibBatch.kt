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

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package org.roboquant.ta

import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MAType
import com.tictactec.ta.lib.MInteger
import com.tictactec.ta.lib.RetCode

/**
 * TALib wrapper that supports the standard (batch oriented) API. So when invoking a method, you typically get
 * back an array with multiple results.
 *
 */
object TALibBatch {

    var core: Core = Core()

    /**
     * Apply the Vector Trigonometric ACos on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun acos(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.acos(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate acos")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Chaikin A/D Line on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Volume Indicators.
     *
     */
    fun ad(high: DoubleArray, low: DoubleArray, close: DoubleArray, volume: DoubleArray): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ad(0, endIdx, high, low, close, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate ad")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Arithmetic Add on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun add(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.size - 1
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.add(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate add")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Chaikin A/D Oscillator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Volume Indicators.
     *
     */
    fun adOsc(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        volume: DoubleArray,
        fastPeriod: Int = 3,
        slowPeriod: Int = 10,
    ): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.adOsc(0, endIdx, high, low, close, volume, fastPeriod, slowPeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate adOsc")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Average Directional Movement Index on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun adx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adx(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate adx")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Average Directional Movement Index Rating on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun adxr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adxr(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate adxr")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Absolute Price Oscillator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun apo(data: DoubleArray, fastPeriod: Int = 12, slowPeriod: Int = 26, mAType: MAType = MAType.Ema): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.apo(0, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate apo")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Aroon on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun aroon(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): Pair<DoubleArray, DoubleArray> {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroon(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate aroon")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Aroon Oscillator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun aroonOsc(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroonOsc(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate aroonOsc")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric ASin on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun asin(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.asin(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate asin")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric ATan on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun atan(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atan(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate atan")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Average True Range on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Volatility Indicators.
     *
     */
    fun atr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atr(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate atr")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Average Price on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Price Transform.
     *
     */
    fun avgPrice(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.avgPrice(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate avgPrice")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Bollinger Bands on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun bbands(
        data: DoubleArray,
        timePeriod: Int = 5,
        deviationsup: Double = 2.000000e+0,
        deviationsdown: Double = 2.000000e+0,
        mAType: MAType = MAType.Ema,
    ): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bbands(
            0,
            endIdx,
            data,
            timePeriod,
            deviationsup,
            deviationsdown,
            mAType,
            startOutput,
            endOutput,
            output1,
            output2,
            output3
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate bbands")
        return Triple(output1.copyOfRange(0, last), output2.copyOfRange(0, last), output3.copyOfRange(0, last))
    }

    /**
     * Apply the Beta on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun beta(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 5): DoubleArray {
        val endIdx = data0.size - 1
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.beta(0, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate beta")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Balance Of Power on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun bop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bop(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate bop")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Commodity Channel Index on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun cci(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cci(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cci")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Two Crows on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl2Crows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl2Crows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl2Crows")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Three Black Crows on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl3BlackCrows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3BlackCrows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl3BlackCrows")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Three Inside Up/Down on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl3Inside(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Inside(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl3Inside")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Three-Line Strike on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl3LineStrike(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3LineStrike(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl3LineStrike")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Three Outside Up/Down on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl3Outside(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Outside(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl3Outside")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Three Stars In The South on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl3StarsInSouth(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3StarsInSouth(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl3StarsInSouth")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Three Advancing White Soldiers on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdl3WhiteSoldiers(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3WhiteSoldiers(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdl3WhiteSoldiers")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Abandoned Baby on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlAbandonedBaby(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlAbandonedBaby(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlAbandonedBaby")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Advance Block on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlAdvanceBlock(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlAdvanceBlock(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlAdvanceBlock")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Belt-hold on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlBeltHold(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBeltHold(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlBeltHold")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Breakaway on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlBreakaway(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBreakaway(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlBreakaway")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Closing Marubozu on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlClosingMarubozu(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlClosingMarubozu(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlClosingMarubozu")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Concealing Baby Swallow on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlConcealBabysWall(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlConcealBabysWall(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlConcealBabysWall")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Counterattack on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlCounterAttack(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlCounterAttack(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlCounterAttack")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Dark Cloud Cover on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlDarkCloudCover(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 5.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlDarkCloudCover(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlDarkCloudCover")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Doji on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlDoji")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Doji Star on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlDojiStar(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDojiStar(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlDojiStar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Dragonfly Doji on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlDragonflyDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDragonflyDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlDragonflyDoji")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Engulfing Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlEngulfing(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEngulfing(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlEngulfing")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Evening Doji Star on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlEveningDojiStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlEveningDojiStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlEveningDojiStar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Evening Star on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlEveningStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEveningStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlEveningStar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Up/Down-gap side-by-side white lines on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlGapSideSideWhite(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGapSideSideWhite(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlGapSideSideWhite")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Gravestone Doji on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlGravestoneDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGravestoneDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlGravestoneDoji")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hammer on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHammer(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHammer(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHammer")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hanging Man on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHangingMan(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHangingMan(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHangingMan")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Harami Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHarami(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHarami(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHarami")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Harami Cross Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHaramiCross(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHaramiCross(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHaramiCross")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the High-Wave Candle on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHignWave(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHignWave(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHignWave")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hikkake Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHikkake(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkake(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHikkake")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Modified Hikkake Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHikkakeMod(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkakeMod(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHikkakeMod")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Homing Pigeon on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlHomingPigeon(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHomingPigeon(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlHomingPigeon")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Identical Three Crows on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlIdentical3Crows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlIdentical3Crows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlIdentical3Crows")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the In-Neck Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlInNeck(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInNeck(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlInNeck")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Inverted Hammer on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlInvertedHammer(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInvertedHammer(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlInvertedHammer")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Kicking on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlKicking(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKicking(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlKicking")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Kicking - bull/bear determined by the longer marubozu on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlKickingByLength(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKickingByLength(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlKickingByLength")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Ladder Bottom on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlLadderBottom(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLadderBottom(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlLadderBottom")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Long Legged Doji on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlLongLeggedDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLeggedDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlLongLeggedDoji")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Long Line Candle on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlLongLine(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLine(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlLongLine")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Marubozu on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlMarubozu(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMarubozu(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlMarubozu")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Matching Low on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlMatchingLow(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatchingLow(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlMatchingLow")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Mat Hold on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlMatHold(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 5.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatHold(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlMatHold")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Morning Doji Star on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlMorningDojiStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlMorningDojiStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlMorningDojiStar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Morning Star on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlMorningStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMorningStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlMorningStar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the On-Neck Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlOnNeck(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlOnNeck(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlOnNeck")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Piercing Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlPiercing(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlPiercing(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlPiercing")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Rickshaw Man on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlRickshawMan(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRickshawMan(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlRickshawMan")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Rising/Falling Three Methods on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlRiseFall3Methods(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRiseFall3Methods(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlRiseFall3Methods")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Separating Lines on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlSeperatingLines(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSeperatingLines(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlSeperatingLines")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Shooting Star on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlShootingStar(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShootingStar(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlShootingStar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Short Line Candle on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlShortLine(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShortLine(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlShortLine")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Spinning Top on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlSpinningTop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSpinningTop(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlSpinningTop")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Stalled Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlStalledPattern(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStalledPattern(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlStalledPattern")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Stick Sandwich on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlStickSandwhich(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStickSandwhich(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlStickSandwhich")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Takuri (Dragonfly Doji with very long lower shadow) on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlTakuri(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTakuri(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlTakuri")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Tasuki Gap on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlTasukiGap(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTasukiGap(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlTasukiGap")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Thrusting Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlThrusting(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlThrusting(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlThrusting")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Tristar Pattern on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlTristar(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTristar(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlTristar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Unique 3 River on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlUnique3River(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUnique3River(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlUnique3River")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Upside Gap Two Crows on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlUpsideGap2Crows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUpsideGap2Crows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlUpsideGap2Crows")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Upside/Downside Gap Three Methods on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Pattern Recognition.
     *
     */
    fun cdlXSideGap3Methods(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.size - 1
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlXSideGap3Methods(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cdlXSideGap3Methods")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Ceil on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun ceil(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ceil(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate ceil")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Chande Momentum Oscillator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun cmo(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cmo(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cmo")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Pearson's Correlation Coefficient (r) on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun correl(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data0.size - 1
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.correl(0, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate correl")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric Cos on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun cos(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cos(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cos")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric Cosh on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun cosh(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cosh(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate cosh")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Double Exponential Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun dema(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dema(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate dema")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Arithmetic Div on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun div(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.size - 1
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.div(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate div")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Directional Movement Index on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun dx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dx(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate dx")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Exponential Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun ema(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ema(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate ema")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Arithmetic Exp on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun exp(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.exp(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate exp")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Floor on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun floor(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.floor(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate floor")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hilbert Transform - Dominant Cycle Period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Cycle Indicators.
     *
     */
    fun htDcPeriod(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPeriod(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate htDcPeriod")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hilbert Transform - Dominant Cycle Phase on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Cycle Indicators.
     *
     */
    fun htDcPhase(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPhase(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate htDcPhase")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hilbert Transform - Phasor Components on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Cycle Indicators.
     *
     */
    fun htPhasor(data: DoubleArray): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htPhasor(0, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate htPhasor")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Hilbert Transform - SineWave on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Cycle Indicators.
     *
     */
    fun htSine(data: DoubleArray): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htSine(0, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate htSine")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Hilbert Transform - Instantaneous Trendline on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun htTrendline(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendline(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate htTrendline")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Hilbert Transform - Trend vs Cycle Mode on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Cycle Indicators.
     *
     */
    fun htTrendMode(data: DoubleArray): IntArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendMode(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate htTrendMode")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Kaufman Adaptive Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun kama(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.kama(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate kama")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Linear Regression on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun linearReg(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearReg(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate linearReg")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Linear Regression Angle on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun linearRegAngle(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegAngle(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate linearRegAngle")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Linear Regression Intercept on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun linearRegIntercept(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegIntercept(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate linearRegIntercept")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Linear Regression Slope on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun linearRegSlope(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegSlope(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate linearRegSlope")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Log Natural on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun ln(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ln(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate ln")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Log10 on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun log10(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.log10(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate log10")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Moving average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun movingAverage(data: DoubleArray, timePeriod: Int = 30, mAType: MAType = MAType.Ema): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverage(0, endIdx, data, timePeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate movingAverage")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Moving Average Convergence/Divergence on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun macd(
        data: DoubleArray,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9,
    ): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macd(
            0,
            endIdx,
            data,
            fastPeriod,
            slowPeriod,
            signalPeriod,
            startOutput,
            endOutput,
            output1,
            output2,
            output3
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate macd")
        return Triple(output1.copyOfRange(0, last), output2.copyOfRange(0, last), output3.copyOfRange(0, last))
    }

    /**
     * Apply the MACD with controllable MA type on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun macdExt(
        data: DoubleArray,
        fastPeriod: Int = 12,
        fastMA: MAType = MAType.Ema,
        slowPeriod: Int = 26,
        slowMA: MAType = MAType.Ema,
        signalPeriod: Int = 9,
        signalMA: MAType = MAType.Ema,
    ): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdExt(
            0,
            endIdx,
            data,
            fastPeriod,
            fastMA,
            slowPeriod,
            slowMA,
            signalPeriod,
            signalMA,
            startOutput,
            endOutput,
            output1,
            output2,
            output3
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate macdExt")
        return Triple(output1.copyOfRange(0, last), output2.copyOfRange(0, last), output3.copyOfRange(0, last))
    }

    /**
     * Apply the Moving Average Convergence/Divergence Fix 12/26 on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun macdFix(data: DoubleArray, signalPeriod: Int = 9): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdFix(0, endIdx, data, signalPeriod, startOutput, endOutput, output1, output2, output3)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate macdFix")
        return Triple(output1.copyOfRange(0, last), output2.copyOfRange(0, last), output3.copyOfRange(0, last))
    }

    /**
     * Apply the MESA Adaptive Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun mama(
        data: DoubleArray,
        fastLimit: Double = 5.000000e-1,
        slowLimit: Double = 5.000000e-2,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mama(0, endIdx, data, fastLimit, slowLimit, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate mama")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Moving average with variable period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun movingAverageVariablePeriod(
        data: DoubleArray,
        inPeriods: DoubleArray,
        minimumPeriod: Int = 2,
        maximumPeriod: Int = 30,
        mAType: MAType = MAType.Ema,
    ): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverageVariablePeriod(
            0,
            endIdx,
            data,
            inPeriods,
            minimumPeriod,
            maximumPeriod,
            mAType,
            startOutput,
            endOutput,
            output1
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate movingAverageVariablePeriod")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Highest value over a specified period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun max(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.max(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate max")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Index of highest value over a specified period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun maxIndex(data: DoubleArray, timePeriod: Int = 30): IntArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.maxIndex(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate maxIndex")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Median Price on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Price Transform.
     *
     */
    fun medPrice(high: DoubleArray, low: DoubleArray): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.medPrice(0, endIdx, high, low, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate medPrice")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Money Flow Index on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun mfi(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        volume: DoubleArray,
        timePeriod: Int = 14,
    ): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mfi(0, endIdx, high, low, close, volume, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate mfi")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the MidPoint over period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun midPoint(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPoint(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate midPoint")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Midpoint Price over period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun midPrice(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPrice(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate midPrice")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Lowest value over a specified period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun min(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.min(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate min")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Index of lowest value over a specified period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun minIndex(data: DoubleArray, timePeriod: Int = 30): IntArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minIndex(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate minIndex")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Lowest and highest values over a specified period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun minMax(data: DoubleArray, timePeriod: Int = 30): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMax(0, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate minMax")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Indexes of lowest and highest values over a specified period on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun minMaxIndex(data: DoubleArray, timePeriod: Int = 30): Pair<IntArray, IntArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val output2 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMaxIndex(0, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate minMaxIndex")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Minus Directional Indicator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun minusDI(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDI(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate minusDI")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Minus Directional Movement on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun minusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDM(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate minusDM")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Momentum on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun mom(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mom(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate mom")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Arithmetic Mult on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun mult(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.size - 1
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mult(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate mult")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Normalized Average True Range on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Volatility Indicators.
     *
     */
    fun natr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.natr(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate natr")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the On Balance Volume on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Volume Indicators.
     *
     */
    fun obv(data: DoubleArray, volume: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.obv(0, endIdx, data, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate obv")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Plus Directional Indicator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun plusDI(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDI(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate plusDI")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Plus Directional Movement on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun plusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDM(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate plusDM")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Percentage Price Oscillator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun ppo(data: DoubleArray, fastPeriod: Int = 12, slowPeriod: Int = 26, mAType: MAType = MAType.Ema): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ppo(0, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate ppo")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Rate of change : ((price/prevPrice)-1)*100 on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun roc(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.roc(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate roc")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Rate of change Percentage: (price-prevPrice)/prevPrice on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun rocP(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocP(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate rocP")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Rate of change ratio: (price/prevPrice) on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun rocR(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate rocR")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Rate of change ratio 100 scale: (price/prevPrice)*100 on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun rocR100(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR100(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate rocR100")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Relative Strength Index on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun rsi(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rsi(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate rsi")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Parabolic SAR on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun sar(
        high: DoubleArray,
        low: DoubleArray,
        accelerationFactor: Double = 2.000000e-2,
        aFMaximum: Double = 2.000000e-1,
    ): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sar(0, endIdx, high, low, accelerationFactor, aFMaximum, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sar")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Parabolic SAR - Extended on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun sarExt(
        high: DoubleArray,
        low: DoubleArray,
        startValue: Double = 0.000000e+0,
        offsetonReverse: Double = 0.000000e+0,
        aFInitLong: Double = 2.000000e-2,
        aFLong: Double = 2.000000e-2,
        aFMaxLong: Double = 2.000000e-1,
        aFInitShort: Double = 2.000000e-2,
        aFShort: Double = 2.000000e-2,
        aFMaxShort: Double = 2.000000e-1,
    ): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sarExt(
            0,
            endIdx,
            high,
            low,
            startValue,
            offsetonReverse,
            aFInitLong,
            aFLong,
            aFMaxLong,
            aFInitShort,
            aFShort,
            aFMaxShort,
            startOutput,
            endOutput,
            output1
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sarExt")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric Sin on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun sin(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sin(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sin")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric Sinh on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun sinh(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sinh(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sinh")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Simple Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun sma(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sma(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sma")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Square Root on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun sqrt(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sqrt(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sqrt")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Standard Deviation on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun stdDev(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stdDev(0, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate stdDev")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Stochastic on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun stoch(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        fastKPeriod: Int = 5,
        slowKPeriod: Int = 3,
        slowKMA: MAType = MAType.Ema,
        slowDPeriod: Int = 3,
        slowDMA: MAType = MAType.Ema,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stoch(
            0,
            endIdx,
            high,
            low,
            close,
            fastKPeriod,
            slowKPeriod,
            slowKMA,
            slowDPeriod,
            slowDMA,
            startOutput,
            endOutput,
            output1,
            output2
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate stoch")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Stochastic Fast on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun stochF(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stochF(
            0,
            endIdx,
            high,
            low,
            close,
            fastKPeriod,
            fastDPeriod,
            fastDMA,
            startOutput,
            endOutput,
            output1,
            output2
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate stochF")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Stochastic Relative Strength Index on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun stochRsi(
        data: DoubleArray,
        timePeriod: Int = 14,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stochRsi(
            0,
            endIdx,
            data,
            timePeriod,
            fastKPeriod,
            fastDPeriod,
            fastDMA,
            startOutput,
            endOutput,
            output1,
            output2
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate stochRsi")
        return Pair(output1.copyOfRange(0, last), output2.copyOfRange(0, last))
    }

    /**
     * Apply the Vector Arithmetic Subtraction on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun sub(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.size - 1
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sub(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sub")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Summation on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Operators.
     *
     */
    fun sum(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sum(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate sum")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Triple Exponential Moving Average (T3) on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun t3(data: DoubleArray, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.t3(0, endIdx, data, timePeriod, volumeFactor, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate t3")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric Tan on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun tan(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tan(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate tan")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Vector Trigonometric Tanh on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Math Transform.
     *
     */
    fun tanh(data: DoubleArray): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tanh(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate tanh")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Triple Exponential Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun tema(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tema(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate tema")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the True Range on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Volatility Indicators.
     *
     */
    fun trueRange(high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trueRange(0, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate trueRange")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Triangular Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun trima(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trima(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate trima")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the 1-day Rate-Of-Change (ROC) of a Triple Smooth EMA on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun trix(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trix(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate trix")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Time Series Forecast on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun tsf(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tsf(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate tsf")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Typical Price on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Price Transform.
     *
     */
    fun typPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.typPrice(0, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate typPrice")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Ultimate Oscillator on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun ultOsc(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        firstPeriod: Int = 7,
        secondPeriod: Int = 14,
        thirdPeriod: Int = 28,
    ): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ultOsc(
            0,
            endIdx,
            high,
            low,
            close,
            firstPeriod,
            secondPeriod,
            thirdPeriod,
            startOutput,
            endOutput,
            output1
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate ultOsc")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Variance on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Statistic Functions.
     *
     */
    fun variance(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.variance(0, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate variance")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Weighted Close Price on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Price Transform.
     *
     */
    fun wclPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wclPrice(0, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate wclPrice")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Williams' %R on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Momentum Indicators.
     *
     */
    fun willR(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.size - 1
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.willR(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate willR")
        return output1.copyOfRange(0, last)
    }

    /**
     * Apply the Weighted Moving Average on the provided input and return the result as an array.
     *
     * This indicator belongs to the group Overlap Studies.
     *
     */
    fun wma(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.size - 1
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wma(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        if (last < 0) throw InsufficientData("Not enough data available to calculate wma")
        return output1.copyOfRange(0, last)
    }
}