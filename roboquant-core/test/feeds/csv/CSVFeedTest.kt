package org.roboquant.feeds.csv

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.roboquant.TestData
import org.roboquant.common.Asset
import java.time.Instant
import java.time.Period
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class CSVFeedTest {

    @Test
    fun getContracts() {
        val feed = CSVFeed(TestData.dataDir() + "US")
        assertEquals(4, feed.assets.size )
        // val c= StockBuilder().invoke("AAPL")
        // assertTrue(feed.assets.contains(c))
        val c2 = feed.assets.find { it.symbol == "AAPL" }
        assertTrue(c2 !== null)
    }

    @Test
    fun noAssets() {
        assertFailsWith<Exception> {
            CSVFeed(TestData.dataDir() + "NON_Existing_DIR")
        }

        val config = CSVConfig(fileExtension = "non_existing_ext")
        val feed1 = CSVFeed(TestData.dataDir() + "US", config)
        assertTrue(feed1.assets.isEmpty())
    }



    @Test
    fun customBuilder() {
        val asset = Asset("TEMPLATE", exchangeCode = "TEST123")
        val config = CSVConfig(template = asset)
        val feed =  CSVFeed(TestData.dataDir() + "US", config)
        assertEquals("TEST123", feed.assets.first().exchangeCode)
    }

    @Test
    fun play() {
        val feed =  CSVFeed(TestData.dataDir() + "US")
        var past = Instant.MIN
        runBlocking {
            for (event in org.roboquant.feeds.play(feed)) {
                assertTrue(event.now > past)
                past = event.now
            }
        }
    }

    @Test
    fun find() {
        val feed =  CSVFeed(TestData.dataDir() + "US")
        val apple = feed.find("AAPL")
        assertNotNull(apple)
    }


    @Test
    fun split() {
        val feed =  CSVFeed(TestData.dataDir() + "US")
        val tfs = feed.split(Period.ofYears(1))
        var cnt = 0
        runBlocking {
            for (tf in tfs)
                for (event in org.roboquant.feeds.play(feed, tf)) {
                   cnt++
                }
        }
        assertEquals(feed.timeline.size, cnt)
    }

    @Test
    fun mergeCSVFeeds() {
        val feed = CSVFeed(TestData.dataDir() + "US")
        val feed2 =  CSVFeed(TestData.dataDir() +"EU")
        feed.merge(feed2)
        assertEquals(2, feed.assets.map {it.currency}.toHashSet().size)
        val feed3 =  CSVFeed(TestData.dataDir() + "FX")
        feed.merge(feed3)
        assertEquals(2, feed.assets.map {it.currency}.toHashSet().size)



    }

}