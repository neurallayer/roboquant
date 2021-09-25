package org.roboquant.yahoo

import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.junit.Test


internal class YahooFinanceFeedTestIT {

    @Test
    fun test() {
        System.getProperty("yahoo") ?: return
        val feed = YahooFinanceFeed()
        val asset = Asset("AAPL")
        feed.retrieve(asset, timeFrame = TimeFrame.fromYears(2015, 2020))
    }


}