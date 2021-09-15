package org.roboquant


import kotlin.test.*

internal class PhaseTest {

    @Test
    fun test() {
        assertEquals("VALIDATE", Phase.VALIDATE.value)
        assertEquals("MAIN", Phase.MAIN.value)
    }


}