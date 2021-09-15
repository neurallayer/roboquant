package org.roboquant.feeds.csv

import java.time.Instant

fun interface TimeParser {
    fun parse(s: String): Instant
}