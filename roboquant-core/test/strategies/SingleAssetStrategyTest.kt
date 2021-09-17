package org.roboquant.strategies

import org.junit.Test
import org.roboquant.common.Asset
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceQuote
import java.time.Instant
import kotlin.test.assertEquals

internal class SingleAssetStrategyTest {

    class MyStrategy(asset: Asset) : SingleAssetStrategy(asset) {

        override fun generate(priceAction: PriceAction, now: Instant): Signal {
            return Signal(asset, Rating.BUY)
        }

    }


    @Test
    fun test()  {
        val asset = Asset("DUMMY")
        val strategy1 = MyStrategy(asset)
        val s = strategy1.generate(PriceQuote(asset, 100.0, 100.0, 100.0, 100.0), Instant.now())
        assertEquals(asset, s.asset)
    }

}