@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package org.roboquant.ta

import com.tictactec.ta.lib.*
import org.roboquant.strategies.utils.PriceBarBuffer

/**
 * TALib wrapper that supports the API in a streaming/online context. Calling a method will only return a single
 * value, by default the most recent one, but this can be changed by setting the "previous" argument.
 *
 * For accessing the regular access, see TALibBatch
 */
object TALib {

    var core: Core = Core()

    /**
     * Apply Vector Trigonometric ACos on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun acos(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.acos(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate acos") else output1[last]
    }

    /**
     * Apply Chaikin A/D Line on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volume Indicators.
     */
    fun ad(high: DoubleArray, low: DoubleArray, close: DoubleArray, volume: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ad(endIdx, endIdx, high, low, close, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate ad") else output1[last]
    }

    fun ad(buffer: PriceBarBuffer, previous: Int = 0) =
        ad(buffer.high, buffer.low, buffer.close, buffer.volume, previous)

    /**
     * Apply Vector Arithmetic Add on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun add(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.add(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate add") else output1[last]
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
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adOsc(
            endIdx,
            endIdx,
            high,
            low,
            close,
            volume,
            fastPeriod,
            slowPeriod,
            startOutput,
            endOutput,
            output1
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate adOsc") else output1[last]
    }

    fun adOsc(buffer: PriceBarBuffer, fastPeriod: Int = 3, slowPeriod: Int = 10, previous: Int = 0) =
        adOsc(buffer.high, buffer.low, buffer.close, buffer.volume, fastPeriod, slowPeriod, previous)

    /**
     * Apply Average Directional Movement Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun adx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adx(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate adx") else output1[last]
    }

    fun adx(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        adx(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Average Directional Movement Index Rating on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun adxr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adxr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate adxr") else output1[last]
    }

    fun adxr(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        adxr(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Absolute Price Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun apo(
        data: DoubleArray,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.apo(endIdx, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate apo") else output1[last]
    }

    /**
     * Apply Aroon on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun aroon(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Pair<Double, Double> {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroon(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate aroon") else Pair(
            output1[last],
            output2[last]
        )
    }

    fun aroon(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        aroon(buffer.high, buffer.low, timePeriod, previous)

    /**
     * Apply Aroon Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun aroonOsc(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroonOsc(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate aroonOsc") else output1[last]
    }

    fun aroonOsc(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        aroonOsc(buffer.high, buffer.low, timePeriod, previous)

    /**
     * Apply Vector Trigonometric ASin on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun asin(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.asin(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate asin") else output1[last]
    }

    /**
     * Apply Vector Trigonometric ATan on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun atan(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atan(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate atan") else output1[last]
    }

    /**
     * Apply Average True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun atr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate atr") else output1[last]
    }

    fun atr(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        atr(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Average Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun avgPrice(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Double {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.avgPrice(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate avgPrice") else output1[last]
    }

    fun avgPrice(buffer: PriceBarBuffer, previous: Int = 0) =
        avgPrice(buffer.open, buffer.high, buffer.low, buffer.close, previous)

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
        previous: Int = 0
    ): Triple<Double, Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bbands(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate bbands") else Triple(
            output1[last],
            output2[last],
            output3[last]
        )
    }

    /**
     * Apply Beta on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun beta(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 5, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.beta(endIdx, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate beta") else output1[last]
    }

    /**
     * Apply Balance Of Power on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun bop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bop(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate bop") else output1[last]
    }

    fun bop(buffer: PriceBarBuffer, previous: Int = 0) =
        bop(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Commodity Channel Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun cci(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cci(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cci") else output1[last]
    }

    fun cci(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        cci(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Two Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl2Crows(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl2Crows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl2Crows") else output1[last] != 0
    }

    fun cdl2Crows(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl2Crows(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Three Black Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3BlackCrows(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3BlackCrows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3BlackCrows") else output1[last] != 0
    }

    fun cdl3BlackCrows(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl3BlackCrows(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Three Inside Up/Down on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3Inside(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Inside(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3Inside") else output1[last] != 0
    }

    fun cdl3Inside(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl3Inside(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Three-Line Strike on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3LineStrike(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3LineStrike(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3LineStrike") else output1[last] != 0
    }

    fun cdl3LineStrike(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl3LineStrike(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Three Outside Up/Down on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3Outside(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Outside(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3Outside") else output1[last] != 0
    }

    fun cdl3Outside(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl3Outside(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Three Stars In The South on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3StarsInSouth(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3StarsInSouth(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3StarsInSouth") else output1[last] != 0
    }

    fun cdl3StarsInSouth(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl3StarsInSouth(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Three Advancing White Soldiers on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdl3WhiteSoldiers(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3WhiteSoldiers(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdl3WhiteSoldiers") else output1[last] != 0
    }

    fun cdl3WhiteSoldiers(buffer: PriceBarBuffer, previous: Int = 0) =
        cdl3WhiteSoldiers(buffer.open, buffer.high, buffer.low, buffer.close, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlAbandonedBaby(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlAbandonedBaby") else output1[last] != 0
    }

    fun cdlAbandonedBaby(buffer: PriceBarBuffer, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlAbandonedBaby(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

    /**
     * Apply Advance Block on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlAdvanceBlock(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlAdvanceBlock(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlAdvanceBlock") else output1[last] != 0
    }

    fun cdlAdvanceBlock(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlAdvanceBlock(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Belt-hold on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlBeltHold(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBeltHold(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlBeltHold") else output1[last] != 0
    }

    fun cdlBeltHold(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlBeltHold(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Breakaway on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlBreakaway(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBreakaway(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlBreakaway") else output1[last] != 0
    }

    fun cdlBreakaway(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlBreakaway(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Closing Marubozu on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlClosingMarubozu(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlClosingMarubozu(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlClosingMarubozu") else output1[last] != 0
    }

    fun cdlClosingMarubozu(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlClosingMarubozu(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Concealing Baby Swallow on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlConcealBabysWall(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlConcealBabysWall(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlConcealBabysWall") else output1[last] != 0
    }

    fun cdlConcealBabysWall(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlConcealBabysWall(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Counterattack on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlCounterAttack(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlCounterAttack(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlCounterAttack") else output1[last] != 0
    }

    fun cdlCounterAttack(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlCounterAttack(buffer.open, buffer.high, buffer.low, buffer.close, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlDarkCloudCover(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDarkCloudCover") else output1[last] != 0
    }

    fun cdlDarkCloudCover(buffer: PriceBarBuffer, penetration: Double = 5.000000e-1, previous: Int = 0) =
        cdlDarkCloudCover(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

    /**
     * Apply Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDoji(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDoji") else output1[last] != 0
    }

    fun cdlDoji(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlDoji(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Doji Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDojiStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDojiStar(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDojiStar") else output1[last] != 0
    }

    fun cdlDojiStar(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlDojiStar(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Dragonfly Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlDragonflyDoji(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDragonflyDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlDragonflyDoji") else output1[last] != 0
    }

    fun cdlDragonflyDoji(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlDragonflyDoji(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Engulfing Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlEngulfing(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEngulfing(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlEngulfing") else output1[last] != 0
    }

    fun cdlEngulfing(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlEngulfing(buffer.open, buffer.high, buffer.low, buffer.close, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEveningDojiStar(
            endIdx,
            endIdx,
            open,
            high,
            low,
            close,
            penetration,
            startOutput,
            endOutput,
            output1
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlEveningDojiStar") else output1[last] != 0
    }

    fun cdlEveningDojiStar(buffer: PriceBarBuffer, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlEveningDojiStar(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlEveningStar(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlEveningStar") else output1[last] != 0
    }

    fun cdlEveningStar(buffer: PriceBarBuffer, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlEveningStar(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

    /**
     * Apply Up/Down-gap side-by-side white lines on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlGapSideSideWhite(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGapSideSideWhite(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlGapSideSideWhite") else output1[last] != 0
    }

    fun cdlGapSideSideWhite(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlGapSideSideWhite(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Gravestone Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlGravestoneDoji(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGravestoneDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlGravestoneDoji") else output1[last] != 0
    }

    fun cdlGravestoneDoji(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlGravestoneDoji(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Hammer on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHammer(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHammer(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHammer") else output1[last] != 0
    }

    fun cdlHammer(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHammer(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Hanging Man on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHangingMan(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHangingMan(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHangingMan") else output1[last] != 0
    }

    fun cdlHangingMan(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHangingMan(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Harami Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHarami(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHarami(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHarami") else output1[last] != 0
    }

    fun cdlHarami(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHarami(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Harami Cross Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHaramiCross(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHaramiCross(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHaramiCross") else output1[last] != 0
    }

    fun cdlHaramiCross(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHaramiCross(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply High-Wave Candle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHignWave(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHignWave(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHignWave") else output1[last] != 0
    }

    fun cdlHignWave(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHignWave(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Hikkake Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHikkake(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkake(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHikkake") else output1[last] != 0
    }

    fun cdlHikkake(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHikkake(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Modified Hikkake Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHikkakeMod(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkakeMod(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHikkakeMod") else output1[last] != 0
    }

    fun cdlHikkakeMod(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHikkakeMod(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Homing Pigeon on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlHomingPigeon(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHomingPigeon(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlHomingPigeon") else output1[last] != 0
    }

    fun cdlHomingPigeon(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlHomingPigeon(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Identical Three Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlIdentical3Crows(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlIdentical3Crows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlIdentical3Crows") else output1[last] != 0
    }

    fun cdlIdentical3Crows(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlIdentical3Crows(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply In-Neck Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlInNeck(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInNeck(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlInNeck") else output1[last] != 0
    }

    fun cdlInNeck(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlInNeck(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Inverted Hammer on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlInvertedHammer(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInvertedHammer(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlInvertedHammer") else output1[last] != 0
    }

    fun cdlInvertedHammer(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlInvertedHammer(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Kicking on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlKicking(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKicking(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlKicking") else output1[last] != 0
    }

    fun cdlKicking(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlKicking(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Kicking - bull/bear determined by the longer marubozu on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlKickingByLength(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKickingByLength(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlKickingByLength") else output1[last] != 0
    }

    fun cdlKickingByLength(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlKickingByLength(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Ladder Bottom on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlLadderBottom(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLadderBottom(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlLadderBottom") else output1[last] != 0
    }

    fun cdlLadderBottom(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlLadderBottom(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Long Legged Doji on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlLongLeggedDoji(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLeggedDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlLongLeggedDoji") else output1[last] != 0
    }

    fun cdlLongLeggedDoji(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlLongLeggedDoji(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Long Line Candle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlLongLine(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLine(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlLongLine") else output1[last] != 0
    }

    fun cdlLongLine(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlLongLine(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Marubozu on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMarubozu(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMarubozu(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMarubozu") else output1[last] != 0
    }

    fun cdlMarubozu(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlMarubozu(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Matching Low on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlMatchingLow(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatchingLow(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMatchingLow") else output1[last] != 0
    }

    fun cdlMatchingLow(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlMatchingLow(buffer.open, buffer.high, buffer.low, buffer.close, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatHold(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMatHold") else output1[last] != 0
    }

    fun cdlMatHold(buffer: PriceBarBuffer, penetration: Double = 5.000000e-1, previous: Int = 0) =
        cdlMatHold(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMorningDojiStar(
            endIdx,
            endIdx,
            open,
            high,
            low,
            close,
            penetration,
            startOutput,
            endOutput,
            output1
        )
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMorningDojiStar") else output1[last] != 0
    }

    fun cdlMorningDojiStar(buffer: PriceBarBuffer, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlMorningDojiStar(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

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
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlMorningStar(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlMorningStar") else output1[last] != 0
    }

    fun cdlMorningStar(buffer: PriceBarBuffer, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlMorningStar(buffer.open, buffer.high, buffer.low, buffer.close, penetration, previous)

    /**
     * Apply On-Neck Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlOnNeck(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlOnNeck(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlOnNeck") else output1[last] != 0
    }

    fun cdlOnNeck(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlOnNeck(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Piercing Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlPiercing(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlPiercing(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlPiercing") else output1[last] != 0
    }

    fun cdlPiercing(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlPiercing(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Rickshaw Man on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlRickshawMan(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRickshawMan(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlRickshawMan") else output1[last] != 0
    }

    fun cdlRickshawMan(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlRickshawMan(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Rising/Falling Three Methods on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlRiseFall3Methods(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRiseFall3Methods(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlRiseFall3Methods") else output1[last] != 0
    }

    fun cdlRiseFall3Methods(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlRiseFall3Methods(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Separating Lines on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlSeperatingLines(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSeperatingLines(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlSeperatingLines") else output1[last] != 0
    }

    fun cdlSeperatingLines(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlSeperatingLines(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Shooting Star on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlShootingStar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShootingStar(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlShootingStar") else output1[last] != 0
    }

    fun cdlShootingStar(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlShootingStar(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Short Line Candle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlShortLine(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShortLine(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlShortLine") else output1[last] != 0
    }

    fun cdlShortLine(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlShortLine(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Spinning Top on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlSpinningTop(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSpinningTop(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlSpinningTop") else output1[last] != 0
    }

    fun cdlSpinningTop(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlSpinningTop(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Stalled Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlStalledPattern(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStalledPattern(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlStalledPattern") else output1[last] != 0
    }

    fun cdlStalledPattern(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlStalledPattern(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Stick Sandwich on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlStickSandwhich(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStickSandwhich(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlStickSandwhich") else output1[last] != 0
    }

    fun cdlStickSandwhich(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlStickSandwhich(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Takuri (Dragonfly Doji with very long lower shadow) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlTakuri(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTakuri(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlTakuri") else output1[last] != 0
    }

    fun cdlTakuri(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlTakuri(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Tasuki Gap on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlTasukiGap(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTasukiGap(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlTasukiGap") else output1[last] != 0
    }

    fun cdlTasukiGap(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlTasukiGap(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Thrusting Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlThrusting(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlThrusting(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlThrusting") else output1[last] != 0
    }

    fun cdlThrusting(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlThrusting(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Tristar Pattern on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlTristar(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTristar(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlTristar") else output1[last] != 0
    }

    fun cdlTristar(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlTristar(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Unique 3 River on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlUnique3River(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUnique3River(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlUnique3River") else output1[last] != 0
    }

    fun cdlUnique3River(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlUnique3River(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Upside Gap Two Crows on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlUpsideGap2Crows(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUpsideGap2Crows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlUpsideGap2Crows") else output1[last] != 0
    }

    fun cdlUpsideGap2Crows(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlUpsideGap2Crows(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Upside/Downside Gap Three Methods on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Pattern Recognition.
     */
    fun cdlXSideGap3Methods(
        open: DoubleArray,
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        previous: Int = 0
    ): Boolean {
        val endIdx = open.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlXSideGap3Methods(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cdlXSideGap3Methods") else output1[last] != 0
    }

    fun cdlXSideGap3Methods(buffer: PriceBarBuffer, previous: Int = 0) =
        cdlXSideGap3Methods(buffer.open, buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Vector Ceil on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun ceil(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ceil(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate ceil") else output1[last]
    }

    /**
     * Apply Chande Momentum Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun cmo(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cmo(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cmo") else output1[last]
    }

    /**
     * Apply Pearson's Correlation Coefficient (r) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun correl(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.correl(endIdx, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate correl") else output1[last]
    }

    /**
     * Apply Vector Trigonometric Cos on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun cos(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cos(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cos") else output1[last]
    }

    /**
     * Apply Vector Trigonometric Cosh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun cosh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cosh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate cosh") else output1[last]
    }

    /**
     * Apply Double Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun dema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate dema") else output1[last]
    }

    /**
     * Apply Vector Arithmetic Div on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun div(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.div(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate div") else output1[last]
    }

    /**
     * Apply Directional Movement Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun dx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dx(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate dx") else output1[last]
    }

    fun dx(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        dx(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun ema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate ema") else output1[last]
    }

    /**
     * Apply Vector Arithmetic Exp on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun exp(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.exp(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate exp") else output1[last]
    }

    /**
     * Apply Vector Floor on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun floor(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.floor(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate floor") else output1[last]
    }

    /**
     * Apply Hilbert Transform - Dominant Cycle Period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htDcPeriod(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPeriod(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate htDcPeriod") else output1[last]
    }

    /**
     * Apply Hilbert Transform - Dominant Cycle Phase on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htDcPhase(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPhase(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate htDcPhase") else output1[last]
    }

    /**
     * Apply Hilbert Transform - Phasor Components on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htPhasor(data: DoubleArray, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htPhasor(endIdx, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate htPhasor") else Pair(
            output1[last],
            output2[last]
        )
    }

    /**
     * Apply Hilbert Transform - SineWave on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htSine(data: DoubleArray, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htSine(endIdx, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate htSine") else Pair(
            output1[last],
            output2[last]
        )
    }

    /**
     * Apply Hilbert Transform - Instantaneous Trendline on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun htTrendline(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendline(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate htTrendline") else output1[last]
    }

    /**
     * Apply Hilbert Transform - Trend vs Cycle Mode on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htTrendMode(data: DoubleArray, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendMode(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate htTrendMode") else output1[last]
    }

    /**
     * Apply Kaufman Adaptive Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun kama(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.kama(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate kama") else output1[last]
    }

    /**
     * Apply Linear Regression on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearReg(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearReg(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearReg") else output1[last]
    }

    /**
     * Apply Linear Regression Angle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegAngle(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegAngle(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearRegAngle") else output1[last]
    }

    /**
     * Apply Linear Regression Intercept on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegIntercept(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegIntercept(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearRegIntercept") else output1[last]
    }

    /**
     * Apply Linear Regression Slope on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegSlope(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegSlope(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate linearRegSlope") else output1[last]
    }

    /**
     * Apply Vector Log Natural on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun ln(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ln(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate ln") else output1[last]
    }

    /**
     * Apply Vector Log10 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun log10(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.log10(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate log10") else output1[last]
    }

    /**
     * Apply Moving average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun movingAverage(data: DoubleArray, timePeriod: Int = 30, mAType: MAType = MAType.Ema, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverage(endIdx, endIdx, data, timePeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate movingAverage") else output1[last]
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
        previous: Int = 0
    ): Triple<Double, Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macd(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate macd") else Triple(
            output1[last],
            output2[last],
            output3[last]
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
        previous: Int = 0
    ): Triple<Double, Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdExt(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate macdExt") else Triple(
            output1[last],
            output2[last],
            output3[last]
        )
    }

    /**
     * Apply Moving Average Convergence/Divergence Fix 12/26 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun macdFix(data: DoubleArray, signalPeriod: Int = 9, previous: Int = 0): Triple<Double, Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val output3 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdFix(endIdx, endIdx, data, signalPeriod, startOutput, endOutput, output1, output2, output3)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate macdFix") else Triple(
            output1[last],
            output2[last],
            output3[last]
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
        previous: Int = 0
    ): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mama(endIdx, endIdx, data, fastLimit, slowLimit, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate mama") else Pair(
            output1[last],
            output2[last]
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
        previous: Int = 0
    ): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverageVariablePeriod(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate movingAverageVariablePeriod") else output1[last]
    }

    /**
     * Apply Highest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun max(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.max(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate max") else output1[last]
    }

    /**
     * Apply Index of highest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun maxIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.maxIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate maxIndex") else output1[last]
    }

    /**
     * Apply Median Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun medPrice(high: DoubleArray, low: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.medPrice(endIdx, endIdx, high, low, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate medPrice") else output1[last]
    }

    fun medPrice(buffer: PriceBarBuffer, previous: Int = 0) = medPrice(buffer.high, buffer.low, previous)

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
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mfi(endIdx, endIdx, high, low, close, volume, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate mfi") else output1[last]
    }

    fun mfi(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        mfi(buffer.high, buffer.low, buffer.close, buffer.volume, timePeriod, previous)

    /**
     * Apply MidPoint over period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun midPoint(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPoint(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate midPoint") else output1[last]
    }

    /**
     * Apply Midpoint Price over period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun midPrice(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPrice(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate midPrice") else output1[last]
    }

    fun midPrice(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        midPrice(buffer.high, buffer.low, timePeriod, previous)

    /**
     * Apply Lowest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun min(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.min(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate min") else output1[last]
    }

    /**
     * Apply Index of lowest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate minIndex") else output1[last]
    }

    /**
     * Apply Lowest and highest values over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minMax(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMax(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate minMax") else Pair(
            output1[last],
            output2[last]
        )
    }

    /**
     * Apply Indexes of lowest and highest values over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minMaxIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Pair<Int, Int> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = IntArray(outputSize)
        val output2 = IntArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMaxIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate minMaxIndex") else Pair(
            output1[last],
            output2[last]
        )
    }

    /**
     * Apply Minus Directional Indicator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun minusDI(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        timePeriod: Int = 14,
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDI(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate minusDI") else output1[last]
    }

    fun minusDI(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        minusDI(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Minus Directional Movement on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun minusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDM(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate minusDM") else output1[last]
    }

    fun minusDM(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        minusDM(buffer.high, buffer.low, timePeriod, previous)

    /**
     * Apply Momentum on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun mom(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mom(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate mom") else output1[last]
    }

    /**
     * Apply Vector Arithmetic Mult on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun mult(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mult(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate mult") else output1[last]
    }

    /**
     * Apply Normalized Average True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun natr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.natr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate natr") else output1[last]
    }

    fun natr(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        natr(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply On Balance Volume on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volume Indicators.
     */
    fun obv(data: DoubleArray, volume: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.obv(endIdx, endIdx, data, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate obv") else output1[last]
    }

    /**
     * Apply Plus Directional Indicator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun plusDI(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        timePeriod: Int = 14,
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDI(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate plusDI") else output1[last]
    }

    fun plusDI(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        plusDI(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Plus Directional Movement on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun plusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDM(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate plusDM") else output1[last]
    }

    fun plusDM(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        plusDM(buffer.high, buffer.low, timePeriod, previous)

    /**
     * Apply Percentage Price Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun ppo(
        data: DoubleArray,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ppo(endIdx, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate ppo") else output1[last]
    }

    /**
     * Apply Rate of change : ((price/prevPrice)-1)*100 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun roc(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.roc(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate roc") else output1[last]
    }

    /**
     * Apply Rate of change Percentage: (price-prevPrice)/prevPrice on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocP(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocP(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate rocP") else output1[last]
    }

    /**
     * Apply Rate of change ratio: (price/prevPrice) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocR(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate rocR") else output1[last]
    }

    /**
     * Apply Rate of change ratio 100 scale: (price/prevPrice)*100 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocR100(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR100(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate rocR100") else output1[last]
    }

    /**
     * Apply Relative Strength Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rsi(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rsi(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate rsi") else output1[last]
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
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sar(endIdx, endIdx, high, low, accelerationFactor, aFMaximum, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sar") else output1[last]
    }

    fun sar(
        buffer: PriceBarBuffer,
        accelerationFactor: Double = 2.000000e-2,
        aFMaximum: Double = 2.000000e-1,
        previous: Int = 0
    ) = sar(buffer.high, buffer.low, accelerationFactor, aFMaximum, previous)

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
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sarExt(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sarExt") else output1[last]
    }

    fun sarExt(
        buffer: PriceBarBuffer,
        startValue: Double = 0.000000e+0,
        offsetonReverse: Double = 0.000000e+0,
        aFInitLong: Double = 2.000000e-2,
        aFLong: Double = 2.000000e-2,
        aFMaxLong: Double = 2.000000e-1,
        aFInitShort: Double = 2.000000e-2,
        aFShort: Double = 2.000000e-2,
        aFMaxShort: Double = 2.000000e-1,
        previous: Int = 0
    ) = sarExt(
        buffer.high,
        buffer.low,
        startValue,
        offsetonReverse,
        aFInitLong,
        aFLong,
        aFMaxLong,
        aFInitShort,
        aFShort,
        aFMaxShort,
        previous
    )

    /**
     * Apply Vector Trigonometric Sin on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sin(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sin(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sin") else output1[last]
    }

    /**
     * Apply Vector Trigonometric Sinh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sinh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sinh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sinh") else output1[last]
    }

    /**
     * Apply Simple Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun sma(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sma(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sma") else output1[last]
    }

    /**
     * Apply Vector Square Root on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sqrt(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sqrt(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sqrt") else output1[last]
    }

    /**
     * Apply Standard Deviation on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun stdDev(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stdDev(endIdx, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate stdDev") else output1[last]
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
        previous: Int = 0
    ): Pair<Double, Double> {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stoch(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate stoch") else Pair(
            output1[last],
            output2[last]
        )
    }

    fun stoch(
        buffer: PriceBarBuffer,
        fastKPeriod: Int = 5,
        slowKPeriod: Int = 3,
        slowKMA: MAType = MAType.Ema,
        slowDPeriod: Int = 3,
        slowDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stoch(buffer.high, buffer.low, buffer.close, fastKPeriod, slowKPeriod, slowKMA, slowDPeriod, slowDMA, previous)

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
        previous: Int = 0
    ): Pair<Double, Double> {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stochF(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate stochF") else Pair(
            output1[last],
            output2[last]
        )
    }

    fun stochF(
        buffer: PriceBarBuffer,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stochF(buffer.high, buffer.low, buffer.close, fastKPeriod, fastDPeriod, fastDMA, previous)

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
        previous: Int = 0
    ): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val output2 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stochRsi(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate stochRsi") else Pair(
            output1[last],
            output2[last]
        )
    }

    /**
     * Apply Vector Arithmetic Subtraction on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun sub(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sub(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sub") else output1[last]
    }

    /**
     * Apply Summation on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun sum(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sum(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate sum") else output1[last]
    }

    /**
     * Apply Triple Exponential Moving Average (T3) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun t3(data: DoubleArray, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.t3(endIdx, endIdx, data, timePeriod, volumeFactor, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate t3") else output1[last]
    }

    /**
     * Apply Vector Trigonometric Tan on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun tan(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tan(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate tan") else output1[last]
    }

    /**
     * Apply Vector Trigonometric Tanh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun tanh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tanh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate tanh") else output1[last]
    }

    /**
     * Apply Triple Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun tema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate tema") else output1[last]
    }

    /**
     * Apply True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun trueRange(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trueRange(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate trueRange") else output1[last]
    }

    fun trueRange(buffer: PriceBarBuffer, previous: Int = 0) =
        trueRange(buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Triangular Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun trima(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trima(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate trima") else output1[last]
    }

    /**
     * Apply 1-day Rate-Of-Change (ROC) of a Triple Smooth EMA on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun trix(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trix(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate trix") else output1[last]
    }

    /**
     * Apply Time Series Forecast on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun tsf(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tsf(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate tsf") else output1[last]
    }

    /**
     * Apply Typical Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun typPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.typPrice(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate typPrice") else output1[last]
    }

    fun typPrice(buffer: PriceBarBuffer, previous: Int = 0) = typPrice(buffer.high, buffer.low, buffer.close, previous)

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
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ultOsc(
            endIdx,
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
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate ultOsc") else output1[last]
    }

    fun ultOsc(
        buffer: PriceBarBuffer,
        firstPeriod: Int = 7,
        secondPeriod: Int = 14,
        thirdPeriod: Int = 28,
        previous: Int = 0
    ) = ultOsc(buffer.high, buffer.low, buffer.close, firstPeriod, secondPeriod, thirdPeriod, previous)

    /**
     * Apply Variance on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun variance(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.variance(endIdx, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate variance") else output1[last]
    }

    /**
     * Apply Weighted Close Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun wclPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wclPrice(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate wclPrice") else output1[last]
    }

    fun wclPrice(buffer: PriceBarBuffer, previous: Int = 0) = wclPrice(buffer.high, buffer.low, buffer.close, previous)

    /**
     * Apply Williams' %R on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun willR(
        high: DoubleArray,
        low: DoubleArray,
        close: DoubleArray,
        timePeriod: Int = 14,
        previous: Int = 0
    ): Double {
        val endIdx = high.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.willR(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate willR") else output1[last]
    }

    fun willR(buffer: PriceBarBuffer, timePeriod: Int = 14, previous: Int = 0) =
        willR(buffer.high, buffer.low, buffer.close, timePeriod, previous)

    /**
     * Apply Weighted Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun wma(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val outputSize = 1
        val output1 = DoubleArray(outputSize)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wma(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        return if (last < 0) throw InsufficientData("Not enough data to calculate wma") else output1[last]
    }

}

