package org.roboquant.feeds

import org.junit.Test
import kotlin.test.*
import org.roboquant.TestData

internal class ActionTest {



    @Test
    fun corporateAction() {
        val asset = TestData.euStock()
        val action = CorporateAction(asset, "SPLIT", 2.0)
        assertEquals("SPLIT", action.type)
    }

    @Test
    fun newsAction() {
        val item = NewsAction.NewsItem("Some text", mapOf("source" to "TWITTER"))
        val action = NewsAction(listOf(item))
        assertEquals(1, action.items.size)
        assertEquals(1, action.items[0].meta.size)
        assertFalse(action.items.isEmpty())
    }
}