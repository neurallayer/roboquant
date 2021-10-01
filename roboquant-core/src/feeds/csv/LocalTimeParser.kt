package org.roboquant.feeds.csv

import org.roboquant.common.Exchange
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Datetime parser that parses local time or dates
 *
 * @constructor
 *
 */
internal class LocalTimeParser(pattern: String, val dateFormat:Boolean = pattern.length < 11, val exchange:Exchange = Exchange.getInstance("") ) : TimeParser {

    private val dtf: DateTimeFormatter

    init {
        val zoneId = exchange.zoneId
        dtf = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    }

    override fun parse(s: String): Instant {
        return if (dateFormat) {
            val date = LocalDate.parse(s, dtf)
            exchange.getClosingTime(date)
         } else {
            ZonedDateTime.parse(s, dtf).toInstant()
        }

    }


}