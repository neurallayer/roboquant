
@file:Suppress("KotlinConstantConditions")

import org.roboquant.Roboquant
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.ta.TaLibStrategy

fun vwap() {
    val strategy = TaLibStrategy.vwap(20, 100)
    val feed = AvroFeed.sp500()

    val rq = Roboquant(strategy)
    rq.run(feed)

    println(rq.broker.account.summary())
}


fun main() {
    when("VWAP") {
        "VWAP" -> vwap()
    }
}