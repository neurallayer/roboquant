@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "unused",
    "LargeClass",
    "TooManyFunctions",
    "WildcardImport",
    "MaxLineLength",
    "LongParameterList",
    "SpellCheckingInspection"
)

package org.roboquant.ta

import com.tictactec.ta.lib.*
import org.roboquant.common.DoesNotComputeException
import org.roboquant.strategies.utils.PriceBarSeries

/**
 * This class wraps the excellent TALib library and makes it easy to use any of indicators provided by that library.
 * This wrapper is optimized for event based updates.
 */
class TaLib(var core: Core = Core()) {

    /**
     * Calculate **Vector Trigonometric ACos** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun acos(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.acos(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.acosLookback() + previous
            throw InsufficientData("Not enough data to calculate acos, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [acos]
     */
    fun acos(series: PriceBarSeries, previous: Int = 0) = acos(series.close, previous)

    /**
     * Calculate **Chaikin A/D Line** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Volume Indicators**.
     */
    fun ad(high: DoubleArray, low: DoubleArray, close: DoubleArray, volume: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ad(endIdx, endIdx, high, low, close, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adLookback() + previous
            throw InsufficientData("Not enough data to calculate ad, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [ad]
     */
    fun ad(series: PriceBarSeries, previous: Int = 0) =
        ad(series.high, series.low, series.close, series.volume, previous)

    /**
     * Calculate **Vector Arithmetic Add** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun add(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.add(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.addLookback() + previous
            throw InsufficientData("Not enough data to calculate add, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Chaikin A/D Oscillator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Volume Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adOscLookback(fastPeriod, slowPeriod) + previous
            throw InsufficientData("Not enough data to calculate adOsc, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [adOsc]
     */
    fun adOsc(series: PriceBarSeries, fastPeriod: Int = 3, slowPeriod: Int = 10, previous: Int = 0) =
        adOsc(series.high, series.low, series.close, series.volume, fastPeriod, slowPeriod, previous)

    /**
     * Calculate **Average Directional Movement Index** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun adx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adx(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adxLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate adx, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [adx]
     */
    fun adx(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        adx(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Average Directional Movement Index Rating** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun adxr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.adxr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.adxrLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate adxr, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [adxr]
     */
    fun adxr(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        adxr(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Absolute Price Oscillator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.apoLookback(fastPeriod, slowPeriod, mAType) + previous
            throw InsufficientData("Not enough data to calculate apo, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [apo]
     */
    fun apo(
        series: PriceBarSeries,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ) = apo(series.close, fastPeriod, slowPeriod, mAType, previous)

    /**
     * Calculate **Aroon** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun aroon(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Pair<Double, Double> {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroon(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.aroonLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate aroon, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [aroon]
     */
    fun aroon(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        aroon(series.high, series.low, timePeriod, previous)

    /**
     * Calculate **Aroon Oscillator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun aroonOsc(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.aroonOsc(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.aroonOscLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate aroonOsc, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [aroonOsc]
     */
    fun aroonOsc(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        aroonOsc(series.high, series.low, timePeriod, previous)

    /**
     * Calculate **Vector Trigonometric ASin** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun asin(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.asin(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.asinLookback() + previous
            throw InsufficientData("Not enough data to calculate asin, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [asin]
     */
    fun asin(series: PriceBarSeries, previous: Int = 0) = asin(series.close, previous)

    /**
     * Calculate **Vector Trigonometric ATan** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun atan(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atan(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.atanLookback() + previous
            throw InsufficientData("Not enough data to calculate atan, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [atan]
     */
    fun atan(series: PriceBarSeries, previous: Int = 0) = atan(series.close, previous)

    /**
     * Calculate **Average True Range** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Volatility Indicators**.
     */
    fun atr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.atr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.atrLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate atr, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [atr]
     */
    fun atr(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        atr(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Average Price** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Price Transform**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.avgPriceLookback() + previous
            throw InsufficientData("Not enough data to calculate avgPrice, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [avgPrice]
     */
    fun avgPrice(series: PriceBarSeries, previous: Int = 0) =
        avgPrice(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Bollinger Bands** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.bbandsLookback(timePeriod, deviationsup, deviationsdown, mAType) + previous
            throw InsufficientData("Not enough data to calculate bbands, minimal lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [bbands]
     */
    fun bbands(
        series: PriceBarSeries,
        timePeriod: Int = 5,
        deviationsup: Double = 2.000000e+0,
        deviationsdown: Double = 2.000000e+0,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ) = bbands(series.close, timePeriod, deviationsup, deviationsdown, mAType, previous)

    /**
     * Calculate **Beta** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun beta(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 5, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.beta(endIdx, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.betaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate beta, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Balance Of Power** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun bop(open: DoubleArray, high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = open.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.bop(endIdx, endIdx, open, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.bopLookback() + previous
            throw InsufficientData("Not enough data to calculate bop, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [bop]
     */
    fun bop(series: PriceBarSeries, previous: Int = 0) =
        bop(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Commodity Channel Index** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun cci(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cci(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cciLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate cci, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cci]
     */
    fun cci(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        cci(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Two Crows** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl2CrowsLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl2Crows, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl2Crows]
     */
    fun cdl2Crows(series: PriceBarSeries, previous: Int = 0) =
        cdl2Crows(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Three Black Crows** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3BlackCrowsLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl3BlackCrows, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl3BlackCrows]
     */
    fun cdl3BlackCrows(series: PriceBarSeries, previous: Int = 0) =
        cdl3BlackCrows(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Three Inside Up/Down** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3InsideLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl3Inside, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl3Inside]
     */
    fun cdl3Inside(series: PriceBarSeries, previous: Int = 0) =
        cdl3Inside(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Three-Line Strike** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3LineStrikeLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl3LineStrike, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl3LineStrike]
     */
    fun cdl3LineStrike(series: PriceBarSeries, previous: Int = 0) =
        cdl3LineStrike(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Three Outside Up/Down** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3OutsideLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl3Outside, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl3Outside]
     */
    fun cdl3Outside(series: PriceBarSeries, previous: Int = 0) =
        cdl3Outside(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Three Stars In The South** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3StarsInSouthLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl3StarsInSouth, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl3StarsInSouth]
     */
    fun cdl3StarsInSouth(series: PriceBarSeries, previous: Int = 0) =
        cdl3StarsInSouth(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Three Advancing White Soldiers** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdl3WhiteSoldiersLookback() + previous
            throw InsufficientData("Not enough data to calculate cdl3WhiteSoldiers, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdl3WhiteSoldiers]
     */
    fun cdl3WhiteSoldiers(series: PriceBarSeries, previous: Int = 0) =
        cdl3WhiteSoldiers(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Abandoned Baby** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlAbandonedBabyLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlAbandonedBaby, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlAbandonedBaby]
     */
    fun cdlAbandonedBaby(series: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlAbandonedBaby(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **Advance Block** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlAdvanceBlockLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlAdvanceBlock, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlAdvanceBlock]
     */
    fun cdlAdvanceBlock(series: PriceBarSeries, previous: Int = 0) =
        cdlAdvanceBlock(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Belt-hold** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlBeltHoldLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlBeltHold, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlBeltHold]
     */
    fun cdlBeltHold(series: PriceBarSeries, previous: Int = 0) =
        cdlBeltHold(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Breakaway** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlBreakawayLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlBreakaway, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlBreakaway]
     */
    fun cdlBreakaway(series: PriceBarSeries, previous: Int = 0) =
        cdlBreakaway(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Closing Marubozu** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlClosingMarubozuLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlClosingMarubozu, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlClosingMarubozu]
     */
    fun cdlClosingMarubozu(series: PriceBarSeries, previous: Int = 0) =
        cdlClosingMarubozu(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Concealing Baby Swallow** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlConcealBabysWallLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlConcealBabysWall, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlConcealBabysWall]
     */
    fun cdlConcealBabysWall(series: PriceBarSeries, previous: Int = 0) =
        cdlConcealBabysWall(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Counterattack** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlCounterAttackLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlCounterAttack, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlCounterAttack]
     */
    fun cdlCounterAttack(series: PriceBarSeries, previous: Int = 0) =
        cdlCounterAttack(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Dark Cloud Cover** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDarkCloudCoverLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlDarkCloudCover, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlDarkCloudCover]
     */
    fun cdlDarkCloudCover(series: PriceBarSeries, penetration: Double = 5.000000e-1, previous: Int = 0) =
        cdlDarkCloudCover(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **Doji** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDojiLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlDoji, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlDoji]
     */
    fun cdlDoji(series: PriceBarSeries, previous: Int = 0) =
        cdlDoji(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Doji Star** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDojiStarLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlDojiStar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlDojiStar]
     */
    fun cdlDojiStar(series: PriceBarSeries, previous: Int = 0) =
        cdlDojiStar(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Dragonfly Doji** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlDragonflyDojiLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlDragonflyDoji, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlDragonflyDoji]
     */
    fun cdlDragonflyDoji(series: PriceBarSeries, previous: Int = 0) =
        cdlDragonflyDoji(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Engulfing Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlEngulfingLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlEngulfing, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlEngulfing]
     */
    fun cdlEngulfing(series: PriceBarSeries, previous: Int = 0) =
        cdlEngulfing(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Evening Doji Star** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlEveningDojiStarLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlEveningDojiStar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlEveningDojiStar]
     */
    fun cdlEveningDojiStar(series: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlEveningDojiStar(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **Evening Star** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlEveningStarLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlEveningStar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlEveningStar]
     */
    fun cdlEveningStar(series: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlEveningStar(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **Up/Down-gap side-by-side white lines** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlGapSideSideWhiteLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlGapSideSideWhite, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlGapSideSideWhite]
     */
    fun cdlGapSideSideWhite(series: PriceBarSeries, previous: Int = 0) =
        cdlGapSideSideWhite(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Gravestone Doji** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlGravestoneDojiLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlGravestoneDoji, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlGravestoneDoji]
     */
    fun cdlGravestoneDoji(series: PriceBarSeries, previous: Int = 0) =
        cdlGravestoneDoji(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Hammer** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHammerLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHammer, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHammer]
     */
    fun cdlHammer(series: PriceBarSeries, previous: Int = 0) =
        cdlHammer(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Hanging Man** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHangingManLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHangingMan, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHangingMan]
     */
    fun cdlHangingMan(series: PriceBarSeries, previous: Int = 0) =
        cdlHangingMan(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Harami Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHaramiLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHarami, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHarami]
     */
    fun cdlHarami(series: PriceBarSeries, previous: Int = 0) =
        cdlHarami(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Harami Cross Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHaramiCrossLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHaramiCross, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHaramiCross]
     */
    fun cdlHaramiCross(series: PriceBarSeries, previous: Int = 0) =
        cdlHaramiCross(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **High-Wave Candle** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHignWaveLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHignWave, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHignWave]
     */
    fun cdlHignWave(series: PriceBarSeries, previous: Int = 0) =
        cdlHignWave(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Hikkake Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHikkakeLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHikkake, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHikkake]
     */
    fun cdlHikkake(series: PriceBarSeries, previous: Int = 0) =
        cdlHikkake(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Modified Hikkake Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHikkakeModLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHikkakeMod, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHikkakeMod]
     */
    fun cdlHikkakeMod(series: PriceBarSeries, previous: Int = 0) =
        cdlHikkakeMod(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Homing Pigeon** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlHomingPigeonLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlHomingPigeon, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlHomingPigeon]
     */
    fun cdlHomingPigeon(series: PriceBarSeries, previous: Int = 0) =
        cdlHomingPigeon(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Identical Three Crows** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlIdentical3CrowsLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlIdentical3Crows, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlIdentical3Crows]
     */
    fun cdlIdentical3Crows(series: PriceBarSeries, previous: Int = 0) =
        cdlIdentical3Crows(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **In-Neck Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlInNeckLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlInNeck, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlInNeck]
     */
    fun cdlInNeck(series: PriceBarSeries, previous: Int = 0) =
        cdlInNeck(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Inverted Hammer** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlInvertedHammerLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlInvertedHammer, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlInvertedHammer]
     */
    fun cdlInvertedHammer(series: PriceBarSeries, previous: Int = 0) =
        cdlInvertedHammer(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Kicking** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlKickingLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlKicking, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlKicking]
     */
    fun cdlKicking(series: PriceBarSeries, previous: Int = 0) =
        cdlKicking(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Kicking - bull/bear determined by the longer marubozu** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlKickingByLengthLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlKickingByLength, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlKickingByLength]
     */
    fun cdlKickingByLength(series: PriceBarSeries, previous: Int = 0) =
        cdlKickingByLength(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Ladder Bottom** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlLadderBottomLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlLadderBottom, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlLadderBottom]
     */
    fun cdlLadderBottom(series: PriceBarSeries, previous: Int = 0) =
        cdlLadderBottom(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Long Legged Doji** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlLongLeggedDojiLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlLongLeggedDoji, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlLongLeggedDoji]
     */
    fun cdlLongLeggedDoji(series: PriceBarSeries, previous: Int = 0) =
        cdlLongLeggedDoji(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Long Line Candle** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlLongLineLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlLongLine, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlLongLine]
     */
    fun cdlLongLine(series: PriceBarSeries, previous: Int = 0) =
        cdlLongLine(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Marubozu** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMarubozuLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlMarubozu, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlMarubozu]
     */
    fun cdlMarubozu(series: PriceBarSeries, previous: Int = 0) =
        cdlMarubozu(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Matching Low** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMatchingLowLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlMatchingLow, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlMatchingLow]
     */
    fun cdlMatchingLow(series: PriceBarSeries, previous: Int = 0) =
        cdlMatchingLow(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Mat Hold** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMatHoldLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlMatHold, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlMatHold]
     */
    fun cdlMatHold(series: PriceBarSeries, penetration: Double = 5.000000e-1, previous: Int = 0) =
        cdlMatHold(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **Morning Doji Star** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMorningDojiStarLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlMorningDojiStar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlMorningDojiStar]
     */
    fun cdlMorningDojiStar(series: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlMorningDojiStar(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **Morning Star** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlMorningStarLookback(penetration) + previous
            throw InsufficientData("Not enough data to calculate cdlMorningStar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlMorningStar]
     */
    fun cdlMorningStar(series: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlMorningStar(series.open, series.high, series.low, series.close, penetration, previous)

    /**
     * Calculate **On-Neck Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlOnNeckLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlOnNeck, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlOnNeck]
     */
    fun cdlOnNeck(series: PriceBarSeries, previous: Int = 0) =
        cdlOnNeck(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Piercing Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlPiercingLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlPiercing, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlPiercing]
     */
    fun cdlPiercing(series: PriceBarSeries, previous: Int = 0) =
        cdlPiercing(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Rickshaw Man** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlRickshawManLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlRickshawMan, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlRickshawMan]
     */
    fun cdlRickshawMan(series: PriceBarSeries, previous: Int = 0) =
        cdlRickshawMan(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Rising/Falling Three Methods** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlRiseFall3MethodsLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlRiseFall3Methods, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlRiseFall3Methods]
     */
    fun cdlRiseFall3Methods(series: PriceBarSeries, previous: Int = 0) =
        cdlRiseFall3Methods(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Separating Lines** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlSeperatingLinesLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlSeperatingLines, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlSeperatingLines]
     */
    fun cdlSeperatingLines(series: PriceBarSeries, previous: Int = 0) =
        cdlSeperatingLines(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Shooting Star** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlShootingStarLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlShootingStar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlShootingStar]
     */
    fun cdlShootingStar(series: PriceBarSeries, previous: Int = 0) =
        cdlShootingStar(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Short Line Candle** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlShortLineLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlShortLine, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlShortLine]
     */
    fun cdlShortLine(series: PriceBarSeries, previous: Int = 0) =
        cdlShortLine(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Spinning Top** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlSpinningTopLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlSpinningTop, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlSpinningTop]
     */
    fun cdlSpinningTop(series: PriceBarSeries, previous: Int = 0) =
        cdlSpinningTop(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Stalled Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlStalledPatternLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlStalledPattern, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlStalledPattern]
     */
    fun cdlStalledPattern(series: PriceBarSeries, previous: Int = 0) =
        cdlStalledPattern(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Stick Sandwich** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
     */
    fun cdlStickSandwich(
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlStickSandwhichLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlStickSandwich, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlStickSandwich]
     */
    fun cdlStickSandwich(series: PriceBarSeries, previous: Int = 0) =
        cdlStickSandwich(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Takuri (Dragonfly Doji with very long lower shadow)** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlTakuriLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlTakuri, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlTakuri]
     */
    fun cdlTakuri(series: PriceBarSeries, previous: Int = 0) =
        cdlTakuri(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Tasuki Gap** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlTasukiGapLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlTasukiGap, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlTasukiGap]
     */
    fun cdlTasukiGap(series: PriceBarSeries, previous: Int = 0) =
        cdlTasukiGap(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Thrusting Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlThrustingLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlThrusting, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlThrusting]
     */
    fun cdlThrusting(series: PriceBarSeries, previous: Int = 0) =
        cdlThrusting(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Tristar Pattern** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlTristarLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlTristar, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlTristar]
     */
    fun cdlTristar(series: PriceBarSeries, previous: Int = 0) =
        cdlTristar(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Unique 3 River** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlUnique3RiverLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlUnique3River, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlUnique3River]
     */
    fun cdlUnique3River(series: PriceBarSeries, previous: Int = 0) =
        cdlUnique3River(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Upside Gap Two Crows** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlUpsideGap2CrowsLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlUpsideGap2Crows, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlUpsideGap2Crows]
     */
    fun cdlUpsideGap2Crows(series: PriceBarSeries, previous: Int = 0) =
        cdlUpsideGap2Crows(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Upside/Downside Gap Three Methods** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Pattern Recognition**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cdlXSideGap3MethodsLookback() + previous
            throw InsufficientData("Not enough data to calculate cdlXSideGap3Methods, minimal lookback period is $lookback")
        }
        return output1[0] != 0
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cdlXSideGap3Methods]
     */
    fun cdlXSideGap3Methods(series: PriceBarSeries, previous: Int = 0) =
        cdlXSideGap3Methods(series.open, series.high, series.low, series.close, previous)

    /**
     * Calculate **Vector Ceil** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun ceil(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ceil(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.ceilLookback() + previous
            throw InsufficientData("Not enough data to calculate ceil, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [ceil]
     */
    fun ceil(series: PriceBarSeries, previous: Int = 0) = ceil(series.close, previous)

    /**
     * Calculate **Chande Momentum Oscillator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun cmo(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cmo(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cmoLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate cmo, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cmo]
     */
    fun cmo(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) = cmo(series.close, timePeriod, previous)

    /**
     * Calculate **Pearson's Correlation Coefficient (r)** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun correl(data0: DoubleArray, data1: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.correl(endIdx, endIdx, data0, data1, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.correlLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate correl, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Vector Trigonometric Cos** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun cos(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cos(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.cosLookback() + previous
            throw InsufficientData("Not enough data to calculate cos, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cos]
     */
    fun cos(series: PriceBarSeries, previous: Int = 0) = cos(series.close, previous)

    /**
     * Calculate **Vector Trigonometric Cosh** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun cosh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.cosh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.coshLookback() + previous
            throw InsufficientData("Not enough data to calculate cosh, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [cosh]
     */
    fun cosh(series: PriceBarSeries, previous: Int = 0) = cosh(series.close, previous)

    /**
     * Calculate **Double Exponential Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun dema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.demaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate dema, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [dema]
     */
    fun dema(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = dema(series.close, timePeriod, previous)

    /**
     * Calculate **Vector Arithmetic Div** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun div(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.div(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.divLookback() + previous
            throw InsufficientData("Not enough data to calculate div, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Directional Movement Index** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun dx(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.dx(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.dxLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate dx, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [dx]
     */
    fun dx(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        dx(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Exponential Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun ema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.emaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate ema, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [ema]
     */
    fun ema(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = ema(series.close, timePeriod, previous)

    /**
     * Calculate **Vector Arithmetic Exp** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun exp(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.exp(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.expLookback() + previous
            throw InsufficientData("Not enough data to calculate exp, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [exp]
     */
    fun exp(series: PriceBarSeries, previous: Int = 0) = exp(series.close, previous)

    /**
     * Calculate **Vector Floor** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun floor(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.floor(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.floorLookback() + previous
            throw InsufficientData("Not enough data to calculate floor, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [floor]
     */
    fun floor(series: PriceBarSeries, previous: Int = 0) = floor(series.close, previous)

    /**
     * Calculate **Hilbert Transform - Dominant Cycle Period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Cycle Indicators**.
     */
    fun htDcPeriod(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPeriod(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htDcPeriodLookback() + previous
            throw InsufficientData("Not enough data to calculate htDcPeriod, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [htDcPeriod]
     */
    fun htDcPeriod(series: PriceBarSeries, previous: Int = 0) = htDcPeriod(series.close, previous)

    /**
     * Calculate **Hilbert Transform - Dominant Cycle Phase** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Cycle Indicators**.
     */
    fun htDcPhase(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htDcPhase(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htDcPhaseLookback() + previous
            throw InsufficientData("Not enough data to calculate htDcPhase, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [htDcPhase]
     */
    fun htDcPhase(series: PriceBarSeries, previous: Int = 0) = htDcPhase(series.close, previous)

    /**
     * Calculate **Hilbert Transform - Phasor Components** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Cycle Indicators**.
     */
    fun htPhasor(data: DoubleArray, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htPhasor(endIdx, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htPhasorLookback() + previous
            throw InsufficientData("Not enough data to calculate htPhasor, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [htPhasor]
     */
    fun htPhasor(series: PriceBarSeries, previous: Int = 0) = htPhasor(series.close, previous)

    /**
     * Calculate **Hilbert Transform - SineWave** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Cycle Indicators**.
     */
    fun htSine(data: DoubleArray, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htSine(endIdx, endIdx, data, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htSineLookback() + previous
            throw InsufficientData("Not enough data to calculate htSine, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [htSine]
     */
    fun htSine(series: PriceBarSeries, previous: Int = 0) = htSine(series.close, previous)

    /**
     * Calculate **Hilbert Transform - Instantaneous Trendline** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun htTrendline(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendline(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htTrendlineLookback() + previous
            throw InsufficientData("Not enough data to calculate htTrendline, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [htTrendline]
     */
    fun htTrendline(series: PriceBarSeries, previous: Int = 0) = htTrendline(series.close, previous)

    /**
     * Calculate **Hilbert Transform - Trend vs Cycle Mode** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Cycle Indicators**.
     */
    fun htTrendMode(data: DoubleArray, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.htTrendMode(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.htTrendModeLookback() + previous
            throw InsufficientData("Not enough data to calculate htTrendMode, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [htTrendMode]
     */
    fun htTrendMode(series: PriceBarSeries, previous: Int = 0) = htTrendMode(series.close, previous)

    /**
     * Calculate **Kaufman Adaptive Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun kama(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.kama(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.kamaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate kama, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [kama]
     */
    fun kama(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = kama(series.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun linearReg(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearReg(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate linearReg, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [linearReg]
     */
    fun linearReg(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearReg(series.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression Angle** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun linearRegAngle(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegAngle(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegAngleLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate linearRegAngle, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [linearRegAngle]
     */
    fun linearRegAngle(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearRegAngle(series.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression Intercept** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun linearRegIntercept(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegIntercept(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegInterceptLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate linearRegIntercept, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [linearRegIntercept]
     */
    fun linearRegIntercept(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearRegIntercept(series.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression Slope** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun linearRegSlope(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.linearRegSlope(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.linearRegSlopeLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate linearRegSlope, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [linearRegSlope]
     */
    fun linearRegSlope(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearRegSlope(series.close, timePeriod, previous)

    /**
     * Calculate **Vector Log Natural** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun ln(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.ln(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.lnLookback() + previous
            throw InsufficientData("Not enough data to calculate ln, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [ln]
     */
    fun ln(series: PriceBarSeries, previous: Int = 0) = ln(series.close, previous)

    /**
     * Calculate **Vector Log10** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun log10(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.log10(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.log10Lookback() + previous
            throw InsufficientData("Not enough data to calculate log10, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [log10]
     */
    fun log10(series: PriceBarSeries, previous: Int = 0) = log10(series.close, previous)

    /**
     * Calculate **Moving average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun movingAverage(data: DoubleArray, timePeriod: Int = 30, mAType: MAType = MAType.Ema, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.movingAverage(endIdx, endIdx, data, timePeriod, mAType, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.movingAverageLookback(timePeriod, mAType) + previous
            throw InsufficientData("Not enough data to calculate movingAverage, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [movingAverage]
     */
    fun movingAverage(series: PriceBarSeries, timePeriod: Int = 30, mAType: MAType = MAType.Ema, previous: Int = 0) =
        movingAverage(series.close, timePeriod, mAType, previous)

    /**
     * Calculate **Moving Average Convergence/Divergence** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.macdLookback(fastPeriod, slowPeriod, signalPeriod) + previous
            throw InsufficientData("Not enough data to calculate macd, minimal lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [macd]
     */
    fun macd(
        series: PriceBarSeries,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9,
        previous: Int = 0
    ) = macd(series.close, fastPeriod, slowPeriod, signalPeriod, previous)

    /**
     * Calculate **MACD with controllable MA type** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback =
                core.macdExtLookback(fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA) + previous
            throw InsufficientData("Not enough data to calculate macdExt, minimal lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [macdExt]
     */
    fun macdExt(
        series: PriceBarSeries,
        fastPeriod: Int = 12,
        fastMA: MAType = MAType.Ema,
        slowPeriod: Int = 26,
        slowMA: MAType = MAType.Ema,
        signalPeriod: Int = 9,
        signalMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = macdExt(series.close, fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA, previous)

    /**
     * Calculate **Moving Average Convergence/Divergence Fix 12/26** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun macdFix(data: DoubleArray, signalPeriod: Int = 9, previous: Int = 0): Triple<Double, Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val output3 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.macdFix(endIdx, endIdx, data, signalPeriod, startOutput, endOutput, output1, output2, output3)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.macdFixLookback(signalPeriod) + previous
            throw InsufficientData("Not enough data to calculate macdFix, minimal lookback period is $lookback")
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [macdFix]
     */
    fun macdFix(series: PriceBarSeries, signalPeriod: Int = 9, previous: Int = 0) =
        macdFix(series.close, signalPeriod, previous)

    /**
     * Calculate **MESA Adaptive Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.mamaLookback(fastLimit, slowLimit) + previous
            throw InsufficientData("Not enough data to calculate mama, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [mama]
     */
    fun mama(
        series: PriceBarSeries,
        fastLimit: Double = 5.000000e-1,
        slowLimit: Double = 5.000000e-2,
        previous: Int = 0
    ) = mama(series.close, fastLimit, slowLimit, previous)

    /**
     * Calculate **Moving average with variable period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.movingAverageVariablePeriodLookback(minimumPeriod, maximumPeriod, mAType) + previous
            throw InsufficientData("Not enough data to calculate movingAverageVariablePeriod, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Highest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun max(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.max(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.maxLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate max, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [max]
     */
    fun max(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = max(series.close, timePeriod, previous)

    /**
     * Calculate **Index of highest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun maxIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.maxIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.maxIndexLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate maxIndex, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [maxIndex]
     */
    fun maxIndex(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        maxIndex(series.close, timePeriod, previous)

    /**
     * Calculate **Median Price** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Price Transform**.
     */
    fun medPrice(high: DoubleArray, low: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.medPrice(endIdx, endIdx, high, low, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.medPriceLookback() + previous
            throw InsufficientData("Not enough data to calculate medPrice, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [medPrice]
     */
    fun medPrice(series: PriceBarSeries, previous: Int = 0) = medPrice(series.high, series.low, previous)

    /**
     * Calculate **Money Flow Index** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.mfiLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate mfi, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [mfi]
     */
    fun mfi(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        mfi(series.high, series.low, series.close, series.volume, timePeriod, previous)

    /**
     * Calculate **MidPoint over period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun midPoint(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPoint(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.midPointLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate midPoint, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [midPoint]
     */
    fun midPoint(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        midPoint(series.close, timePeriod, previous)

    /**
     * Calculate **Midpoint Price over period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun midPrice(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.midPrice(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.midPriceLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate midPrice, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [midPrice]
     */
    fun midPrice(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        midPrice(series.high, series.low, timePeriod, previous)

    /**
     * Calculate **Lowest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun min(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.min(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate min, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [min]
     */
    fun min(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = min(series.close, timePeriod, previous)

    /**
     * Calculate **Index of lowest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun minIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Int {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minIndexLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate minIndex, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [minIndex]
     */
    fun minIndex(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        minIndex(series.close, timePeriod, previous)

    /**
     * Calculate **Lowest and highest values over a specified period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun minMax(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Pair<Double, Double> {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val output2 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMax(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minMaxLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate minMax, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [minMax]
     */
    fun minMax(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        minMax(series.close, timePeriod, previous)

    /**
     * Calculate **Indexes of lowest and highest values over a specified period** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun minMaxIndex(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Pair<Int, Int> {
        val endIdx = data.lastIndex - previous
        val output1 = IntArray(1)
        val output2 = IntArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minMaxIndex(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1, output2)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minMaxIndexLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate minMaxIndex, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [minMaxIndex]
     */
    fun minMaxIndex(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        minMaxIndex(series.close, timePeriod, previous)

    /**
     * Calculate **Minus Directional Indicator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minusDILookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate minusDI, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [minusDI]
     */
    fun minusDI(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        minusDI(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Minus Directional Movement** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun minusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.minusDM(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.minusDMLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate minusDM, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [minusDM]
     */
    fun minusDM(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        minusDM(series.high, series.low, timePeriod, previous)

    /**
     * Calculate **Momentum** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun mom(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mom(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.momLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate mom, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [mom]
     */
    fun mom(series: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = mom(series.close, timePeriod, previous)

    /**
     * Calculate **Vector Arithmetic Mult** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun mult(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.mult(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.multLookback() + previous
            throw InsufficientData("Not enough data to calculate mult, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Normalized Average True Range** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Volatility Indicators**.
     */
    fun natr(high: DoubleArray, low: DoubleArray, close: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.natr(endIdx, endIdx, high, low, close, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.natrLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate natr, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [natr]
     */
    fun natr(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        natr(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **On Balance Volume** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Volume Indicators**.
     */
    fun obv(data: DoubleArray, volume: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.obv(endIdx, endIdx, data, volume, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.obvLookback() + previous
            throw InsufficientData("Not enough data to calculate obv, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Plus Directional Indicator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.plusDILookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate plusDI, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [plusDI]
     */
    fun plusDI(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        plusDI(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Plus Directional Movement** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun plusDM(high: DoubleArray, low: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.plusDM(endIdx, endIdx, high, low, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.plusDMLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate plusDM, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [plusDM]
     */
    fun plusDM(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        plusDM(series.high, series.low, timePeriod, previous)

    /**
     * Calculate **Percentage Price Oscillator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.ppoLookback(fastPeriod, slowPeriod, mAType) + previous
            throw InsufficientData("Not enough data to calculate ppo, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [ppo]
     */
    fun ppo(
        series: PriceBarSeries,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ) = ppo(series.close, fastPeriod, slowPeriod, mAType, previous)

    /**
     * Calculate **Rate of change : ((price/prevPrice)-1) * 100** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun roc(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.roc(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate roc, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [roc]
     */
    fun roc(series: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = roc(series.close, timePeriod, previous)

    /**
     * Calculate **Rate of change Percentage: (price-prevPrice)/prevPrice** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun rocP(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocP(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocPLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate rocP, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [rocP]
     */
    fun rocP(series: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = rocP(series.close, timePeriod, previous)

    /**
     * Calculate **Rate of change ratio: (price/prevPrice)** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun rocR(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocRLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate rocR, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [rocR]
     */
    fun rocR(series: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = rocR(series.close, timePeriod, previous)

    /**
     * Calculate **Rate of change ratio 100 scale: (price/prevPrice) * 100** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun rocR100(data: DoubleArray, timePeriod: Int = 10, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rocR100(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rocR100Lookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate rocR100, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [rocR100]
     */
    fun rocR100(series: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) =
        rocR100(series.close, timePeriod, previous)

    /**
     * Calculate **Relative Strength Index** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun rsi(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.rsi(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.rsiLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate rsi, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [rsi]
     */
    fun rsi(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) = rsi(series.close, timePeriod, previous)

    /**
     * Calculate **Parabolic SAR** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sarLookback(accelerationFactor, aFMaximum) + previous
            throw InsufficientData("Not enough data to calculate sar, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sar]
     */
    fun sar(
        series: PriceBarSeries,
        accelerationFactor: Double = 2.000000e-2,
        aFMaximum: Double = 2.000000e-1,
        previous: Int = 0
    ) = sar(series.high, series.low, accelerationFactor, aFMaximum, previous)

    /**
     * Calculate **Parabolic SAR - Extended** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
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
            ) + previous
            throw InsufficientData("Not enough data to calculate sarExt, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sarExt]
     */
    fun sarExt(
        series: PriceBarSeries,
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
        series.high,
        series.low,
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
     * Calculate **Vector Trigonometric Sin** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun sin(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sin(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sinLookback() + previous
            throw InsufficientData("Not enough data to calculate sin, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sin]
     */
    fun sin(series: PriceBarSeries, previous: Int = 0) = sin(series.close, previous)

    /**
     * Calculate **Vector Trigonometric Sinh** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun sinh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sinh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sinhLookback() + previous
            throw InsufficientData("Not enough data to calculate sinh, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sinh]
     */
    fun sinh(series: PriceBarSeries, previous: Int = 0) = sinh(series.close, previous)

    /**
     * Calculate **Simple Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun sma(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sma(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.smaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate sma, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sma]
     */
    fun sma(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = sma(series.close, timePeriod, previous)

    /**
     * Calculate **Vector Square Root** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun sqrt(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sqrt(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sqrtLookback() + previous
            throw InsufficientData("Not enough data to calculate sqrt, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sqrt]
     */
    fun sqrt(series: PriceBarSeries, previous: Int = 0) = sqrt(series.close, previous)

    /**
     * Calculate **Standard Deviation** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun stdDev(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.stdDev(endIdx, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.stdDevLookback(timePeriod, deviations) + previous
            throw InsufficientData("Not enough data to calculate stdDev, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [stdDev]
     */
    fun stdDev(series: PriceBarSeries, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0) =
        stdDev(series.close, timePeriod, deviations, previous)

    /**
     * Calculate **Stochastic** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.stochLookback(fastKPeriod, slowKPeriod, slowKMA, slowDPeriod, slowDMA) + previous
            throw InsufficientData("Not enough data to calculate stoch, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [stoch]
     */
    fun stoch(
        series: PriceBarSeries,
        fastKPeriod: Int = 5,
        slowKPeriod: Int = 3,
        slowKMA: MAType = MAType.Ema,
        slowDPeriod: Int = 3,
        slowDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stoch(series.high, series.low, series.close, fastKPeriod, slowKPeriod, slowKMA, slowDPeriod, slowDMA, previous)

    /**
     * Calculate **Stochastic Fast** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.stochFLookback(fastKPeriod, fastDPeriod, fastDMA) + previous
            throw InsufficientData("Not enough data to calculate stochF, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [stochF]
     */
    fun stochF(
        series: PriceBarSeries,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stochF(series.high, series.low, series.close, fastKPeriod, fastDPeriod, fastDMA, previous)

    /**
     * Calculate **Stochastic Relative Strength Index** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.stochRsiLookback(timePeriod, fastKPeriod, fastDPeriod, fastDMA) + previous
            throw InsufficientData("Not enough data to calculate stochRsi, minimal lookback period is $lookback")
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [stochRsi]
     */
    fun stochRsi(
        series: PriceBarSeries,
        timePeriod: Int = 14,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stochRsi(series.close, timePeriod, fastKPeriod, fastDPeriod, fastDMA, previous)

    /**
     * Calculate **Vector Arithmetic Subtraction** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun sub(data0: DoubleArray, data1: DoubleArray, previous: Int = 0): Double {
        val endIdx = data0.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sub(endIdx, endIdx, data0, data1, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.subLookback() + previous
            throw InsufficientData("Not enough data to calculate sub, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Calculate **Summation** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Operators**.
     */
    fun sum(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.sum(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.sumLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate sum, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [sum]
     */
    fun sum(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = sum(series.close, timePeriod, previous)

    /**
     * Calculate **Triple Exponential Moving Average (T3)** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun t3(data: DoubleArray, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.t3(endIdx, endIdx, data, timePeriod, volumeFactor, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.t3Lookback(timePeriod, volumeFactor) + previous
            throw InsufficientData("Not enough data to calculate t3, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [t3]
     */
    fun t3(series: PriceBarSeries, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1, previous: Int = 0) =
        t3(series.close, timePeriod, volumeFactor, previous)

    /**
     * Calculate **Vector Trigonometric Tan** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun tan(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tan(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.tanLookback() + previous
            throw InsufficientData("Not enough data to calculate tan, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [tan]
     */
    fun tan(series: PriceBarSeries, previous: Int = 0) = tan(series.close, previous)

    /**
     * Calculate **Vector Trigonometric Tanh** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Math Transform**.
     */
    fun tanh(data: DoubleArray, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tanh(endIdx, endIdx, data, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.tanhLookback() + previous
            throw InsufficientData("Not enough data to calculate tanh, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [tanh]
     */
    fun tanh(series: PriceBarSeries, previous: Int = 0) = tanh(series.close, previous)

    /**
     * Calculate **Triple Exponential Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun tema(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tema(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.temaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate tema, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [tema]
     */
    fun tema(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = tema(series.close, timePeriod, previous)

    /**
     * Calculate **True Range** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Volatility Indicators**.
     */
    fun trueRange(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trueRange(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.trueRangeLookback() + previous
            throw InsufficientData("Not enough data to calculate trueRange, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [trueRange]
     */
    fun trueRange(series: PriceBarSeries, previous: Int = 0) =
        trueRange(series.high, series.low, series.close, previous)

    /**
     * Calculate **Triangular Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun trima(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trima(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.trimaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate trima, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [trima]
     */
    fun trima(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        trima(series.close, timePeriod, previous)

    /**
     * Calculate **1-day Rate-Of-Change (ROC) of a Triple Smooth EMA** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
     */
    fun trix(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.trix(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.trixLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate trix, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [trix]
     */
    fun trix(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = trix(series.close, timePeriod, previous)

    /**
     * Calculate **Time Series Forecast** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun tsf(data: DoubleArray, timePeriod: Int = 14, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.tsf(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.tsfLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate tsf, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [tsf]
     */
    fun tsf(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) = tsf(series.close, timePeriod, previous)

    /**
     * Calculate **Typical Price** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Price Transform**.
     */
    fun typPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.typPrice(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.typPriceLookback() + previous
            throw InsufficientData("Not enough data to calculate typPrice, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [typPrice]
     */
    fun typPrice(series: PriceBarSeries, previous: Int = 0) = typPrice(series.high, series.low, series.close, previous)

    /**
     * Calculate **Ultimate Oscillator** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.ultOscLookback(firstPeriod, secondPeriod, thirdPeriod) + previous
            throw InsufficientData("Not enough data to calculate ultOsc, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [ultOsc]
     */
    fun ultOsc(
        series: PriceBarSeries,
        firstPeriod: Int = 7,
        secondPeriod: Int = 14,
        thirdPeriod: Int = 28,
        previous: Int = 0
    ) = ultOsc(series.high, series.low, series.close, firstPeriod, secondPeriod, thirdPeriod, previous)

    /**
     * Calculate **Variance** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Statistic Functions**.
     */
    fun variance(data: DoubleArray, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.variance(endIdx, endIdx, data, timePeriod, deviations, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.varianceLookback(timePeriod, deviations) + previous
            throw InsufficientData("Not enough data to calculate variance, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [variance]
     */
    fun variance(series: PriceBarSeries, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0) =
        variance(series.close, timePeriod, deviations, previous)

    /**
     * Calculate **Weighted Close Price** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Price Transform**.
     */
    fun wclPrice(high: DoubleArray, low: DoubleArray, close: DoubleArray, previous: Int = 0): Double {
        val endIdx = high.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wclPrice(endIdx, endIdx, high, low, close, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.wclPriceLookback() + previous
            throw InsufficientData("Not enough data to calculate wclPrice, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [wclPrice]
     */
    fun wclPrice(series: PriceBarSeries, previous: Int = 0) = wclPrice(series.high, series.low, series.close, previous)

    /**
     * Calculate **Williams' %R** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Momentum Indicators**.
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
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.willRLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate willR, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [willR]
     */
    fun willR(series: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        willR(series.high, series.low, series.close, timePeriod, previous)

    /**
     * Calculate **Weighted Moving Average** using the provided input data and by default return the most recent result.
     * You can set previous if you don't want the most recent result.
     * If there is insufficient data to calculate the indicators, an [InsufficientData] will be thrown.
     *
     * This indicator belongs to the group **Overlap Studies**.
     */
    fun wma(data: DoubleArray, timePeriod: Int = 30, previous: Int = 0): Double {
        val endIdx = data.lastIndex - previous
        val output1 = DoubleArray(1)
        val startOutput = MInteger()
        val endOutput = MInteger()
        val ret = core.wma(endIdx, endIdx, data, timePeriod, startOutput, endOutput, output1)
        if (ret != RetCode.Success) throw DoesNotComputeException(ret.toString())
        val last = endOutput.value - 1
        if (last < 0) {
            val lookback = core.wmaLookback(timePeriod) + previous
            throw InsufficientData("Not enough data to calculate wma, minimal lookback period is $lookback")
        }
        return output1[0]
    }

    /**
     * Simple wrapper that allows to use PricebarSeries as input.
     * @see [wma]
     */
    fun wma(series: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = wma(series.close, timePeriod, previous)
}

