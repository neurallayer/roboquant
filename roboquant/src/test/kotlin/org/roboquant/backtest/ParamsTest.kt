package org.roboquant.backtest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParamsTest {

    @Test
    fun basic() {
        val params = Params()
        params["param1"] = 12
        params["param2"] = 13.0
        params["param3"] = "text"
        params["param4"] = listOf(1, 3.0, "text")

        assertEquals(12, params.getInt("param1"))
        assertEquals(13.0, params.getDouble("param2"))
        assertEquals("text", params.getString("param3"))
        assertEquals(listOf(1, 3.0, "text"), params["param4"])
        assertNull(params["param5"])

        assertThrows<java.lang.ClassCastException> {
            params.getInt("param3")
        }

    }

}