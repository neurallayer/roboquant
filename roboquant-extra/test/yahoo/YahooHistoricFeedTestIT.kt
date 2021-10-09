package org.roboquant.yahoo

import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.junit.Test


internal class YahooHistoricFeedTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_YAHOO") ?: return
        val feed = YahooHistoricFeed()
        val asset = Asset("AAPL")
        feed.retrieve(asset, timeFrame = TimeFrame.fromYears(2015, 2020))
    }


}