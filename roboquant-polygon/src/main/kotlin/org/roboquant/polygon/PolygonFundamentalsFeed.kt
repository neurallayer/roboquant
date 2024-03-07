/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("LongParameterList")

package org.roboquant.polygon

import io.polygon.kotlin.sdk.rest.PolygonRestClient
import io.polygon.kotlin.sdk.rest.experimental.ExperimentalAPI
import io.polygon.kotlin.sdk.rest.experimental.FinancialForm
import io.polygon.kotlin.sdk.rest.experimental.FinancialForms
import io.polygon.kotlin.sdk.rest.experimental.FinancialsParameters
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.Timeline
import org.roboquant.feeds.Item
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.HistoricFeed
import org.roboquant.polygon.Polygon.availableAssets
import org.roboquant.polygon.Polygon.getRestClient
import java.time.Instant
import java.util.*


/**
 * The financials data (a subset of the XBRL report submitted to the SEC) that is included as actions in a feed.
 *
 * @property asset the asset of the filing
 * @property balanceSheet the balance sheet properties
 * @property cashFlowStatement the cash flow statement properties
 * @property incomeStatement the income flow statement properties
 * @property comprehensiveIncome the comprehensive flow statement properties
 */
data class SecFiling internal constructor(
    val asset: Asset,
    val balanceSheet: Map<String, Double>,
    val cashFlowStatement: Map<String, Double>,
    val incomeStatement: Map<String, Double>,
    val comprehensiveIncome: Map<String, Double>
) : Item

/**
 * This feed provides fundamental data retrieved from Polygon.io.
 *
 * Under the hood, Polygon uses the SEC Filing as its source. As a result, this data will only be available much later
 * after a financial period has completed. This feed uses the filing date as time to mark the data.
 */
class PolygonFundamentalsFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricFeed {

    private val config = PolygonConfig()
    private var client: PolygonRestClient
    private val events = sortedMapOf<Instant, MutableList<SecFiling>>()
    private val logger = Logging.getLogger(this::class)

    /**
     * @see HistoricFeed.timeline
     */
    override val timeline: Timeline
        get() = events.keys.toList()

    /**
     * @see HistoricFeed.timeframe
     */
    override val timeframe
        get() = if (events.isEmpty()) Timeframe.INFINITE else Timeframe(events.firstKey(), events.lastKey(), true)

    /**
     * @see HistoricFeed.assets
     */
    override val assets: SortedSet<Asset>
        get() = events.asSequence().map { entry -> entry.value.map { it.asset } }.flatten().toSortedSet()

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = getRestClient(config)
    }

    /**
     * Return the available assets. Due to the number of API calls made, this requires a
     * non-free subscription at Polygon.io
     */
    val availableAssets: List<Asset> by lazy {
        availableAssets(client)
    }

    @OptIn(ExperimentalAPI::class)
    private fun FinancialForm?.toMap() = this?.mapValues { it.value.value } ?: emptyMap()

    @OptIn(ExperimentalAPI::class)
    private fun financials2Action(symbol: String, forms: FinancialForms): SecFiling {
        val asset = Asset(symbol)
        val balanceSheet = forms.balanceSheet.toMap()
        val cashFlowStatement = forms.cashFlowStatement.toMap()
        val incomeStatement = forms.incomeStatement.toMap()
        val comprehensiveIncome = forms.comprehensiveIncome.toMap()
        return SecFiling(
            asset,
            balanceSheet,
            cashFlowStatement,
            incomeStatement,
            comprehensiveIncome
        )
    }

    /**
     * Retrieve [SecFiling] data for the provided [symbols]. The [coverPeriod] determines the type of financials and
     * [limit] the maximum number of results returned
     *
     * This is just a first feasibility test and no useful attributes are yet included.
     */
    @OptIn(ExperimentalAPI::class)
    fun retrieve(
        vararg symbols: String,
        coverPeriod: String = "quarterly",
        limit: Int = 10,
    ) {
        require(coverPeriod in setOf("annual", "quarterly", "ttm"))
        for (symbol in symbols) {
            val param = FinancialsParameters(ticker = symbol, limit = limit, timeframe = coverPeriod)
            val results = client.experimentalClient.getFinancialsBlocking(param).results ?: emptyList()
            for (financials in results) {
                logger.trace { financials.toString() }
                if (financials.filingDate == null || financials.financials == null) continue
                val action = financials2Action(symbol, financials.financials!!)
                val time = Instant.parse(financials.filingDate + "T23:59:59Z")
                add(time, action)
            }
        }
    }

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        events.forEach {
            val event = Event(it.key, it.value)
            channel.send(event)
        }
    }

    /**
     * Add a new [action] to this feed at the provided [time]
     */
    @Synchronized
    private fun add(time: Instant, action: SecFiling) {
        val actions = events.getOrPut(time) { mutableListOf() }
        actions.add(action)
    }

}
