@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package org.roboquant.ta

import com.tictactec.ta.lib.*

/**
 * TALib wrapper that supports the standard (batch oriented) API. So when invoking a method, you typically get
 * back an array with multiple results.
 *
 */
object TALibBatch {

    var core: Core = Core()

    /**
     * Apply Vector Trigonometric ACos on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun acos(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.acos(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate acos") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Chaikin A/D Line on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volume Indicators.
     */
    fun ad(high: DoubleArray, low: DoubleArray, close: DoubleArray, volume: DoubleArray): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ad(0, endIdx, high, low, close, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate ad") else output1.copyOfRange(0, last)
    }

    /**
     * Apply Vector Arithmetic Add on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun add(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.lastIndex
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.add(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate add") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Chaikin A/D Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volume Indicators.
     */
    fun adOsc(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        volume: DoubleArray,
        fastPeriod: Int = 3,
        slowPeriod: Int = 10,
    ): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.adOsc(0, endIdx, high, low, close, volume, fastPeriod, slowPeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate adOsc") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Average Directional Movement Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun adx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adx(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate adx") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Average Directional Movement Index Rating on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun adxr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adxr(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate adxr") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Absolute Price Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun apo(data: DoubleArray, fastPeriod: Int = 12, slowPeriod: Int = 26, mAType: MAType = MAType.Ema): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.apo(0, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate apo") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Aroon on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun aroon(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): Pair<DoubleArray, DoubleArray> {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroon(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate aroon") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Aroon Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun aroonOsc(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroonOsc(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate aroonOsc") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric ASin on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun asin(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.asin(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate asin") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric ATan on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun atan(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atan(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate atan") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Average True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun atr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atr(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate atr") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Average Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun avgPrice(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.avgPrice(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate avgPrice") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Bollinger Bands on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun bbands(
        data: DoubleArray,
        timePeriod: Int = 5,
        deviationsup: Double = 2.000000e+0,
        deviationsdown: Double = 2.000000e+0,
        mAType: MAType = MAType.Ema,
    ): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate bbands") else Triple(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last), output3.copyOfRange(0, last)
        )
    }

