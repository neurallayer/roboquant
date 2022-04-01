@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package org.roboquant.ta

import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MAType
import com.tictactec.ta.lib.MInteger
import com.tictactec.ta.lib.RetCode
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.acos(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.acosLookback()
            throw InsufficientData("Not enough data to calculate acos, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Chaikin A/D Line on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volume Indicators.
     */
    fun ad(high: DoubleArray, low: DoubleArray, close: DoubleArray, volume: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ad(endIdx, endIdx, high, low, close, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adLookback()
            throw InsufficientData("Not enough data to calculate ad, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.add(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.addLookback()
            throw InsufficientData("Not enough data to calculate add, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.adOscLookback(fastPeriod, slowPeriod)
            throw InsufficientData("Not enough data to calculate adOsc, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adx(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adxLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate adx, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adxr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adxrLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate adxr, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.apo(endIdx, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.apoLookback(fastPeriod, slowPeriod, mAType)
            throw InsufficientData("Not enough data to calculate apo, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Aroon on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun aroon(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Pair<Double, Double> {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroon(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.aroonLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate aroon, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroonOsc(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.aroonOscLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate aroonOsc, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.asin(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.asinLookback()
            throw InsufficientData("Not enough data to calculate asin, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Trigonometric ATan on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun atan(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atan(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.atanLookback()
            throw InsufficientData("Not enough data to calculate atan, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Average True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun atr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.atrLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate atr, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.avgPrice(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.avgPriceLookback()
            throw InsufficientData("Not enough data to calculate avgPrice, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val output3 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.bbandsLookback(timePeriod, deviationsup, deviationsdown, mAType)
            throw InsufficientData("Not enough data to calculate bbands, required lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Apply Beta on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun beta(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 5, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.beta(endIdx, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.betaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate beta, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Balance Of Power on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun bop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = open.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bop(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.bopLookback()
            throw InsufficientData("Not enough data to calculate bop, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cci(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cciLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate cci, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl2Crows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl2CrowsLookback()
            throw InsufficientData("Not enough data to calculate cdl2Crows, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3BlackCrows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3BlackCrowsLookback()
            throw InsufficientData("Not enough data to calculate cdl3BlackCrows, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Inside(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3InsideLookback()
            throw InsufficientData("Not enough data to calculate cdl3Inside, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3LineStrike(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3LineStrikeLookback()
            throw InsufficientData("Not enough data to calculate cdl3LineStrike, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3Outside(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3OutsideLookback()
            throw InsufficientData("Not enough data to calculate cdl3Outside, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3StarsInSouth(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3StarsInSouthLookback()
            throw InsufficientData("Not enough data to calculate cdl3StarsInSouth, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdl3WhiteSoldiers(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3WhiteSoldiersLookback()
            throw InsufficientData("Not enough data to calculate cdl3WhiteSoldiers, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlAbandonedBaby(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlAbandonedBabyLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlAbandonedBaby, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlAdvanceBlock(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlAdvanceBlockLookback()
            throw InsufficientData("Not enough data to calculate cdlAdvanceBlock, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBeltHold(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlBeltHoldLookback()
            throw InsufficientData("Not enough data to calculate cdlBeltHold, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlBreakaway(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlBreakawayLookback()
            throw InsufficientData("Not enough data to calculate cdlBreakaway, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlClosingMarubozu(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlClosingMarubozuLookback()
            throw InsufficientData("Not enough data to calculate cdlClosingMarubozu, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlConcealBabysWall(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlConcealBabysWallLookback()
            throw InsufficientData("Not enough data to calculate cdlConcealBabysWall, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlCounterAttack(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlCounterAttackLookback()
            throw InsufficientData("Not enough data to calculate cdlCounterAttack, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlDarkCloudCover(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDarkCloudCoverLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlDarkCloudCover, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDojiLookback()
            throw InsufficientData("Not enough data to calculate cdlDoji, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDojiStar(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDojiStarLookback()
            throw InsufficientData("Not enough data to calculate cdlDojiStar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlDragonflyDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDragonflyDojiLookback()
            throw InsufficientData("Not enough data to calculate cdlDragonflyDoji, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlEngulfing(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlEngulfingLookback()
            throw InsufficientData("Not enough data to calculate cdlEngulfing, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
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
        if (last < 0) {
            val lookback = core.cdlEveningDojiStarLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlEveningDojiStar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlEveningStar(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlEveningStarLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlEveningStar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGapSideSideWhite(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlGapSideSideWhiteLookback()
            throw InsufficientData("Not enough data to calculate cdlGapSideSideWhite, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlGravestoneDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlGravestoneDojiLookback()
            throw InsufficientData("Not enough data to calculate cdlGravestoneDoji, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHammer(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHammerLookback()
            throw InsufficientData("Not enough data to calculate cdlHammer, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHangingMan(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHangingManLookback()
            throw InsufficientData("Not enough data to calculate cdlHangingMan, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHarami(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHaramiLookback()
            throw InsufficientData("Not enough data to calculate cdlHarami, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHaramiCross(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHaramiCrossLookback()
            throw InsufficientData("Not enough data to calculate cdlHaramiCross, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHignWave(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHignWaveLookback()
            throw InsufficientData("Not enough data to calculate cdlHignWave, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkake(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHikkakeLookback()
            throw InsufficientData("Not enough data to calculate cdlHikkake, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHikkakeMod(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHikkakeModLookback()
            throw InsufficientData("Not enough data to calculate cdlHikkakeMod, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlHomingPigeon(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHomingPigeonLookback()
            throw InsufficientData("Not enough data to calculate cdlHomingPigeon, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlIdentical3Crows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlIdentical3CrowsLookback()
            throw InsufficientData("Not enough data to calculate cdlIdentical3Crows, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInNeck(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlInNeckLookback()
            throw InsufficientData("Not enough data to calculate cdlInNeck, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlInvertedHammer(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlInvertedHammerLookback()
            throw InsufficientData("Not enough data to calculate cdlInvertedHammer, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKicking(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlKickingLookback()
            throw InsufficientData("Not enough data to calculate cdlKicking, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlKickingByLength(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlKickingByLengthLookback()
            throw InsufficientData("Not enough data to calculate cdlKickingByLength, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLadderBottom(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlLadderBottomLookback()
            throw InsufficientData("Not enough data to calculate cdlLadderBottom, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLeggedDoji(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlLongLeggedDojiLookback()
            throw InsufficientData("Not enough data to calculate cdlLongLeggedDoji, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlLongLine(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlLongLineLookback()
            throw InsufficientData("Not enough data to calculate cdlLongLine, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMarubozu(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMarubozuLookback()
            throw InsufficientData("Not enough data to calculate cdlMarubozu, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatchingLow(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMatchingLowLookback()
            throw InsufficientData("Not enough data to calculate cdlMatchingLow, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlMatHold(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMatHoldLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlMatHold, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
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
        if (last < 0) {
            val lookback = core.cdlMorningDojiStarLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlMorningDojiStar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret =
            core.cdlMorningStar(endIdx, endIdx, open, high, low, close, penetration, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMorningStarLookback(penetration)
            throw InsufficientData("Not enough data to calculate cdlMorningStar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlOnNeck(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlOnNeckLookback()
            throw InsufficientData("Not enough data to calculate cdlOnNeck, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlPiercing(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlPiercingLookback()
            throw InsufficientData("Not enough data to calculate cdlPiercing, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRickshawMan(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlRickshawManLookback()
            throw InsufficientData("Not enough data to calculate cdlRickshawMan, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlRiseFall3Methods(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlRiseFall3MethodsLookback()
            throw InsufficientData("Not enough data to calculate cdlRiseFall3Methods, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSeperatingLines(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlSeperatingLinesLookback()
            throw InsufficientData("Not enough data to calculate cdlSeperatingLines, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShootingStar(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlShootingStarLookback()
            throw InsufficientData("Not enough data to calculate cdlShootingStar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlShortLine(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlShortLineLookback()
            throw InsufficientData("Not enough data to calculate cdlShortLine, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlSpinningTop(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlSpinningTopLookback()
            throw InsufficientData("Not enough data to calculate cdlSpinningTop, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStalledPattern(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlStalledPatternLookback()
            throw InsufficientData("Not enough data to calculate cdlStalledPattern, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlStickSandwhich(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlStickSandwhichLookback()
            throw InsufficientData("Not enough data to calculate cdlStickSandwhich, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTakuri(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlTakuriLookback()
            throw InsufficientData("Not enough data to calculate cdlTakuri, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTasukiGap(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlTasukiGapLookback()
            throw InsufficientData("Not enough data to calculate cdlTasukiGap, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlThrusting(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlThrustingLookback()
            throw InsufficientData("Not enough data to calculate cdlThrusting, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlTristar(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlTristarLookback()
            throw InsufficientData("Not enough data to calculate cdlTristar, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUnique3River(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlUnique3RiverLookback()
            throw InsufficientData("Not enough data to calculate cdlUnique3River, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlUpsideGap2Crows(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlUpsideGap2CrowsLookback()
            throw InsufficientData("Not enough data to calculate cdlUpsideGap2Crows, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cdlXSideGap3Methods(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlXSideGap3MethodsLookback()
            throw InsufficientData("Not enough data to calculate cdlXSideGap3Methods, required lookback period is $lookback")
        }
        return output1[0] != 0
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ceil(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.ceilLookback()
            throw InsufficientData("Not enough data to calculate ceil, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Chande Momentum Oscillator on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun cmo(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cmo(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cmoLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate cmo, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Pearson's Correlation Coefficient (r) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun correl(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.correl(endIdx, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.correlLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate correl, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Trigonometric Cos on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun cos(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cos(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cosLookback()
            throw InsufficientData("Not enough data to calculate cos, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Trigonometric Cosh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun cosh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cosh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.coshLookback()
            throw InsufficientData("Not enough data to calculate cosh, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Double Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun dema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.demaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate dema, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Arithmetic Div on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun div(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.div(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.divLookback()
            throw InsufficientData("Not enough data to calculate div, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Directional Movement Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun dx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dx(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.dxLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate dx, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.emaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate ema, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Arithmetic Exp on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun exp(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.exp(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.expLookback()
            throw InsufficientData("Not enough data to calculate exp, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Floor on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun floor(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.floor(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.floorLookback()
            throw InsufficientData("Not enough data to calculate floor, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Hilbert Transform - Dominant Cycle Period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htDcPeriod(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPeriod(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htDcPeriodLookback()
            throw InsufficientData("Not enough data to calculate htDcPeriod, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Hilbert Transform - Dominant Cycle Phase on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htDcPhase(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPhase(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htDcPhaseLookback()
            throw InsufficientData("Not enough data to calculate htDcPhase, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Hilbert Transform - Phasor Components on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htPhasor(data: DoubleArray, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htPhasor(endIdx, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htPhasorLookback()
            throw InsufficientData("Not enough data to calculate htPhasor, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Apply Hilbert Transform - SineWave on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htSine(data: DoubleArray, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htSine(endIdx, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htSineLookback()
            throw InsufficientData("Not enough data to calculate htSine, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Apply Hilbert Transform - Instantaneous Trendline on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun htTrendline(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendline(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htTrendlineLookback()
            throw InsufficientData("Not enough data to calculate htTrendline, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Hilbert Transform - Trend vs Cycle Mode on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Cycle Indicators.
     */
    fun htTrendMode(data: DoubleArray, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendMode(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htTrendModeLookback()
            throw InsufficientData("Not enough data to calculate htTrendMode, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Kaufman Adaptive Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun kama(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.kama(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.kamaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate kama, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Linear Regression on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearReg(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearReg(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate linearReg, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Linear Regression Angle on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegAngle(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegAngle(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegAngleLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate linearRegAngle, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Linear Regression Intercept on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegIntercept(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegIntercept(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegInterceptLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate linearRegIntercept, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Linear Regression Slope on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun linearRegSlope(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegSlope(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegSlopeLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate linearRegSlope, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Log Natural on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun ln(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ln(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.lnLookback()
            throw InsufficientData("Not enough data to calculate ln, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Log10 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun log10(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.log10(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.log10Lookback()
            throw InsufficientData("Not enough data to calculate log10, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Moving average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun movingAverage(data: DoubleArray, timePeriod: Int = 30, mAType: MAType = MAType.Ema, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverage(endIdx, endIdx, data, timePeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.movingAverageLookback(timePeriod, mAType)
            throw InsufficientData("Not enough data to calculate movingAverage, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val output3 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.macdLookback(fastPeriod, slowPeriod, signalPeriod)
            throw InsufficientData("Not enough data to calculate macd, required lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val output3 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.macdExtLookback(fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA)
            throw InsufficientData("Not enough data to calculate macdExt, required lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Apply Moving Average Convergence/Divergence Fix 12/26 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun macdFix(data: DoubleArray, signalPeriod: Int = 9, previous: Int = 0): Triple<Double, Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val output3 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdFix(endIdx, endIdx, data, signalPeriod, startOutput, endOutput, output1, output2, output3)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.macdFixLookback(signalPeriod)
            throw InsufficientData("Not enough data to calculate macdFix, required lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mama(endIdx, endIdx, data, fastLimit, slowLimit, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.mamaLookback(fastLimit, slowLimit)
            throw InsufficientData("Not enough data to calculate mama, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
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
        val output1 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.movingAverageVariablePeriodLookback(minimumPeriod, maximumPeriod, mAType)
            throw InsufficientData("Not enough data to calculate movingAverageVariablePeriod, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Highest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun max(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.max(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.maxLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate max, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Index of highest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun maxIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.maxIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.maxIndexLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate maxIndex, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Median Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun medPrice(high: DoubleArray, low: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.medPrice(endIdx, endIdx, high, low, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.medPriceLookback()
            throw InsufficientData("Not enough data to calculate medPrice, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mfi(endIdx, endIdx, high, low, close, volume, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.mfiLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate mfi, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPoint(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.midPointLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate midPoint, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Midpoint Price over period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun midPrice(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPrice(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.midPriceLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate midPrice, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.min(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate min, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Index of lowest value over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minIndexLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate minIndex, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Lowest and highest values over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minMax(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMax(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minMaxLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate minMax, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Apply Indexes of lowest and highest values over a specified period on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun minMaxIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Pair<Int, Int> {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val output2 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMaxIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minMaxIndexLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate minMaxIndex, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDI(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minusDILookback(timePeriod)
            throw InsufficientData("Not enough data to calculate minusDI, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDM(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minusDMLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate minusDM, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mom(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.momLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate mom, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Arithmetic Mult on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun mult(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mult(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.multLookback()
            throw InsufficientData("Not enough data to calculate mult, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Normalized Average True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun natr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.natr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.natrLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate natr, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.obv(endIdx, endIdx, data, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.obvLookback()
            throw InsufficientData("Not enough data to calculate obv, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDI(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.plusDILookback(timePeriod)
            throw InsufficientData("Not enough data to calculate plusDI, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDM(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.plusDMLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate plusDM, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ppo(endIdx, endIdx, data, fastPeriod, slowPeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.ppoLookback(fastPeriod, slowPeriod, mAType)
            throw InsufficientData("Not enough data to calculate ppo, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Rate of change : ((price/prevPrice)-1)*100 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun roc(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.roc(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate roc, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Rate of change Percentage: (price-prevPrice)/prevPrice on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocP(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocP(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocPLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate rocP, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Rate of change ratio: (price/prevPrice) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocR(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocRLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate rocR, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Rate of change ratio 100 scale: (price/prevPrice)*100 on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rocR100(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR100(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocR100Lookback(timePeriod)
            throw InsufficientData("Not enough data to calculate rocR100, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Relative Strength Index on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun rsi(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rsi(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rsiLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate rsi, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sar(endIdx, endIdx, high, low, accelerationFactor, aFMaximum, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sarLookback(accelerationFactor, aFMaximum)
            throw InsufficientData("Not enough data to calculate sar, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.sarExtLookback(
                startValue,
                offsetonReverse,
                aFInitLong,
                aFLong,
                aFMaxLong,
                aFInitShort,
                aFShort,
                aFMaxShort,
            )
            throw InsufficientData("Not enough data to calculate sarExt, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sin(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sinLookback()
            throw InsufficientData("Not enough data to calculate sin, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Trigonometric Sinh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sinh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sinh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sinhLookback()
            throw InsufficientData("Not enough data to calculate sinh, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Simple Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun sma(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sma(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.smaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate sma, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Square Root on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun sqrt(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sqrt(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sqrtLookback()
            throw InsufficientData("Not enough data to calculate sqrt, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Standard Deviation on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun stdDev(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stdDev(endIdx, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.stdDevLookback(timePeriod, deviations)
            throw InsufficientData("Not enough data to calculate stdDev, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.stochLookback(fastKPeriod, slowKPeriod, slowKMA, slowDPeriod, slowDMA)
            throw InsufficientData("Not enough data to calculate stoch, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.stochFLookback(fastKPeriod, fastDPeriod, fastDMA)
            throw InsufficientData("Not enough data to calculate stochF, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
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
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.stochRsiLookback(timePeriod, fastKPeriod, fastDPeriod, fastDMA)
            throw InsufficientData("Not enough data to calculate stochRsi, required lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Apply Vector Arithmetic Subtraction on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun sub(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sub(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.subLookback()
            throw InsufficientData("Not enough data to calculate sub, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Summation on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Operators.
     */
    fun sum(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sum(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sumLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate sum, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Triple Exponential Moving Average (T3) on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun t3(data: DoubleArray, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.t3(endIdx, endIdx, data, timePeriod, volumeFactor, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.t3Lookback(timePeriod, volumeFactor)
            throw InsufficientData("Not enough data to calculate t3, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Trigonometric Tan on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun tan(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tan(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.tanLookback()
            throw InsufficientData("Not enough data to calculate tan, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Vector Trigonometric Tanh on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Math Transform.
     */
    fun tanh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tanh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.tanhLookback()
            throw InsufficientData("Not enough data to calculate tanh, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Triple Exponential Moving Average on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Overlap Studies.
     */
    fun tema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.temaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate tema, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply True Range on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Volatility Indicators.
     */
    fun trueRange(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trueRange(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.trueRangeLookback()
            throw InsufficientData("Not enough data to calculate trueRange, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trima(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.trimaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate trima, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply 1-day Rate-Of-Change (ROC) of a Triple Smooth EMA on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Momentum Indicators.
     */
    fun trix(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trix(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.trixLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate trix, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Time Series Forecast on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Statistic Functions.
     */
    fun tsf(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tsf(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.tsfLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate tsf, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Typical Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun typPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.typPrice(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.typPriceLookback()
            throw InsufficientData("Not enough data to calculate typPrice, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
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
        if (last < 0) {
            val lookback = core.ultOscLookback(firstPeriod, secondPeriod, thirdPeriod)
            throw InsufficientData("Not enough data to calculate ultOsc, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.variance(endIdx, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.varianceLookback(timePeriod, deviations)
            throw InsufficientData("Not enough data to calculate variance, required lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Apply Weighted Close Price on the provided input data and return the most recent output only. If there is insufficient
     * data to calculate the indicators, an [InsufficientData] will be thrown.
     * This indicator belongs to the group Price Transform.
     */
    fun wclPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wclPrice(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.wclPriceLookback()
            throw InsufficientData("Not enough data to calculate wclPrice, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.willR(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.willRLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate willR, required lookback period is $lookback")
        }
        return output1[0]
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
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wma(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw Exception(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.wmaLookback(timePeriod)
            throw InsufficientData("Not enough data to calculate wma, required lookback period is $lookback")
        }
        return output1[0]
    }

}

