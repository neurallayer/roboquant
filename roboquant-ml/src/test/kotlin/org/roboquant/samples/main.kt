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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.common.Asset
import org.roboquant.common.percent
import org.roboquant.common.returns
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.ml.drop
import org.roboquant.ml.dropLast
import org.roboquant.ml.takeLast
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import org.roboquant.ta.PriceBarSeries
import smile.data.DataFrame
import smile.data.formula.Formula
import smile.data.vector.DoubleVector
import smile.regression.GradientTreeBoost
import smile.regression.gbm



fun main() {
    test()
    // graalPy()
}


class MyStrat(
    private val historySize: Int,
    private val foreCastPeriod: Int = 1,
    private val perc: Double = 5.percent
) : Strategy {

    private val history = mutableMapOf<Asset, PriceBarSeries>()
    private var models = mutableMapOf<Asset, GradientTreeBoost>()


    private fun predict(asset: Asset, hist: PriceBarSeries): Double {
        if (asset !in models) {
            val y = DoubleVector.of("y", hist.close.returns(foreCastPeriod).drop(1))
            val o = DoubleVector.of("open", hist.open.returns(1).dropLast(foreCastPeriod))
            val h = DoubleVector.of("high", hist.high.returns(1).dropLast(foreCastPeriod))
            val l = DoubleVector.of("low", hist.low.returns(1).dropLast(foreCastPeriod))
            val c = DoubleVector.of("close", hist.close.returns(1).dropLast(foreCastPeriod))
            val df = DataFrame.of(y, o, h, l, c)
            models[asset] = gbm(Formula.lhs("y"), df)
        }
        val model = models.getValue(asset)
        val y = DoubleVector.of("y", DoubleArray(1))
        val o = DoubleVector.of("open", hist.open.takeLast(2).returns())
        val h = DoubleVector.of("high", hist.open.takeLast(2).returns())
        val l = DoubleVector.of("low", hist.low.takeLast(2).returns())
        val c = DoubleVector.of("close", hist.open.takeLast(2).returns())
        val df = DataFrame.of(y, o, h, l, c)
        val results = model.predict(df)
        println(results.last())
        return results.last()
    }

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        val time = event.time
        event.actions.filterIsInstance<PriceBar>().forEach {
            val h = history.getOrPut(it.asset) { PriceBarSeries(historySize) }
            h.add(it, time)
            if (h.isFull()) {
                val c = predict(it.asset, h)
                val rating = when {
                    c > perc -> Rating.BUY
                    c < -perc -> Rating.SELL
                    else -> null
                }

                if (rating != null) result.add(Signal(it.asset, rating))

            }
        }
        return result
    }

    override fun reset() {
        models.clear()
        history.clear()
    }
}





fun test() {

    val feed = AvroFeed.sp500()
    val myStrat = MyStrat(250, 10, 5.percent)

    val rq = Roboquant(myStrat, ProgressMetric(), logger = ConsoleLogger())
    rq.run(feed)
    println(rq.broker.account.summary())

}

/*
fun graalPy() {

    val pythonCode = """
        
        class Plus:
            def __init__(self, value):
                self.value = value
                
            def plus(self, v):
                self.value += v
                return self.value
     
        p = Plus(12)
        lambda x: p.plus(x)
    """.trimIndent()

    val ctx = Context.newBuilder("python").
        allowIO(IOAccess.ALL).
        option("python.Executable", "").
        build()

    ctx.use { context ->
        val function = context.eval("python", pythonCode)
        assert(function.canExecute())

        repeat(10) {
            val x: Int = function.execute(41).asInt()
            println(x)
        }

    }
}
*/