    /**
     * Apply Beta on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun beta(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 5): DoubleArray {
        val endIdx = data0.lastIndex
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.beta(0, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate beta") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Balance Of Power on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun bop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bop(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate bop") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Commodity Channel Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun cci(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cci(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cci") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Two Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl2Crows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl2Crows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl2Crows") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Three Black Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3BlackCrows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3BlackCrows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3BlackCrows") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Three Inside Up/Down on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3Inside(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Inside(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3Inside") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Three-Line Strike on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3LineStrike(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3LineStrike(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3LineStrike") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Three Outside Up/Down on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3Outside(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Outside(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3Outside") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Three Stars In The South on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3StarsInSouth(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3StarsInSouth(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3StarsInSouth") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Three Advancing White Soldiers on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3WhiteSoldiers(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3WhiteSoldiers(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3WhiteSoldiers") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Abandoned Baby on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlAbandonedBaby(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlAbandonedBaby(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlAbandonedBaby") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Advance Block on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlAdvanceBlock(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlAdvanceBlock(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlAdvanceBlock") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Belt-hold on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlBeltHold(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBeltHold(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlBeltHold") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Breakaway on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlBreakaway(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBreakaway(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlBreakaway") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Closing Marubozu on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlClosingMarubozu(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlClosingMarubozu(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlClosingMarubozu") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Concealing Baby Swallow on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlConcealBabysWall(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlConcealBabysWall(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlConcealBabysWall") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Counterattack on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlCounterAttack(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlCounterAttack(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlCounterAttack") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Dark Cloud Cover on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDarkCloudCover(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 5.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlDarkCloudCover(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDarkCloudCover") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDoji") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Doji Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDojiStar(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDojiStar(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDojiStar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Dragonfly Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDragonflyDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDragonflyDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDragonflyDoji") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Engulfing Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlEngulfing(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEngulfing(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlEngulfing") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Evening Doji Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlEveningDojiStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlEveningDojiStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlEveningDojiStar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Evening Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlEveningStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEveningStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlEveningStar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Up/Down-gap side-by-side white lines on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlGapSideSideWhite(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGapSideSideWhite(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlGapSideSideWhite") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Gravestone Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlGravestoneDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGravestoneDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlGravestoneDoji") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hammer on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHammer(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHammer(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHammer") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hanging Man on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHangingMan(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHangingMan(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHangingMan") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Harami Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHarami(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHarami(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHarami") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Harami Cross Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHaramiCross(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHaramiCross(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHaramiCross") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply High-Wave Candle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHignWave(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHignWave(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHignWave") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hikkake Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHikkake(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkake(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHikkake") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Modified Hikkake Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHikkakeMod(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkakeMod(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHikkakeMod") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Homing Pigeon on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHomingPigeon(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHomingPigeon(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHomingPigeon") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Identical Three Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlIdentical3Crows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlIdentical3Crows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlIdentical3Crows") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply In-Neck Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlInNeck(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInNeck(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlInNeck") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Inverted Hammer on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlInvertedHammer(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInvertedHammer(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlInvertedHammer") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Kicking on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlKicking(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKicking(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlKicking") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Kicking - bull/bear determined by the longer marubozu on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlKickingByLength(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKickingByLength(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlKickingByLength") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Ladder Bottom on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlLadderBottom(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLadderBottom(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlLadderBottom") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Long Legged Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlLongLeggedDoji(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLeggedDoji(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlLongLeggedDoji") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Long Line Candle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlLongLine(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLine(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlLongLine") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Marubozu on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMarubozu(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMarubozu(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMarubozu") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Matching Low on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMatchingLow(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatchingLow(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMatchingLow") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Mat Hold on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMatHold(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 5.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatHold(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMatHold") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Morning Doji Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMorningDojiStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlMorningDojiStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMorningDojiStar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Morning Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMorningStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        penetration: Double = 3.000000e-1,
    ): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMorningStar(0, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMorningStar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply On-Neck Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlOnNeck(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlOnNeck(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlOnNeck") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Piercing Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlPiercing(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlPiercing(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlPiercing") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Rickshaw Man on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlRickshawMan(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRickshawMan(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlRickshawMan") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Rising/Falling Three Methods on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlRiseFall3Methods(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRiseFall3Methods(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlRiseFall3Methods") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Separating Lines on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlSeperatingLines(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSeperatingLines(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlSeperatingLines") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Shooting Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlShootingStar(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShootingStar(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlShootingStar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Short Line Candle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlShortLine(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShortLine(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlShortLine") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Spinning Top on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlSpinningTop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSpinningTop(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlSpinningTop") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Stalled Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlStalledPattern(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStalledPattern(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlStalledPattern") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Stick Sandwich on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlStickSandwhich(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStickSandwhich(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlStickSandwhich") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Takuri (Dragonfly Doji with very long lower shadow) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlTakuri(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTakuri(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlTakuri") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Tasuki Gap on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlTasukiGap(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTasukiGap(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlTasukiGap") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Thrusting Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlThrusting(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlThrusting(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlThrusting") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Tristar Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlTristar(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTristar(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlTristar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Unique 3 River on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlUnique3River(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUnique3River(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlUnique3River") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Upside Gap Two Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlUpsideGap2Crows(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUpsideGap2Crows(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlUpsideGap2Crows") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Upside/Downside Gap Three Methods on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlXSideGap3Methods(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray): IntArray {
        val endIdx = open.lastIndex
        val outputSize = open.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlXSideGap3Methods(0, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlXSideGap3Methods") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Ceil on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun ceil(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ceil(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate ceil") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Chande Momentum Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun cmo(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cmo(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cmo") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Pearson's Correlation Coefficient (r) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun correl(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data0.lastIndex
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.correl(0, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate correl") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric Cos on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun cos(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cos(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cos") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric Cosh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun cosh(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cosh(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate cosh") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Double Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun dema(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dema(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate dema") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Arithmetic Div on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun div(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.lastIndex
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.div(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate div") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Directional Movement Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun dx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dx(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate dx") else output1.copyOfRange(0, last)
    }

    /**
     * Apply Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun ema(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ema(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate ema") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Arithmetic Exp on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun exp(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.exp(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate exp") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Floor on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun floor(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.floor(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate floor") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hilbert Transform - Dominant Cycle Period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htDcPeriod(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPeriod(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate htDcPeriod") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hilbert Transform - Dominant Cycle Phase on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htDcPhase(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPhase(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate htDcPhase") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hilbert Transform - Phasor Components on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htPhasor(data: DoubleArray): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htPhasor(0, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate htPhasor") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Hilbert Transform - SineWave on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htSine(data: DoubleArray): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htSine(0, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate htSine") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Hilbert Transform - Instantaneous Trendline on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun htTrendline(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendline(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate htTrendline") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Hilbert Transform - Trend vs Cycle Mode on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htTrendMode(data: DoubleArray): IntArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendMode(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate htTrendMode") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Kaufman Adaptive Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun kama(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.kama(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate kama") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Linear Regression on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearReg(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearReg(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearReg") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Linear Regression Angle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegAngle(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegAngle(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearRegAngle") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Linear Regression Intercept on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegIntercept(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegIntercept(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearRegIntercept") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Linear Regression Slope on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegSlope(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegSlope(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearRegSlope") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Log Natural on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun ln(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ln(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate ln") else output1.copyOfRange(0, last)
    }

    /**
     * Apply Vector Log10 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun log10(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.log10(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate log10") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Moving average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun movingAverage(data: DoubleArray, timePeriod: Int = 30, mAType: MAType = MAType.Ema): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverage(0, endIdx, data, timePeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate movingAverage") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Moving Average Convergence/Divergence on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun macd(
        data: DoubleArray,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9,
    ): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate macd") else Triple(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last), output3.copyOfRange(0, last)
        )
    }

    /**
     * Apply MACD with controllable MA type on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
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
        val endIdx = data.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate macdExt") else Triple(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last), output3.copyOfRange(0, last)
        )
    }

    /**
     * Apply Moving Average Convergence/Divergence Fix 12/26 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun macdFix(data: DoubleArray, signalPeriod: Int = 9): Triple<DoubleArray, DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdFix(0, endIdx, data, signalPeriod, startOutput, endOutput, output1, output2, output3)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate macdFix") else Triple(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last), output3.copyOfRange(0, last)
        )
    }

    /**
     * Apply MESA Adaptive Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun mama(
        data: DoubleArray,
        fastLimit: Double = 5.000000e-1,
        slowLimit: Double = 5.000000e-2,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mama(0, endIdx, data, fastLimit, slowLimit, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate mama") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Moving average with variable period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun movingAverageVariablePeriod(
        data: DoubleArray,
        inPeriods: DoubleArray,
        minimumPeriod: Int = 2,
        maximumPeriod: Int = 30,
        mAType: MAType = MAType.Ema,
    ): DoubleArray {
        val endIdx = data.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate movingAverageVariablePeriod") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Highest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun max(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.max(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate max") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Index of highest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun maxIndex(data: DoubleArray, timePeriod: Int = 30): IntArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.maxIndex(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate maxIndex") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Median Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun medPrice(high: DoubleArray, low: DoubleArray): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.medPrice(0, endIdx, high, low, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate medPrice") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Money Flow Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun mfi(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        volume: DoubleArray,
        timePeriod: Int = 14,
    ): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mfi(0, endIdx, high, low, close, volume, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate mfi") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply MidPoint over period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun midPoint(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPoint(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate midPoint") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Midpoint Price over period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun midPrice(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPrice(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate midPrice") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Lowest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun min(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.min(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate min") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Index of lowest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minIndex(data: DoubleArray, timePeriod: Int = 30): IntArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minIndex(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate minIndex") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Lowest and highest values over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minMax(data: DoubleArray, timePeriod: Int = 30): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMax(0, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate minMax") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Indexes of lowest and highest values over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minMaxIndex(data: DoubleArray, timePeriod: Int = 30): Pair<IntArray, IntArray> {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = IntArray(outputSize)
        val output2 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMaxIndex(0, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate minMaxIndex") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Minus Directional Indicator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun minusDI(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDI(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate minusDI") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Minus Directional Movement on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun minusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDM(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate minusDM") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Momentum on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun mom(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mom(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate mom") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Arithmetic Mult on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun mult(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.lastIndex
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mult(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate mult") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Normalized Average True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun natr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.natr(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate natr") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply On Balance Volume on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volume Indicators.
     */
    fun obv(data: DoubleArray, volume: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.obv(0, endIdx, data, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate obv") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Plus Directional Indicator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun plusDI(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDI(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate plusDI") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Plus Directional Movement on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun plusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDM(0, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate plusDM") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Percentage Price Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun ppo(data: DoubleArray, fastPeriod: Int = 12, slowPeriod: Int = 26, mAType: MAType = MAType.Ema): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ppo(0, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate ppo") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Rate of change : ((price/prevPrice)-1)*100 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun roc(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.roc(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate roc") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Rate of change Percentage: (price-prevPrice)/prevPrice on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocP(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocP(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate rocP") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Rate of change ratio: (price/prevPrice) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocR(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate rocR") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Rate of change ratio 100 scale: (price/prevPrice)*100 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocR100(data: DoubleArray, timePeriod: Int = 10): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR100(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate rocR100") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Relative Strength Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rsi(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rsi(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate rsi") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Parabolic SAR on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun sar(
        high: DoubleArray,
        low: DoubleArray,
        accelerationFactor: Double = 2.000000e-2,
        aFMaximum: Double = 2.000000e-1,
    ): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sar(0, endIdx, high, low, accelerationFactor, aFMaximum, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sar") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Parabolic SAR - Extended on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
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
        val endIdx = high.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate sarExt") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric Sin on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sin(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sin(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sin") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric Sinh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sinh(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sinh(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sinh") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Simple Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun sma(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sma(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sma") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Square Root on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sqrt(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sqrt(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sqrt") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Standard Deviation on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun stdDev(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stdDev(0, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate stdDev") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Stochastic on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
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
        val endIdx = high.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate stoch") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Stochastic Fast on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun stochF(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = high.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate stochF") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Stochastic Relative Strength Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun stochRsi(
        data: DoubleArray,
        timePeriod: Int = 14,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
    ): Pair<DoubleArray, DoubleArray> {
        val endIdx = data.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate stochRsi") else Pair(
            output1.copyOfRange(
                0,
                last
            ), output2.copyOfRange(0, last)
        )
    }

    /**
     * Apply Vector Arithmetic Subtraction on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun sub(data0: DoubleArray, data1: DoubleArray): DoubleArray {
        val endIdx = data0.lastIndex
        val outputSize = data0.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sub(0, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sub") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Summation on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun sum(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sum(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate sum") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Triple Exponential Moving Average (T3) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun t3(data: DoubleArray, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.t3(0, endIdx, data, timePeriod, volumeFactor, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate t3") else output1.copyOfRange(0, last)
    }

    /**
     * Apply Vector Trigonometric Tan on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun tan(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tan(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate tan") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Vector Trigonometric Tanh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun tanh(data: DoubleArray): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tanh(0, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate tanh") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Triple Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun tema(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tema(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate tema") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun trueRange(high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trueRange(0, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate trueRange") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Triangular Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun trima(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trima(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate trima") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply 1-day Rate-Of-Change (ROC) of a Triple Smooth EMA on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun trix(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trix(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate trix") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Time Series Forecast on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun tsf(data: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tsf(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate tsf") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Typical Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun typPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.typPrice(0, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate typPrice") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Ultimate Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun ultOsc(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        firstPeriod: Int = 7,
        secondPeriod: Int = 14,
        thirdPeriod: Int = 28,
    ): DoubleArray {
        val endIdx = high.lastIndex
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
        return if (last < 0) throw InsufficientData("Not enough data to calculate ultOsc") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Variance on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun variance(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.variance(0, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate variance") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Weighted Close Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun wclPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wclPrice(0, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate wclPrice") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Williams' %R on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun willR(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14): DoubleArray {
        val endIdx = high.lastIndex
        val outputSize = high.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.willR(0, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate willR") else output1.copyOfRange(
            0,
            last
        )
    }

    /**
     * Apply Weighted Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun wma(data: DoubleArray, timePeriod: Int = 30): DoubleArray {
        val endIdx = data.lastIndex
        val outputSize = data.size
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wma(0, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value
        return if (last < 0) throw InsufficientData("Not enough data to calculate wma") else output1.copyOfRange(
            0,
            last
        )
    }
}

