package org.roboquant.alpaca

import org.roboquant.common.Asset
import kotlin.test.*


internal class AlpacaFeedTestIT {

    @Test
    fun test() {
        System.getProperty("alpaca") ?: return
        val feed = AlpacaFeed()
        val asset = Asset("AAPL")
        feed.subscribe(listOf(asset))
        feed.disconnect()
        feed.connect()
    }

}