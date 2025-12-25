/*
 * Copyright 2020-2026 Neural Layer
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

@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "unused",
    "LargeClass",
    "TooManyFunctions",
    "WildcardImport",
    "MaxLineLength",
    "LongParameterList", "SpellCheckingInspection", "GrazieInspection"
)

package org.roboquant.ta

import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MAType
import com.tictactec.ta.lib.MInteger
import com.tictactec.ta.lib.RetCode
import org.roboquant.common.DoesNotComputeException

/**
 * This exception is thrown if there is insufficient historic data to calculate an indicator. Since this exception is
 * also used to also automatically increase historic buffer sizes, the exception is optimized to not include a
 * stack trace.
 *
 * @param indicator the inidcator name
 * @property minSize the minimum amount of data to calculate this indicator
 */
class InsufficientData(indicator: String, val minSize: Int) :
    Throwable("innsuffient data to calculate $indicator, miniumum is $minSize", null, true, false)

/**
 * This class wraps the excellent TA-Lib library and makes it easy to use indicators provided by that library.
 * This wrapper is optimized for usage within roboquant and supports streaming/event based updates.
 *
 * @see TaLibMetric
 * @see TaLibStrategy
 *
 * @property core The TaLib core library that does the actual execution of the indicactors
 */
class TaLib(var core: Core = Core()) {

    /**
     * Calculate **Vector Trigonometric ACos** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.acosLookback() + previous
            throw InsufficientData("acos", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [acos]
     */
    fun acos(serie: PriceBarSeries, previous: Int = 0) = acos(serie.close, previous)

    /**
     * Calculate **Chaikin A/D Line** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.adLookback() + previous
            throw InsufficientData("ad", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [ad]
     */
    fun ad(serie: PriceBarSeries, previous: Int = 0) =
        ad(serie.high, serie.low, serie.close, serie.volume, previous)

    /**
     * Calculate **Vector Arithmetic Add** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.addLookback() + previous
            throw InsufficientData("add", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Chaikin A/D Oscillator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.adOscLookback(fastPeriod, slowPeriod) + previous
            throw InsufficientData("adOsc", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [adOsc]
     */
    fun adOsc(serie: PriceBarSeries, fastPeriod: Int = 3, slowPeriod: Int = 10, previous: Int = 0) =
        adOsc(serie.high, serie.low, serie.close, serie.volume, fastPeriod, slowPeriod, previous)

