package org.roboquant.jupyter

import org.junit.Test
import kotlin.test.assertTrue

internal class WelcomeTest {

    @Test
    fun test() {
        val w = Welcome()
        assertTrue(w.asHTML().isNotBlank())
    }

}