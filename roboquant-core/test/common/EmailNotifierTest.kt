package org.roboquant.common

import org.junit.Test
import kotlin.test.assertEquals


internal class EmailNotifierTest {

    @Test
    fun test() {
        var p = EmailNotifier.usingGmail("dummy@gmail.com", "some_secret")
        assertEquals("dummy@gmail.com", p.fromAddress.address)

        p = EmailNotifier.usingYahoo("dummy@gmail.com", "some_secret")
        assertEquals("dummy@gmail.com", p.fromAddress.address)

        p = EmailNotifier.usingOutlook("dummy@gmail.com", "some_secret")
        assertEquals("dummy@gmail.com", p.fromAddress.address)

        p = EmailNotifier("smtp.host.local","dummy@gmail.com", "some_secret")
        assertEquals("dummy@gmail.com", p.fromAddress.address)

    }

}