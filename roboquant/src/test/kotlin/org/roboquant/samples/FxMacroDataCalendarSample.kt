/*
 * Copyright 2020-2026 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package org.roboquant.samples

import org.roboquant.common.Event
import org.roboquant.common.Signal
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.run
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.Strategy
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Ignore
import kotlin.test.Test

internal class FxMacroDataCalendarSample {

    @Test
    @Ignore
    internal fun releaseCalendarRiskWindow() {
        val events = fetchCalendar("USD", "2026-07-01", "2026-07-20")
        val blackoutDates = topTierBlackoutDates(events).toSet()

        val baseStrategy = EMACrossover.PERIODS_5_15
        val strategy = MacroBlackoutStrategy(baseStrategy, blackoutDates)
        val feed = RandomWalk.lastYears(years = 1, nAssets = 2)

        val account = run(feed, strategy)
        println("Top-tier USD macro blackout dates: ${blackoutDates.sorted()}")
        println(account)
    }

    internal fun fetchCalendar(currency: String, startDate: String, endDate: String): String {
        val uri = URI.create(
            "https://fxmacrodata.com/api/v1/calendar/$currency" +
                "?start_date=$startDate&end_date=$endDate"
        )
        val request = HttpRequest.newBuilder(uri).GET().build()
        return HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())
            .body()
    }

    private fun topTierBlackoutDates(json: String): List<LocalDate> {
        val eventBlocks = json.split("},{")
        val datePattern = Regex("\"(announcement_datetime_utc|date)\"\\s*:\\s*\"([^\"]+)\"")

        return eventBlocks
            .filter { it.contains("\"top_tier_for_currency\":true") || it.contains("\"market_tier\":1") }
            .mapNotNull { block ->
                datePattern.find(block)?.groupValues?.get(2)?.take(10)?.let(LocalDate::parse)
            }
            .distinct()
            .sorted()
    }

    private class MacroBlackoutStrategy(
        private val delegate: Strategy,
        private val blackoutDates: Set<LocalDate>,
        private val zoneId: ZoneId = ZoneOffset.UTC
    ) : Strategy {

        override fun createSignals(event: Event): List<Signal> {
            val eventDate = event.time.atZone(zoneId).toLocalDate()
            if (eventDate in blackoutDates) return emptyList()
            return delegate.createSignals(event)
        }
    }
}
