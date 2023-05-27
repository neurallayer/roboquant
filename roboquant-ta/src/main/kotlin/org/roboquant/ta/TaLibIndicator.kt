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

package org.roboquant.ta

import org.roboquant.feeds.Action
import org.roboquant.metrics.Indicator
import java.time.Instant

class TaLibIndicator  (
    barCount: Int = 20,
    private val block: TaLib.(series: PriceBarSerie) -> Map<String, Double>
) : Indicator {

    private val taLib = TaLib()
    private val series = PriceBarSerie(barCount)

    override fun calculate(action: Action, time: Instant): Map<String, Double> {
        return if (series.add(action)) {
            block.invoke(taLib, series)
        } else {
            emptyMap()
        }
    }

    override fun clear() {
        series.clear()
    }

    /**
     * Commonly used indicators using the TaLib library
     */
    companion object {
        
        fun rsi(barCount: Int = 10) : TaLibIndicator {
            return TaLibIndicator(barCount+1) {
                mapOf("rsi" to rsi(it, barCount))
            }
        }
        
        fun bbands(barCount: Int = 10) : TaLibIndicator {
            return TaLibIndicator(barCount) {
                val (high, mid, low) = bbands(it, barCount)
                mapOf("bb.low" to low, "bb.high" to high, "bb.mid" to mid)
            }
        }
        
        fun ema(barCount: Int = 10) : TaLibIndicator {
            return TaLibIndicator(barCount) {
                mapOf("ema" to ema(it, barCount))
            }
        }

        fun sma(barCount: Int = 10) : TaLibIndicator {
            return TaLibIndicator(barCount) {
                mapOf("sma" to sma(it, barCount))
            }
        }
        
        
    }

}