    /**
     * Calculate **Average Directional Movement MetadataProvider** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.adxLookback(timePeriod) + previous
            throw InsufficientData("adx", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [adx]
     */
    fun adx(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        adx(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Average Directional Movement MetadataProvider Rating** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.adxrLookback(timePeriod) + previous
            throw InsufficientData("adxr", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [adxr]
     */
    fun adxr(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        adxr(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Absolute Price Oscillator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.apoLookback(fastPeriod, slowPeriod, mAType) + previous
            throw InsufficientData("apo", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [apo]
     */
    fun apo(
        serie: PriceBarSeries,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ) = apo(serie.close, fastPeriod, slowPeriod, mAType, previous)

    /**
     * Calculate **Aroon** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.aroonLookback(timePeriod) + previous
            throw InsufficientData("aroon", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [aroon]
     */
    fun aroon(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        aroon(serie.high, serie.low, timePeriod, previous)

    /**
     * Calculate **Aroon Oscillator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.aroonOscLookback(timePeriod) + previous
            throw InsufficientData("aroonOsc", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [aroonOsc]
     */
    fun aroonOsc(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        aroonOsc(serie.high, serie.low, timePeriod, previous)

    /**
     * Calculate **Vector Trigonometric ASin** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.asinLookback() + previous
            throw InsufficientData("asin", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [asin]
     */
    fun asin(serie: PriceBarSeries, previous: Int = 0) = asin(serie.close, previous)

    /**
     * Calculate **Vector Trigonometric ATan** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.atanLookback() + previous
            throw InsufficientData("atan", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [atan]
     */
    fun atan(serie: PriceBarSeries, previous: Int = 0) = atan(serie.close, previous)

    /**
     * Calculate **Average True Range** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.atrLookback(timePeriod) + previous
            throw InsufficientData("atr", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [atr]
     */
    fun atr(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        atr(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Average Price** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.avgPriceLookback() + previous
            throw InsufficientData("avgPrice", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [avgPrice]
     */
    fun avgPrice(serie: PriceBarSeries, previous: Int = 0) =
        avgPrice(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Bollinger Bands** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.bbandsLookback(timePeriod, deviationsup, deviationsdown, mAType) + previous
            throw InsufficientData("bbands", lookback + 1)
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [bbands]
     */
    fun bbands(
        serie: PriceBarSeries,
        timePeriod: Int = 5,
        deviationsup: Double = 2.000000e+0,
        deviationsdown: Double = 2.000000e+0,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ) = bbands(serie.close, timePeriod, deviationsup, deviationsdown, mAType, previous)

    /**
     * Calculate **Beta** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.betaLookback(timePeriod) + previous
            throw InsufficientData("beta", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Balance Of Power** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.bopLookback() + previous
            throw InsufficientData("bop", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [bop]
     */
    fun bop(serie: PriceBarSeries, previous: Int = 0) =
        bop(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Commodity Channel MetadataProvider** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cciLookback(timePeriod) + previous
            throw InsufficientData("cci", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cci]
     */
    fun cci(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        cci(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Two Crows** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl2CrowsLookback() + previous
            throw InsufficientData("cdl2Crows", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl2Crows]
     */
    fun cdl2Crows(serie: PriceBarSeries, previous: Int = 0) =
        cdl2Crows(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Three Black Crows** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl3BlackCrowsLookback() + previous
            throw InsufficientData("cdl3BlackCrows", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl3BlackCrows]
     */
    fun cdl3BlackCrows(serie: PriceBarSeries, previous: Int = 0) =
        cdl3BlackCrows(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Three Inside Up/Down** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl3InsideLookback() + previous
            throw InsufficientData("cdl3Inside", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl3Inside]
     */
    fun cdl3Inside(serie: PriceBarSeries, previous: Int = 0) =
        cdl3Inside(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Three-Line Strike** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl3LineStrikeLookback() + previous
            throw InsufficientData("cdl3LineStrike", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl3LineStrike]
     */
    fun cdl3LineStrike(serie: PriceBarSeries, previous: Int = 0) =
        cdl3LineStrike(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Three Outside Up/Down** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl3OutsideLookback() + previous
            throw InsufficientData("cdl3Outside", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl3Outside]
     */
    fun cdl3Outside(serie: PriceBarSeries, previous: Int = 0) =
        cdl3Outside(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Three Stars In The South** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl3StarsInSouthLookback() + previous
            throw InsufficientData("cdl3StarsInSouth", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl3StarsInSouth]
     */
    fun cdl3StarsInSouth(serie: PriceBarSeries, previous: Int = 0) =
        cdl3StarsInSouth(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Three Advancing White Soldiers** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdl3WhiteSoldiersLookback() + previous
            throw InsufficientData("cdl3WhiteSoldiers", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdl3WhiteSoldiers]
     */
    fun cdl3WhiteSoldiers(serie: PriceBarSeries, previous: Int = 0) =
        cdl3WhiteSoldiers(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Abandoned Baby** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlAbandonedBabyLookback(penetration) + previous
            throw InsufficientData("cdlAbandonedBaby", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlAbandonedBaby]
     */
    fun cdlAbandonedBaby(serie: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlAbandonedBaby(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **Advance Block** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlAdvanceBlockLookback() + previous
            throw InsufficientData("cdlAdvanceBlock", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlAdvanceBlock]
     */
    fun cdlAdvanceBlock(serie: PriceBarSeries, previous: Int = 0) =
        cdlAdvanceBlock(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Belt-hold** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlBeltHoldLookback() + previous
            throw InsufficientData("cdlBeltHold", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlBeltHold]
     */
    fun cdlBeltHold(serie: PriceBarSeries, previous: Int = 0) =
        cdlBeltHold(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Breakaway** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlBreakawayLookback() + previous
            throw InsufficientData("cdlBreakaway", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlBreakaway]
     */
    fun cdlBreakaway(serie: PriceBarSeries, previous: Int = 0) =
        cdlBreakaway(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Closing Marubozu** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlClosingMarubozuLookback() + previous
            throw InsufficientData("cdlClosingMarubozu", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlClosingMarubozu]
     */
    fun cdlClosingMarubozu(serie: PriceBarSeries, previous: Int = 0) =
        cdlClosingMarubozu(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Concealing Baby Swallow** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlConcealBabysWallLookback() + previous
            throw InsufficientData("cdlConcealBabysWall", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlConcealBabysWall]
     */
    fun cdlConcealBabysWall(serie: PriceBarSeries, previous: Int = 0) =
        cdlConcealBabysWall(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Counterattack** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlCounterAttackLookback() + previous
            throw InsufficientData("cdlCounterAttack", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlCounterAttack]
     */
    fun cdlCounterAttack(serie: PriceBarSeries, previous: Int = 0) =
        cdlCounterAttack(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Dark Cloud Cover** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlDarkCloudCoverLookback(penetration) + previous
            throw InsufficientData("cdlDarkCloudCover", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlDarkCloudCover]
     */
    fun cdlDarkCloudCover(serie: PriceBarSeries, penetration: Double = 5.000000e-1, previous: Int = 0) =
        cdlDarkCloudCover(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **Doji** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlDojiLookback() + previous
            throw InsufficientData("cdlDoji", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlDoji]
     */
    fun cdlDoji(serie: PriceBarSeries, previous: Int = 0) =
        cdlDoji(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Doji Star** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlDojiStarLookback() + previous
            throw InsufficientData("cdlDojiStar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlDojiStar]
     */
    fun cdlDojiStar(serie: PriceBarSeries, previous: Int = 0) =
        cdlDojiStar(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Dragonfly Doji** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlDragonflyDojiLookback() + previous
            throw InsufficientData("cdlDragonflyDoji", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlDragonflyDoji]
     */
    fun cdlDragonflyDoji(serie: PriceBarSeries, previous: Int = 0) =
        cdlDragonflyDoji(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Engulfing Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlEngulfingLookback() + previous
            throw InsufficientData("cdlEngulfing", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlEngulfing]
     */
    fun cdlEngulfing(serie: PriceBarSeries, previous: Int = 0) =
        cdlEngulfing(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Evening Doji Star** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlEveningDojiStarLookback(penetration) + previous
            throw InsufficientData("cdlEveningDojiStar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlEveningDojiStar]
     */
    fun cdlEveningDojiStar(serie: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlEveningDojiStar(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **Evening Star** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlEveningStarLookback(penetration) + previous
            throw InsufficientData("cdlEveningStar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlEveningStar]
     */
    fun cdlEveningStar(serie: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlEveningStar(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **Up/Down-gap side-by-side white lines** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlGapSideSideWhiteLookback() + previous
            throw InsufficientData("cdlGapSideSideWhite", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlGapSideSideWhite]
     */
    fun cdlGapSideSideWhite(serie: PriceBarSeries, previous: Int = 0) =
        cdlGapSideSideWhite(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Gravestone Doji** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlGravestoneDojiLookback() + previous
            throw InsufficientData("cdlGravestoneDoji", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlGravestoneDoji]
     */
    fun cdlGravestoneDoji(serie: PriceBarSeries, previous: Int = 0) =
        cdlGravestoneDoji(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Hammer** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHammerLookback() + previous
            throw InsufficientData("cdlHammer", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHammer]
     */
    fun cdlHammer(serie: PriceBarSeries, previous: Int = 0) =
        cdlHammer(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Hanging Man** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHangingManLookback() + previous
            throw InsufficientData("cdlHangingMan", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHangingMan]
     */
    fun cdlHangingMan(serie: PriceBarSeries, previous: Int = 0) =
        cdlHangingMan(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Harami Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHaramiLookback() + previous
            throw InsufficientData("cdlHarami", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHarami]
     */
    fun cdlHarami(serie: PriceBarSeries, previous: Int = 0) =
        cdlHarami(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Harami Cross Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHaramiCrossLookback() + previous
            throw InsufficientData("cdlHaramiCross", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHaramiCross]
     */
    fun cdlHaramiCross(serie: PriceBarSeries, previous: Int = 0) =
        cdlHaramiCross(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **High-Wave Candle** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHignWaveLookback() + previous
            throw InsufficientData("cdlHignWave", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHignWave]
     */
    fun cdlHignWave(serie: PriceBarSeries, previous: Int = 0) =
        cdlHignWave(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Hikkake Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHikkakeLookback() + previous
            throw InsufficientData("cdlHikkake", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHikkake]
     */
    fun cdlHikkake(serie: PriceBarSeries, previous: Int = 0) =
        cdlHikkake(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Modified Hikkake Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHikkakeModLookback() + previous
            throw InsufficientData("cdlHikkakeMod", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHikkakeMod]
     */
    fun cdlHikkakeMod(serie: PriceBarSeries, previous: Int = 0) =
        cdlHikkakeMod(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Homing Pigeon** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlHomingPigeonLookback() + previous
            throw InsufficientData("cdlHomingPigeon", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlHomingPigeon]
     */
    fun cdlHomingPigeon(serie: PriceBarSeries, previous: Int = 0) =
        cdlHomingPigeon(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Identical Three Crows** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlIdentical3CrowsLookback() + previous
            throw InsufficientData("cdlIdentical3Crows", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlIdentical3Crows]
     */
    fun cdlIdentical3Crows(serie: PriceBarSeries, previous: Int = 0) =
        cdlIdentical3Crows(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **In-Neck Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlInNeckLookback() + previous
            throw InsufficientData("cdlInNeck", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlInNeck]
     */
    fun cdlInNeck(serie: PriceBarSeries, previous: Int = 0) =
        cdlInNeck(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Inverted Hammer** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlInvertedHammerLookback() + previous
            throw InsufficientData("cdlInvertedHammer", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlInvertedHammer]
     */
    fun cdlInvertedHammer(serie: PriceBarSeries, previous: Int = 0) =
        cdlInvertedHammer(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Kicking** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlKickingLookback() + previous
            throw InsufficientData("cdlKicking", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlKicking]
     */
    fun cdlKicking(serie: PriceBarSeries, previous: Int = 0) =
        cdlKicking(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Kicking - bull/bear determined by the longer marubozu** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlKickingByLengthLookback() + previous
            throw InsufficientData("cdlKickingByLength", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlKickingByLength]
     */
    fun cdlKickingByLength(serie: PriceBarSeries, previous: Int = 0) =
        cdlKickingByLength(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Ladder Bottom** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlLadderBottomLookback() + previous
            throw InsufficientData("cdlLadderBottom", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlLadderBottom]
     */
    fun cdlLadderBottom(serie: PriceBarSeries, previous: Int = 0) =
        cdlLadderBottom(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Long Legged Doji** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlLongLeggedDojiLookback() + previous
            throw InsufficientData("cdlLongLeggedDoji", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlLongLeggedDoji]
     */
    fun cdlLongLeggedDoji(serie: PriceBarSeries, previous: Int = 0) =
        cdlLongLeggedDoji(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Long Line Candle** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlLongLineLookback() + previous
            throw InsufficientData("cdlLongLine", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlLongLine]
     */
    fun cdlLongLine(serie: PriceBarSeries, previous: Int = 0) =
        cdlLongLine(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Marubozu** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlMarubozuLookback() + previous
            throw InsufficientData("cdlMarubozu", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlMarubozu]
     */
    fun cdlMarubozu(serie: PriceBarSeries, previous: Int = 0) =
        cdlMarubozu(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Matching Low** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlMatchingLowLookback() + previous
            throw InsufficientData("cdlMatchingLow", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlMatchingLow]
     */
    fun cdlMatchingLow(serie: PriceBarSeries, previous: Int = 0) =
        cdlMatchingLow(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Mat Hold** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlMatHoldLookback(penetration) + previous
            throw InsufficientData("cdlMatHold", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlMatHold]
     */
    fun cdlMatHold(serie: PriceBarSeries, penetration: Double = 5.000000e-1, previous: Int = 0) =
        cdlMatHold(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **Morning Doji Star** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlMorningDojiStarLookback(penetration) + previous
            throw InsufficientData("cdlMorningDojiStar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlMorningDojiStar]
     */
    fun cdlMorningDojiStar(serie: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlMorningDojiStar(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **Morning Star** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlMorningStarLookback(penetration) + previous
            throw InsufficientData("cdlMorningStar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlMorningStar]
     */
    fun cdlMorningStar(serie: PriceBarSeries, penetration: Double = 3.000000e-1, previous: Int = 0) =
        cdlMorningStar(serie.open, serie.high, serie.low, serie.close, penetration, previous)

    /**
     * Calculate **On-Neck Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlOnNeckLookback() + previous
            throw InsufficientData("cdlOnNeck", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlOnNeck]
     */
    fun cdlOnNeck(serie: PriceBarSeries, previous: Int = 0) =
        cdlOnNeck(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Piercing Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlPiercingLookback() + previous
            throw InsufficientData("cdlPiercing", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlPiercing]
     */
    fun cdlPiercing(serie: PriceBarSeries, previous: Int = 0) =
        cdlPiercing(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Rickshaw Man** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlRickshawManLookback() + previous
            throw InsufficientData("cdlRickshawMan", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlRickshawMan]
     */
    fun cdlRickshawMan(serie: PriceBarSeries, previous: Int = 0) =
        cdlRickshawMan(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Rising/Falling Three Methods** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlRiseFall3MethodsLookback() + previous
            throw InsufficientData("cdlRiseFall3Methods", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlRiseFall3Methods]
     */
    fun cdlRiseFall3Methods(serie: PriceBarSeries, previous: Int = 0) =
        cdlRiseFall3Methods(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Separating Lines** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlSeperatingLinesLookback() + previous
            throw InsufficientData("cdlSeperatingLines", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlSeperatingLines]
     */
    fun cdlSeperatingLines(serie: PriceBarSeries, previous: Int = 0) =
        cdlSeperatingLines(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Shooting Star** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlShootingStarLookback() + previous
            throw InsufficientData("cdlShootingStar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlShootingStar]
     */
    fun cdlShootingStar(serie: PriceBarSeries, previous: Int = 0) =
        cdlShootingStar(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Short Line Candle** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlShortLineLookback() + previous
            throw InsufficientData("cdlShortLine", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlShortLine]
     */
    fun cdlShortLine(serie: PriceBarSeries, previous: Int = 0) =
        cdlShortLine(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Spinning Top** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlSpinningTopLookback() + previous
            throw InsufficientData("cdlSpinningTop", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlSpinningTop]
     */
    fun cdlSpinningTop(serie: PriceBarSeries, previous: Int = 0) =
        cdlSpinningTop(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Stalled Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlStalledPatternLookback() + previous
            throw InsufficientData("cdlStalledPattern", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlStalledPattern]
     */
    fun cdlStalledPattern(serie: PriceBarSeries, previous: Int = 0) =
        cdlStalledPattern(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Stick Sandwich** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlStickSandwhichLookback() + previous
            throw InsufficientData("cdlStickSandwich", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlStickSandwich]
     */
    fun cdlStickSandwich(serie: PriceBarSeries, previous: Int = 0) =
        cdlStickSandwich(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Takuri (Dragonfly Doji with very long lower shadow)** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlTakuriLookback() + previous
            throw InsufficientData("cdlTakuri", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlTakuri]
     */
    fun cdlTakuri(serie: PriceBarSeries, previous: Int = 0) =
        cdlTakuri(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Tasuki Gap** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlTasukiGapLookback() + previous
            throw InsufficientData("cdlTasukiGap", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlTasukiGap]
     */
    fun cdlTasukiGap(serie: PriceBarSeries, previous: Int = 0) =
        cdlTasukiGap(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Thrusting Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlThrustingLookback() + previous
            throw InsufficientData("cdlThrusting", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlThrusting]
     */
    fun cdlThrusting(serie: PriceBarSeries, previous: Int = 0) =
        cdlThrusting(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Tristar Pattern** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlTristarLookback() + previous
            throw InsufficientData("cdlTristar", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlTristar]
     */
    fun cdlTristar(serie: PriceBarSeries, previous: Int = 0) =
        cdlTristar(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Unique 3 River** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlUnique3RiverLookback() + previous
            throw InsufficientData("cdlUnique3River", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlUnique3River]
     */
    fun cdlUnique3River(serie: PriceBarSeries, previous: Int = 0) =
        cdlUnique3River(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Upside Gap Two Crows** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlUpsideGap2CrowsLookback() + previous
            throw InsufficientData("cdlUpsideGap2Crows", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlUpsideGap2Crows]
     */
    fun cdlUpsideGap2Crows(serie: PriceBarSeries, previous: Int = 0) =
        cdlUpsideGap2Crows(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Upside/Downside Gap Three Methods** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cdlXSideGap3MethodsLookback() + previous
            throw InsufficientData("cdlXSideGap3Methods", lookback + 1)
        }
        return output1[0] != 0
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cdlXSideGap3Methods]
     */
    fun cdlXSideGap3Methods(serie: PriceBarSeries, previous: Int = 0) =
        cdlXSideGap3Methods(serie.open, serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Vector Ceil** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.ceilLookback() + previous
            throw InsufficientData("ceil", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [ceil]
     */
    fun ceil(serie: PriceBarSeries, previous: Int = 0) = ceil(serie.close, previous)

    /**
     * Calculate **Chande Momentum Oscillator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cmoLookback(timePeriod) + previous
            throw InsufficientData("cmo", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cmo]
     */
    fun cmo(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) = cmo(serie.close, timePeriod, previous)

    /**
     * Calculate **Pearson's Correlation Coefficient (r)** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.correlLookback(timePeriod) + previous
            throw InsufficientData("correl", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Vector Trigonometric Cos** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.cosLookback() + previous
            throw InsufficientData("cos", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cos]
     */
    fun cos(serie: PriceBarSeries, previous: Int = 0) = cos(serie.close, previous)

    /**
     * Calculate **Vector Trigonometric Cosh** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.coshLookback() + previous
            throw InsufficientData("cosh", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [cosh]
     */
    fun cosh(serie: PriceBarSeries, previous: Int = 0) = cosh(serie.close, previous)

    /**
     * Calculate **Double Exponential Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.demaLookback(timePeriod) + previous
            throw InsufficientData("dema", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [dema]
     */
    fun dema(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = dema(serie.close, timePeriod, previous)

    /**
     * Calculate **Vector Arithmetic Div** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.divLookback() + previous
            throw InsufficientData("div", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Directional Movement MetadataProvider** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.dxLookback(timePeriod) + previous
            throw InsufficientData("dx", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [dx]
     */
    fun dx(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        dx(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Exponential Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.emaLookback(timePeriod) + previous
            throw InsufficientData("ema", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [ema]
     */
    fun ema(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = ema(serie.close, timePeriod, previous)

    /**
     * Calculate **Vector Arithmetic Exp** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.expLookback() + previous
            throw InsufficientData("exp", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [exp]
     */
    fun exp(serie: PriceBarSeries, previous: Int = 0) = exp(serie.close, previous)

    /**
     * Calculate **Vector Floor** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.floorLookback() + previous
            throw InsufficientData("floor", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [floor]
     */
    fun floor(serie: PriceBarSeries, previous: Int = 0) = floor(serie.close, previous)

    /**
     * Calculate **Hilbert Transform - Dominant Cycle Period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.htDcPeriodLookback() + previous
            throw InsufficientData("htDcPeriod", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [htDcPeriod]
     */
    fun htDcPeriod(serie: PriceBarSeries, previous: Int = 0) = htDcPeriod(serie.close, previous)

    /**
     * Calculate **Hilbert Transform - Dominant Cycle Phase** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.htDcPhaseLookback() + previous
            throw InsufficientData("htDcPhase", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [htDcPhase]
     */
    fun htDcPhase(serie: PriceBarSeries, previous: Int = 0) = htDcPhase(serie.close, previous)

    /**
     * Calculate **Hilbert Transform - Phasor Components** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.htPhasorLookback() + previous
            throw InsufficientData("htPhasor", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [htPhasor]
     */
    fun htPhasor(serie: PriceBarSeries, previous: Int = 0) = htPhasor(serie.close, previous)

    /**
     * Calculate **Hilbert Transform - SineWave** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.htSineLookback() + previous
            throw InsufficientData("htSine", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [htSine]
     */
    fun htSine(serie: PriceBarSeries, previous: Int = 0) = htSine(serie.close, previous)

    /**
     * Calculate **Hilbert Transform - Instantaneous Trendline** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.htTrendlineLookback() + previous
            throw InsufficientData("htTrendline", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [htTrendline]
     */
    fun htTrendline(serie: PriceBarSeries, previous: Int = 0) = htTrendline(serie.close, previous)

    /**
     * Calculate **Hilbert Transform - Trend vs Cycle Mode** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.htTrendModeLookback() + previous
            throw InsufficientData("htTrendMode", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [htTrendMode]
     */
    fun htTrendMode(serie: PriceBarSeries, previous: Int = 0) = htTrendMode(serie.close, previous)

    /**
     * Calculate **Kaufman Adaptive Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.kamaLookback(timePeriod) + previous
            throw InsufficientData("kama", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [kama]
     */
    fun kama(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = kama(serie.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.linearRegLookback(timePeriod) + previous
            throw InsufficientData("linearReg", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [linearReg]
     */
    fun linearReg(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearReg(serie.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression Angle** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.linearRegAngleLookback(timePeriod) + previous
            throw InsufficientData("linearRegAngle", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [linearRegAngle]
     */
    fun linearRegAngle(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearRegAngle(serie.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression Intercept** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.linearRegInterceptLookback(timePeriod) + previous
            throw InsufficientData("linearRegIntercept", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [linearRegIntercept]
     */
    fun linearRegIntercept(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearRegIntercept(serie.close, timePeriod, previous)

    /**
     * Calculate **Linear Regression Slope** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.linearRegSlopeLookback(timePeriod) + previous
            throw InsufficientData("linearRegSlope", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [linearRegSlope]
     */
    fun linearRegSlope(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        linearRegSlope(serie.close, timePeriod, previous)

    /**
     * Calculate **Vector Log Natural** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.lnLookback() + previous
            throw InsufficientData("ln", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [ln]
     */
    fun ln(serie: PriceBarSeries, previous: Int = 0) = ln(serie.close, previous)

    /**
     * Calculate **Vector Log10** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.log10Lookback() + previous
            throw InsufficientData("log10", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [log10]
     */
    fun log10(serie: PriceBarSeries, previous: Int = 0) = log10(serie.close, previous)

    /**
     * Calculate **Moving average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.movingAverageLookback(timePeriod, mAType) + previous
            throw InsufficientData("movingAverage", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [movingAverage]
     */
    fun movingAverage(serie: PriceBarSeries, timePeriod: Int = 30, mAType: MAType = MAType.Ema, previous: Int = 0) =
        movingAverage(serie.close, timePeriod, mAType, previous)

    /**
     * Calculate **Moving Average Convergence/Divergence** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.macdLookback(fastPeriod, slowPeriod, signalPeriod) + previous
            throw InsufficientData("macd", lookback + 1)
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [macd]
     */
    fun macd(
        serie: PriceBarSeries,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9,
        previous: Int = 0
    ) = macd(serie.close, fastPeriod, slowPeriod, signalPeriod, previous)

    /**
     * Calculate **MACD with controllable MA type** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback =
                core.macdExtLookback(fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA) + previous
            throw InsufficientData("macdExt", lookback + 1)
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [macdExt]
     */
    fun macdExt(
        serie: PriceBarSeries,
        fastPeriod: Int = 12,
        fastMA: MAType = MAType.Ema,
        slowPeriod: Int = 26,
        slowMA: MAType = MAType.Ema,
        signalPeriod: Int = 9,
        signalMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = macdExt(serie.close, fastPeriod, fastMA, slowPeriod, slowMA, signalPeriod, signalMA, previous)

    /**
     * Calculate **Moving Average Convergence/Divergence Fix 12/26** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.macdFixLookback(signalPeriod) + previous
            throw InsufficientData("macdFix", lookback + 1)
        }
        return Triple(output1[0], output2[0], output3[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [macdFix]
     */
    fun macdFix(serie: PriceBarSeries, signalPeriod: Int = 9, previous: Int = 0) =
        macdFix(serie.close, signalPeriod, previous)

    /**
     * Calculate **MESA Adaptive Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.mamaLookback(fastLimit, slowLimit) + previous
            throw InsufficientData("mama", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [mama]
     */
    fun mama(
        serie: PriceBarSeries,
        fastLimit: Double = 5.000000e-1,
        slowLimit: Double = 5.000000e-2,
        previous: Int = 0
    ) = mama(serie.close, fastLimit, slowLimit, previous)

    /**
     * Calculate **Moving average with variable period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.movingAverageVariablePeriodLookback(minimumPeriod, maximumPeriod, mAType) + previous
            throw InsufficientData("movingAverageVariablePeriod", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Highest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.maxLookback(timePeriod) + previous
            throw InsufficientData("max", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [max]
     */
    fun max(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = max(serie.close, timePeriod, previous)

    /**
     * Calculate **MetadataProvider of highest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.maxIndexLookback(timePeriod) + previous
            throw InsufficientData("maxIndex", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [maxIndex]
     */
    fun maxIndex(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        maxIndex(serie.close, timePeriod, previous)

    /**
     * Calculate **Median Price** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.medPriceLookback() + previous
            throw InsufficientData("medPrice", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [medPrice]
     */
    fun medPrice(serie: PriceBarSeries, previous: Int = 0) = medPrice(serie.high, serie.low, previous)

    /**
     * Calculate **Money Flow MetadataProvider** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.mfiLookback(timePeriod) + previous
            throw InsufficientData("mfi", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [mfi]
     */
    fun mfi(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        mfi(serie.high, serie.low, serie.close, serie.volume, timePeriod, previous)

    /**
     * Calculate **MidPoint over period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.midPointLookback(timePeriod) + previous
            throw InsufficientData("midPoint", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [midPoint]
     */
    fun midPoint(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        midPoint(serie.close, timePeriod, previous)

    /**
     * Calculate **Midpoint Price over period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.midPriceLookback(timePeriod) + previous
            throw InsufficientData("midPrice", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [midPrice]
     */
    fun midPrice(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        midPrice(serie.high, serie.low, timePeriod, previous)

    /**
     * Calculate **Lowest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.minLookback(timePeriod) + previous
            throw InsufficientData("min", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [min]
     */
    fun min(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = min(serie.close, timePeriod, previous)

    /**
     * Calculate **MetadataProvider of lowest value over a specified period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.minIndexLookback(timePeriod) + previous
            throw InsufficientData("minIndex", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [minIndex]
     */
    fun minIndex(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        minIndex(serie.close, timePeriod, previous)

    /**
     * Calculate **Lowest and highest values over a specified period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.minMaxLookback(timePeriod) + previous
            throw InsufficientData("minMax", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [minMax]
     */
    fun minMax(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        minMax(serie.close, timePeriod, previous)

    /**
     * Calculate **Indexes of lowest and highest values over a specified period** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.minMaxIndexLookback(timePeriod) + previous
            throw InsufficientData("minMaxIndex", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [minMaxIndex]
     */
    fun minMaxIndex(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        minMaxIndex(serie.close, timePeriod, previous)

    /**
     * Calculate **Minus Directional Indicator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.minusDILookback(timePeriod) + previous
            throw InsufficientData("minusDI", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [minusDI]
     */
    fun minusDI(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        minusDI(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Minus Directional Movement** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.minusDMLookback(timePeriod) + previous
            throw InsufficientData("minusDM", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [minusDM]
     */
    fun minusDM(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        minusDM(serie.high, serie.low, timePeriod, previous)

    /**
     * Calculate **Momentum** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.momLookback(timePeriod) + previous
            throw InsufficientData("mom", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [mom]
     */
    fun mom(serie: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = mom(serie.close, timePeriod, previous)

    /**
     * Calculate **Vector Arithmetic Mult** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.multLookback() + previous
            throw InsufficientData("mult", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Normalized Average True Range** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.natrLookback(timePeriod) + previous
            throw InsufficientData("natr", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [natr]
     */
    fun natr(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        natr(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **On Balance Volume** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.obvLookback() + previous
            throw InsufficientData("obv", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Plus Directional Indicator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.plusDILookback(timePeriod) + previous
            throw InsufficientData("plusDI", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [plusDI]
     */
    fun plusDI(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        plusDI(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Plus Directional Movement** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.plusDMLookback(timePeriod) + previous
            throw InsufficientData("plusDM", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [plusDM]
     */
    fun plusDM(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        plusDM(serie.high, serie.low, timePeriod, previous)

    /**
     * Calculate **Percentage Price Oscillator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.ppoLookback(fastPeriod, slowPeriod, mAType) + previous
            throw InsufficientData("ppo", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [ppo]
     */
    fun ppo(
        serie: PriceBarSeries,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        mAType: MAType = MAType.Ema,
        previous: Int = 0
    ) = ppo(serie.close, fastPeriod, slowPeriod, mAType, previous)

    /**
     * Calculate **Rate of change : ((price/prevPrice)-1) * 100** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.rocLookback(timePeriod) + previous
            throw InsufficientData("roc", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [roc]
     */
    fun roc(serie: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = roc(serie.close, timePeriod, previous)

    /**
     * Calculate **Rate of change Percentage: (price-prevPrice)/prevPrice** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.rocPLookback(timePeriod) + previous
            throw InsufficientData("rocP", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [rocP]
     */
    fun rocP(serie: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = rocP(serie.close, timePeriod, previous)

    /**
     * Calculate **Rate of change ratio: (price/prevPrice)** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.rocRLookback(timePeriod) + previous
            throw InsufficientData("rocR", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [rocR]
     */
    fun rocR(serie: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) = rocR(serie.close, timePeriod, previous)

    /**
     * Calculate **Rate of change ratio 100 scale: (price/prevPrice) * 100** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.rocR100Lookback(timePeriod) + previous
            throw InsufficientData("rocR100", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [rocR100]
     */
    fun rocR100(serie: PriceBarSeries, timePeriod: Int = 10, previous: Int = 0) =
        rocR100(serie.close, timePeriod, previous)

    /**
     * Calculate **Relative Strength MetadataProvider** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.rsiLookback(timePeriod) + previous
            throw InsufficientData("rsi", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [rsi]
     */
    fun rsi(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) = rsi(serie.close, timePeriod, previous)

    /**
     * Calculate **Parabolic SAR** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.sarLookback(accelerationFactor, aFMaximum) + previous
            throw InsufficientData("sar", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sar]
     */
    fun sar(
        serie: PriceBarSeries,
        accelerationFactor: Double = 2.000000e-2,
        aFMaximum: Double = 2.000000e-1,
        previous: Int = 0
    ) = sar(serie.high, serie.low, accelerationFactor, aFMaximum, previous)

    /**
     * Calculate **Parabolic SAR - Extended** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
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
            throw InsufficientData("sarExt", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sarExt]
     */
    fun sarExt(
        serie: PriceBarSeries,
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
        serie.high,
        serie.low,
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
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.sinLookback() + previous
            throw InsufficientData("sin", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sin]
     */
    fun sin(serie: PriceBarSeries, previous: Int = 0) = sin(serie.close, previous)

    /**
     * Calculate **Vector Trigonometric Sinh** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.sinhLookback() + previous
            throw InsufficientData("sinh", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sinh]
     */
    fun sinh(serie: PriceBarSeries, previous: Int = 0) = sinh(serie.close, previous)

    /**
     * Calculate **Simple Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.smaLookback(timePeriod) + previous
            throw InsufficientData("sma", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sma]
     */
    fun sma(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = sma(serie.close, timePeriod, previous)

    /**
     * Calculate **Vector Square Root** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.sqrtLookback() + previous
            throw InsufficientData("sqrt", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sqrt]
     */
    fun sqrt(serie: PriceBarSeries, previous: Int = 0) = sqrt(serie.close, previous)

    /**
     * Calculate **Standard Deviation** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.stdDevLookback(timePeriod, deviations) + previous
            throw InsufficientData("stdDev", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [stdDev]
     */
    fun stdDev(serie: PriceBarSeries, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0) =
        stdDev(serie.close, timePeriod, deviations, previous)

    /**
     * Calculate **Stochastic** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.stochLookback(fastKPeriod, slowKPeriod, slowKMA, slowDPeriod, slowDMA) + previous
            throw InsufficientData("stoch", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [stoch]
     */
    fun stoch(
        serie: PriceBarSeries,
        fastKPeriod: Int = 5,
        slowKPeriod: Int = 3,
        slowKMA: MAType = MAType.Ema,
        slowDPeriod: Int = 3,
        slowDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stoch(serie.high, serie.low, serie.close, fastKPeriod, slowKPeriod, slowKMA, slowDPeriod, slowDMA, previous)

    /**
     * Calculate **Stochastic Fast** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.stochFLookback(fastKPeriod, fastDPeriod, fastDMA) + previous
            throw InsufficientData("stochF", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [stochF]
     */
    fun stochF(
        serie: PriceBarSeries,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stochF(serie.high, serie.low, serie.close, fastKPeriod, fastDPeriod, fastDMA, previous)

    /**
     * Calculate **Stochastic Relative Strength MetadataProvider** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.stochRsiLookback(timePeriod, fastKPeriod, fastDPeriod, fastDMA) + previous
            throw InsufficientData("stochRsi", lookback + 1)
        }
        return Pair(output1[0], output2[0])
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [stochRsi]
     */
    fun stochRsi(
        serie: PriceBarSeries,
        timePeriod: Int = 14,
        fastKPeriod: Int = 5,
        fastDPeriod: Int = 3,
        fastDMA: MAType = MAType.Ema,
        previous: Int = 0
    ) = stochRsi(serie.close, timePeriod, fastKPeriod, fastDPeriod, fastDMA, previous)

    /**
     * Calculate **Vector Arithmetic Subtraction** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.subLookback() + previous
            throw InsufficientData("sub", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Calculate **Summation** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.sumLookback(timePeriod) + previous
            throw InsufficientData("sum", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [sum]
     */
    fun sum(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = sum(serie.close, timePeriod, previous)

    /**
     * Calculate **Triple Exponential Moving Average (T3)** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.t3Lookback(timePeriod, volumeFactor) + previous
            throw InsufficientData("t3", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [t3]
     */
    fun t3(serie: PriceBarSeries, timePeriod: Int = 5, volumeFactor: Double = 7.000000e-1, previous: Int = 0) =
        t3(serie.close, timePeriod, volumeFactor, previous)

    /**
     * Calculate **Vector Trigonometric Tan** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.tanLookback() + previous
            throw InsufficientData("tan", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [tan]
     */
    fun tan(serie: PriceBarSeries, previous: Int = 0) = tan(serie.close, previous)

    /**
     * Calculate **Vector Trigonometric Tanh** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.tanhLookback() + previous
            throw InsufficientData("tanh", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [tanh]
     */
    fun tanh(serie: PriceBarSeries, previous: Int = 0) = tanh(serie.close, previous)

    /**
     * Calculate **Triple Exponential Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.temaLookback(timePeriod) + previous
            throw InsufficientData("tema", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [tema]
     */
    fun tema(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = tema(serie.close, timePeriod, previous)

    /**
     * Calculate **True Range** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.trueRangeLookback() + previous
            throw InsufficientData("trueRange", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [trueRange]
     */
    fun trueRange(serie: PriceBarSeries, previous: Int = 0) =
        trueRange(serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Triangular Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.trimaLookback(timePeriod) + previous
            throw InsufficientData("trima", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [trima]
     */
    fun trima(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) =
        trima(serie.close, timePeriod, previous)

    /**
     * Calculate **1-day Rate-Of-Change (ROC) of a Triple Smooth EMA** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.trixLookback(timePeriod) + previous
            throw InsufficientData("trix", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [trix]
     */
    fun trix(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = trix(serie.close, timePeriod, previous)

    /**
     * Calculate **Time serie Forecast** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.tsfLookback(timePeriod) + previous
            throw InsufficientData("tsf", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [tsf]
     */
    fun tsf(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) = tsf(serie.close, timePeriod, previous)

    /**
     * Calculate **Typical Price** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.typPriceLookback() + previous
            throw InsufficientData("typPrice", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [typPrice]
     */
    fun typPrice(serie: PriceBarSeries, previous: Int = 0) = typPrice(serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Ultimate Oscillator** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.ultOscLookback(firstPeriod, secondPeriod, thirdPeriod) + previous
            throw InsufficientData("ultOsc", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [ultOsc]
     */
    fun ultOsc(
        serie: PriceBarSeries,
        firstPeriod: Int = 7,
        secondPeriod: Int = 14,
        thirdPeriod: Int = 28,
        previous: Int = 0
    ) = ultOsc(serie.high, serie.low, serie.close, firstPeriod, secondPeriod, thirdPeriod, previous)

    /**
     * Calculate **Variance** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.varianceLookback(timePeriod, deviations) + previous
            throw InsufficientData("variance", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [variance]
     */
    fun variance(serie: PriceBarSeries, timePeriod: Int = 5, deviations: Double = 1.000000e+0, previous: Int = 0) =
        variance(serie.close, timePeriod, deviations, previous)

    /**
     * Calculate **Weighted Close Price** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.wclPriceLookback() + previous
            throw InsufficientData("wclPrice", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [wclPrice]
     */
    fun wclPrice(serie: PriceBarSeries, previous: Int = 0) = wclPrice(serie.high, serie.low, serie.close, previous)

    /**
     * Calculate **Williams' %R** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.willRLookback(timePeriod) + previous
            throw InsufficientData("willR", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [willR]
     */
    fun willR(serie: PriceBarSeries, timePeriod: Int = 14, previous: Int = 0) =
        willR(serie.high, serie.low, serie.close, timePeriod, previous)

    /**
     * Calculate **Weighted Moving Average** using the provided input data and by default return the most recent result.
     * You can set [previous] if you don't want the most recent result.
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

        if (endOutput.value <= 0) {
            val lookback = core.wmaLookback(timePeriod) + previous
            throw InsufficientData("wma", lookback + 1)
        }
        return output1[0]
    }

    /**
     * Convencience method that allows to use a price-bar [serie] as input.
     * @see [wma]
     */
    fun wma(serie: PriceBarSeries, timePeriod: Int = 30, previous: Int = 0) = wma(serie.close, timePeriod, previous)
}

