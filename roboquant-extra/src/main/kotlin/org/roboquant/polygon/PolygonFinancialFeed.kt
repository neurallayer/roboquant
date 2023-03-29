/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import io.polygon.kotlin.sdk.rest.experimental.FinancialsParameters
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.Timeline
import org.roboquant.feeds.*
import org.roboquant.polygon.Polygon.availableAssets
import org.roboquant.polygon.Polygon.getRestClient
import java.time.Instant
import java.util.*


/**
 * The financial data (a subset of the XBRL report submitted to the SEC) that is included as actions in a feed.
 */
data class SecFiling(val asset: Asset) : Action


/**
 * Financial data feed from Polygon.io. Under the hood, Polygon uses the SEC Filing as the source. As a result this
 * data may be delivered later after a financial period has completed.
 */
class PolygonFinancialFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricFeed {

    private val config = PolygonConfig()
    private var client: PolygonRestClient
    private val events = sortedMapOf<Instant, MutableList<SecFiling>>()
    private val logger = Logging.getLogger(this::class)

    override val timeline: Timeline
        get() = events.keys.toList()

    override val timeframe
        get() = if (events.isEmpty()) Timeframe.INFINITE else Timeframe(events.firstKey(), events.lastKey(), true)

    override val assets: SortedSet<Asset>
        get() = events.asSequence().map { entry -> entry.value.map { it.asset } }.flatten().toSortedSet()

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = getRestClient(config)
    }

    /**
     * Return the available assets. Due to the amount of API calls made, this requires a
     * non-free subscription at Polygon.io
     */
    val availableAssets: List<Asset> by lazy {
        availableAssets(client)
    }


    /**
     * Retrieve [SecFiling] data for the provided [symbols].
     *
     * This is just a first feasibility test and no useful attributes are yet included.
     */
    @OptIn(ExperimentalAPI::class)
    fun retrieve(
        vararg symbols: String
    ) {
        for (symbol in symbols) {
            val param = FinancialsParameters(ticker=symbol)
            val results = client.experimentalClient.getFinancialsBlocking(param).results ?: emptyList()
            for (financial in results) {
                logger.trace { financial }
                if (financial.filingDate == null) continue
                val asset = Asset(symbol)
                val action = SecFiling(asset)
                val time = Instant.parse(financial.filingDate + "T23:59:59Z")
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
            val event = Event(it.value, it.key)
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